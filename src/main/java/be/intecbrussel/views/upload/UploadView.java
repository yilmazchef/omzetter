package be.intecbrussel.views.upload;

import be.intecbrussel.data.entity.Upload;
import be.intecbrussel.data.service.UploadService;
import be.intecbrussel.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;

@PageTitle("Upload")
@Route(value = "up/:uploadID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class UploadView extends Div implements BeforeEnterObserver {

    private final String UPLOAD_ID = "uploadID";
    private final String UPLOAD_EDIT_ROUTE_TEMPLATE = "up/%s/edit";

    private Grid<Upload> grid = new Grid<>(Upload.class, false);

    private TextField filename;
    private TextField extension;
    private TextField createdBy;
    private TextField updatedBy;
    private DateTimePicker createdAt;
    private DateTimePicker updatedAt;
    private DateTimePicker expiresAt;
    private com.vaadin.flow.component.upload.Upload content;
    private TextField accessCode;
    private Checkbox isActive;
    private TextField downloadCount;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Upload> binder;

    private Upload upload;

    private final UploadService uploadService;

    @Autowired
    public UploadView(UploadService uploadService) {

        this.uploadService = uploadService;
        addClassNames("upload-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("filename").setAutoWidth(true);
        grid.addColumn("extension").setAutoWidth(true);
        grid.addColumn("createdBy").setAutoWidth(true);
        grid.addColumn("updatedBy").setAutoWidth(true);
        grid.addColumn("createdAt").setAutoWidth(true);
        grid.addColumn("updatedAt").setAutoWidth(true);
        grid.addColumn("expiresAt").setAutoWidth(true);
        LitRenderer<Upload> contentRenderer = LitRenderer.<Upload>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.content} /></span>")
                .withProperty("content", Upload::getContent);
        grid.addColumn(contentRenderer).setHeader("Content").setWidth("96px").setFlexGrow(0);

        grid.addColumn("accessCode").setAutoWidth(true);
        LitRenderer<Upload> isActiveRenderer = LitRenderer.<Upload>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActive -> isActive.isIsActive() ? "check" : "minus").withProperty("color",
                        isActive -> isActive.isIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActiveRenderer).setHeader("Is Active").setAutoWidth(true);

        grid.addColumn("downloadCount").setAutoWidth(true);
        grid.setItems(query -> uploadService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(UPLOAD_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UploadView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Upload.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(downloadCount).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("downloadCount");

        binder.bindInstanceFields(this);

        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        this.content.setAcceptedFileTypes("image/*");
        this.content.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });

        this.content.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();

            // TODO: data must be converted here.
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            this.content.getElement().setPropertyJson("files", Json.createArray());
            Notification.show("Data is uploaded. Temp download link:" + dataUrl, 3000, Notification.Position.MIDDLE)
                    .open();
            uploadBuffer.reset();
        });

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.upload == null) {
                    this.upload = new Upload();
                }
                binder.writeBean(this.upload);
                this.upload.setContent(uploadBuffer.toByteArray());

                uploadService.update(this.upload);
                clearForm();
                refreshGrid();
                Notification.show("Upload details stored.");
                UI.getCurrent().navigate(UploadView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the upload details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> uploadId = event.getRouteParameters().get(UPLOAD_ID).map(UUID::fromString);
        if (uploadId.isPresent()) {
            Optional<Upload> uploadFromBackend = uploadService.get(uploadId.get());
            if (uploadFromBackend.isPresent()) {
                populateForm(uploadFromBackend.get());
            } else {
                Notification.show(String.format("The requested upload was not found, ID = %s", uploadId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(UploadView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        filename = new TextField("Filename");
        extension = new TextField("Extension");
        createdBy = new TextField("Created By");
        updatedBy = new TextField("Updated By");
        createdAt = new DateTimePicker("Created At");
        createdAt.setStep(Duration.ofSeconds(1));
        updatedAt = new DateTimePicker("Updated At");
        updatedAt.setStep(Duration.ofSeconds(1));
        expiresAt = new DateTimePicker("Expires At");
        expiresAt.setStep(Duration.ofSeconds(1));
        Label contentLabel = new Label("Content");
        content = new com.vaadin.flow.component.upload.Upload();
        content.getStyle().set("box-sizing", "border-box");
        content.getElement().appendChild(new Label("The file is uploaded to the server and is available. "
                + "The temporary download link is shown below.").getElement());
        accessCode = new TextField("Access Code");
        isActive = new Checkbox("Is Active");
        downloadCount = new TextField("Download Count");
        Component[] fields = new Component[] { filename, extension, createdBy, updatedBy, createdAt, updatedAt,
                expiresAt,
                contentLabel, content, accessCode, isActive, downloadCount };

        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Upload value) {
        this.upload = value;
        binder.readBean(this.upload);
    }
}
