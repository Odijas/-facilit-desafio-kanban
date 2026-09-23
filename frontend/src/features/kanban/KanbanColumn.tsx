import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Divider,
  Stack,
  Typography,
} from "@mui/material";
import type { Project, ProjectStatus } from "../../api/kanban";

type KanbanColumnProps = {
  label: string;
  status: ProjectStatus;
  projects: Project[];
  responsibleNames: ReadonlyMap<string, string>;
  transitionPending: boolean;
  onDropProject: (projectId: string, status: ProjectStatus) => void;
  onEditProject: (project: Project) => void;
  onDeleteProject: (project: Project) => void;
};

export function KanbanColumn({
  label,
  status,
  projects,
  responsibleNames,
  transitionPending,
  onDropProject,
  onEditProject,
  onDeleteProject,
}: KanbanColumnProps) {
  return (
    <Box
      aria-label={`Coluna ${label}`}
      role="region"
      onDragOver={(event) => event.preventDefault()}
      onDrop={(event) => {
        event.preventDefault();
        onDropProject(event.dataTransfer.getData("text/plain"), status);
      }}
      sx={{
        minHeight: 360,
        bgcolor: "action.hover",
        borderRadius: 2,
        p: 2,
      }}
    >
      <Stack direction="row" spacing={1} sx={{ alignItems: "center", mb: 2 }}>
        <Typography sx={{ flexGrow: 1, fontWeight: 700 }}>{label}</Typography>
        <Chip label={projects.length} size="small" />
      </Stack>

      <Stack spacing={2}>
        {projects.length === 0 && (
          <Typography color="text.secondary" variant="body2">
            Nenhum projeto nesta coluna.
          </Typography>
        )}

        {projects.map((project) => (
          <Card
            key={project.id}
            aria-label={`Projeto ${project.name}`}
            draggable={!transitionPending}
            role="article"
            variant="outlined"
            onDragStart={(event) => {
              event.dataTransfer.effectAllowed = "move";
              event.dataTransfer.setData("text/plain", project.id);
            }}
          >
            <CardContent>
              <Stack spacing={1}>
                <Typography sx={{ fontWeight: 700 }}>{project.name}</Typography>
                <Typography color="text.secondary" variant="body2">
                  {project.responsibleIds
                    .map((id) => responsibleNames.get(id) ?? id)
                    .join(", ")}
                </Typography>
                {project.plannedEnd !== null && (
                  <Typography color="text.secondary" variant="body2">
                    Término previsto: {project.plannedEnd}
                  </Typography>
                )}
                <Stack direction="row" spacing={1} sx={{ flexWrap: "wrap" }}>
                  <Chip
                    label={`${project.remainingTimePercentage}% restante`}
                    size="small"
                    variant="outlined"
                  />
                  {project.delayDays > 0 && (
                    <Chip
                      color="error"
                      label={`${project.delayDays} dia(s) de atraso`}
                      size="small"
                    />
                  )}
                </Stack>
              </Stack>
            </CardContent>
            <Divider />
            <CardActions>
              <Button size="small" onClick={() => onEditProject(project)}>
                Editar
              </Button>
              <Button
                color="error"
                size="small"
                onClick={() => onDeleteProject(project)}
              >
                Excluir
              </Button>
            </CardActions>
          </Card>
        ))}
      </Stack>
    </Box>
  );
}
