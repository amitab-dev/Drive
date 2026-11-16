package amitab.drive.repository;

import amitab.drive.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    Optional<File> findByFileKey(String key);
}
