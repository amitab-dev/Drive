package amitab.drive.repository;

import amitab.drive.entity.File;
import amitab.drive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    Optional<File> findByFileKey(String key);

    List<File> findByOwner(User user);

    @Query("SELECT f FROM File f WHERE f.owner = ?1 AND f.trashedAt IS NULL")
    List<File> findByOwnerAndTrashedAtIsNull(User owner);
}
