package be.intecbrussel.views.upload;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("upload-all")
public class UploadUserVİew extends Div {

    public UploadUserVİew() {

        final var buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAutoUpload(false);

        Button uploadAllButton = new Button("Upload All Files");
        uploadAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadAllButton.addClickListener(event -> {
            // No explicit Flow API for this at the moment
            upload.getElement().callJsFunction("uploadFiles");
            Notification.show("Uploaded all files", 3000, Position.TOP_CENTER).open();
        });

        add(upload, uploadAllButton);
    }

}
