package be.intecbrussel.views.download;

import be.intecbrussel.data.entity.Download;
import be.intecbrussel.data.service.DownloadService;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToUuidConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Download")
@Route(value = "down/:downloadID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class DownloadView extends Div implements BeforeEnterObserver {

    private final String DOWNLOAD_ID = "downloadID";
    private final String DOWNLOAD_EDIT_ROUTE_TEMPLATE = "down/%s/edit";

    private Grid<Download> grid = new Grid<>(Download.class, false);

    private TextField upload;
    private TextField requestedBy;
    private DateTimePicker requestedAt;
    private DateTimePicker receivedAt;
    private TextField convertedTo;
    private TextField score;
    private Checkbox isActive;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Download> binder;

    private Download download;

    private final DownloadService downloadService;

    @Autowired
    public DownloadView(DownloadService downloadService) {
        this.downloadService = downloadService;
        addClassNames("download-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("upload").setAutoWidth(true);
        grid.addColumn("requestedBy").setAutoWidth(true);
        grid.addColumn("requestedAt").setAutoWidth(true);
        grid.addColumn("receivedAt").setAutoWidth(true);
        grid.addColumn("convertedTo").setAutoWidth(true);
        grid.addColumn("score").setAutoWidth(true);
        LitRenderer<Download> isActiveRenderer = LitRenderer.<Download>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActive -> isActive.isIsActive() ? "check" : "minus").withProperty("color",
                        isActive -> isActive.isIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActiveRenderer).setHeader("Is Active").setAutoWidth(true);

        grid.setItems(query -> downloadService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(DOWNLOAD_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(DownloadView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Download.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(upload).withConverter(new StringToUuidConverter("Invalid UUID")).bind("upload");
        binder.forField(score).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("score");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.download == null) {
                    this.download = new Download();
                }
                binder.writeBean(this.download);

                downloadService.update(this.download);
                clearForm();
                refreshGrid();
                Notification.show("Download details stored.");
                UI.getCurrent().navigate(DownloadView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the download details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> downloadId = event.getRouteParameters().get(DOWNLOAD_ID).map(UUID::fromString);
        if (downloadId.isPresent()) {
            Optional<Download> downloadFromBackend = downloadService.get(downloadId.get());
            if (downloadFromBackend.isPresent()) {
                populateForm(downloadFromBackend.get());
            } else {
                Notification.show(String.format("The requested download was not found, ID = %s", downloadId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(DownloadView.class);
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
        upload = new TextField("Upload");
        requestedBy = new TextField("Requested By");
        requestedAt = new DateTimePicker("Requested At");
        requestedAt.setStep(Duration.ofSeconds(1));
        receivedAt = new DateTimePicker("Received At");
        receivedAt.setStep(Duration.ofSeconds(1));
        convertedTo = new TextField("Converted To");
        score = new TextField("Score");
        isActive = new Checkbox("Is Active");
        Component[] fields = new Component[]{upload, requestedBy, requestedAt, receivedAt, convertedTo, score,
                isActive};

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

    private void populateForm(Download value) {
        this.download = value;
        binder.readBean(this.download);

    }
}
