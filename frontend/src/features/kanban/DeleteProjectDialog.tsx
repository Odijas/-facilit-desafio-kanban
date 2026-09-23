import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";
import type { Project } from "../../api/kanban";

type DeleteProjectDialogProps = {
  project: Project | null;
  pending: boolean;
  onCancel: () => void;
  onConfirm: (projectId: string) => void;
};

export function DeleteProjectDialog({
  project,
  pending,
  onCancel,
  onConfirm,
}: DeleteProjectDialogProps) {
  return (
    <Dialog open={project !== null} onClose={pending ? undefined : onCancel}>
      <DialogTitle>Excluir projeto?</DialogTitle>
      <DialogContent>
        <Typography>
          {project === null ? "" : `O projeto “${project.name}” será removido.`}
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button disabled={pending} onClick={onCancel}>
          Cancelar
        </Button>
        <Button
          color="error"
          disabled={pending || project === null}
          onClick={() => {
            if (project !== null) {
              onConfirm(project.id);
            }
          }}
          variant="contained"
        >
          {pending ? "Excluindo..." : "Excluir"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
