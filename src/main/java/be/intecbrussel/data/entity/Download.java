package be.intecbrussel.data.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;

@Entity
public class Download extends AbstractEntity {

    private UUID upload;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime receivedAt;
    private String convertedTo;
    private Integer score;
    private boolean isActive;

    public UUID getUpload() {
        return upload;
    }

    public void setUpload(UUID upload) {
        this.upload = upload;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getConvertedTo() {
        return convertedTo;
    }

    public void setConvertedTo(String convertedTo) {
        this.convertedTo = convertedTo;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}
