package br.com.facilit.kanban.infrastructure.persistence.responsible;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "responsibles")
public class ResponsibleJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String position;

    @Column(name = "secretariat_id")
    private UUID secretariatId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ResponsibleJpaEntity() {
    }

    void replace(
            UUID id,
            String name,
            String email,
            String position,
            UUID secretariatId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.position = position;
        this.secretariatId = secretariatId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPosition() {
        return position;
    }

    public UUID getSecretariatId() {
        return secretariatId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
