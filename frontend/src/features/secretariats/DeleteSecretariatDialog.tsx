import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";
import type { Secretariat } from "../../api/kanban";

type DeleteSecretariatDialogProps = {
  secretariat: Secretariat | null;
  pending: boolean;
  onCancel: () => void;
  onConfirm: (secretariatId: string) => void;
};

export function DeleteSecretariatDialog({
  secretariat,
  pending,
  onCancel,
  onConfirm,
}: DeleteSecretariatDialogProps) {
  return (
    <Dialog
      open={secretariat !== null}
      onClose={pending ? undefined : onCancel}
    >
      <DialogTitle>Excluir secretaria?</DialogTitle>
      <DialogContent>
        <Typography>
          {secretariat === null
            ? ""
            : `A secretaria “${secretariat.name}” será excluída.`}
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button disabled={pending} onClick={onCancel}>
          Cancelar
        </Button>
        <Button
          color="error"
          disabled={pending || secretariat === null}
          onClick={() => {
            if (secretariat !== null) {
              onConfirm(secretariat.id);
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
