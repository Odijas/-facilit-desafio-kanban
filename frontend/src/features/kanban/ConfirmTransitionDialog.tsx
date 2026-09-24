import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import type { ProjectStatus } from "../../api/kanban";

export type PendingTransitionConfirmation = {
  projectId: string;
  projectName: string;
  targetLabel: string;
  status: ProjectStatus;
  message: string;
};

type ConfirmTransitionDialogProps = {
  confirmation: PendingTransitionConfirmation | null;
  pending: boolean;
  onCancel: () => void;
  onConfirm: (confirmation: PendingTransitionConfirmation) => void;
};

/**
 * Pede confirmação quando a API responde CONFIRMATION_REQUIRED: a transição é válida, mas apaga uma data
 * já registrada. A mensagem exibida é a do servidor, que diz qual data será apagada.
 */
export function ConfirmTransitionDialog({
  confirmation,
  pending,
  onCancel,
  onConfirm,
}: ConfirmTransitionDialogProps) {
  return (
    <Dialog
      open={confirmation !== null}
      onClose={pending ? undefined : onCancel}
    >
      <DialogTitle>
        {confirmation === null
          ? "Confirmar transição?"
          : `Mover “${confirmation.projectName}” para ${confirmation.targetLabel}?`}
      </DialogTitle>
      <DialogContent>
        <DialogContentText>{confirmation?.message ?? ""}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button disabled={pending} onClick={onCancel}>
          Cancelar
        </Button>
        <Button
          disabled={pending || confirmation === null}
          onClick={() => {
            if (confirmation !== null) {
              onConfirm(confirmation);
            }
          }}
          variant="contained"
        >
          {pending ? "Movendo..." : "Confirmar"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
