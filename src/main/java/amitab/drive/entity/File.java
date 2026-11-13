package amitab.drive.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.node.StringNode;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User ownerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FileType type;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status = FileStatus.PENDING;

    @Column(name = "trashed_at")
    private Instant trashedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
