import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";
import { useEffect, useState } from "react";
import type { Secretariat, SecretariatInput } from "../../api/kanban";

type SecretariatDialogProps = {
  open: boolean;
  secretariat: Secretariat | null;
  pending: boolean;
  errorMessage: string | null;
  onClose: () => void;
  onSubmit: (input: SecretariatInput) => void;
};

export function SecretariatDialog({
  open,
  secretariat,
  pending,
  errorMessage,
  onClose,
  onSubmit,
}: SecretariatDialogProps) {
  const [name, setName] = useState("");

  useEffect(() => {
    if (open) {
      setName(secretariat?.name ?? "");
    }
  }, [open, secretariat]);

  return (
    <Dialog
      fullWidth
      maxWidth="sm"
      open={open}
      onClose={pending ? undefined : onClose}
    >
      <DialogTitle>
        {secretariat === null ? "Nova secretaria" : "Editar secretaria"}
      </DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2}>
          {errorMessage !== null && (
            <Alert severity="error">{errorMessage}</Alert>
          )}
          <TextField
            autoFocus
            fullWidth
            label="Nome da secretaria"
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button disabled={pending} onClick={onClose}>
          Cancelar
        </Button>
        <Button
          disabled={name.trim().length === 0 || pending}
          onClick={() => onSubmit({ name: name.trim() })}
          variant="contained"
        >
          {pending ? "Salvando..." : "Salvar"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
