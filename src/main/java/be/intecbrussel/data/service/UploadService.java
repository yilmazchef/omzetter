package be.intecbrussel.data.service;

import be.intecbrussel.data.entity.Upload;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

    private final UploadRepository repository;

    @Autowired
    public UploadService(UploadRepository repository) {
        this.repository = repository;
    }

    public Optional<Upload> get(UUID id) {
        return repository.findById(id);
    }

    public Upload update(Upload entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Upload> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
