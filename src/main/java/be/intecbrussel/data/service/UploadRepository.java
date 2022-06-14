package be.intecbrussel.data.service;

import be.intecbrussel.data.entity.Upload;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

}