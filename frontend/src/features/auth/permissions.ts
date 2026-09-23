import type { AuthUser } from "../../api/auth";
import type { Project } from "../../api/kanban";

export function isAdministrator(user: AuthUser): boolean {
  return user.authorities.includes("ROLE_ADMIN");
}

export function canManageProject(user: AuthUser, project: Project): boolean {
  return (
    isAdministrator(user) ||
    (user.responsibleId !== null &&
      project.responsibleIds.includes(user.responsibleId))
  );
}
