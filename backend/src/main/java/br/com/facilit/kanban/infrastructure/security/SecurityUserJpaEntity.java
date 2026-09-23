package br.com.facilit.kanban.infrastructure.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class SecurityUserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "responsible_id")
    private UUID responsibleId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SecurityUserJpaEntity() {
    }

    SecurityUserJpaEntity(
            UUID id,
            String email,
            String passwordHash,
            String role,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt) {
        this(id, email, passwordHash, role, enabled, null, createdAt, updatedAt);
    }

    SecurityUserJpaEntity(
            UUID id,
            String email,
            String passwordHash,
            String role,
            boolean enabled,
            UUID responsibleId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.responsibleId = responsibleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    void replaceCredentials(String email, String passwordHash, Instant updatedAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.updatedAt = updatedAt;
    }

    public void changeEmail(String email, Instant updatedAt) {
        if (!this.email.equals(email)) {
            this.email = email;
            this.updatedAt = updatedAt;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UUID getResponsibleId() {
        return responsibleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
