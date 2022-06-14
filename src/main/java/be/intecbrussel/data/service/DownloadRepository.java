package be.intecbrussel.data.service;

import be.intecbrussel.data.entity.Download;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadRepository extends JpaRepository<Download, UUID> {

}