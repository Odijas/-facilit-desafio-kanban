package br.com.facilit.kanban.application.common;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public record Actor(Role role, UUID responsibleId) {

    public enum Role {
        ADMIN,
        RESPONSIBLE
    }

    public Actor {
        Objects.requireNonNull(role, "role is required");
        if (role == Role.RESPONSIBLE && responsibleId == null) {
            throw new IllegalArgumentException("responsibleId is required for responsible actors");
        }
        if (role == Role.ADMIN && responsibleId != null) {
            throw new IllegalArgumentException("administrator actors must not reference a responsible");
        }
    }

    public static Actor admin() {
        return new Actor(Role.ADMIN, null);
    }

    public static Actor responsible(UUID responsibleId) {
        return new Actor(Role.RESPONSIBLE, responsibleId);
    }

    /**
     * Identificação do ator para log de negócio: perfil e, no caso do responsável, só o id.
     */
    public String auditLabel() {
        return isAdmin() ? "ADMIN" : "RESPONSIBLE:" + responsibleId;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean canManage(Collection<UUID> projectResponsibleIds) {
        return isAdmin() || projectResponsibleIds.contains(responsibleId);
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenOperationException("Apenas o administrador pode realizar esta operação.");
        }
    }
}
