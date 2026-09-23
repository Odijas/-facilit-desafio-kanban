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
import type { ResponsibleInput } from "../../api/kanban";

type ResponsibleDialogProps = {
  open: boolean;
  pending: boolean;
  errorMessage: string | null;
  onClose: () => void;
  onSubmit: (input: ResponsibleInput) => void;
};

export function ResponsibleDialog({
  open,
  pending,
  errorMessage,
  onClose,
  onSubmit,
}: ResponsibleDialogProps) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [position, setPosition] = useState("");

  useEffect(() => {
    if (open) {
      setName("");
      setEmail("");
      setPosition("");
    }
  }, [open]);

  const canSubmit =
    name.trim().length > 0 &&
    email.trim().length > 0 &&
    position.trim().length > 0 &&
    !pending;

  return (
    <Dialog
      fullWidth
      maxWidth="sm"
      open={open}
      onClose={pending ? undefined : onClose}
    >
      <DialogTitle>Novo responsável</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2}>
          {errorMessage !== null && (
            <Alert severity="error">{errorMessage}</Alert>
          )}
          <TextField
            autoFocus
            fullWidth
            label="Nome"
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
          <TextField
            fullWidth
            label="E-mail"
            required
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <TextField
            fullWidth
            label="Cargo"
            required
            value={position}
            onChange={(event) => setPosition(event.target.value)}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button disabled={pending} onClick={onClose}>
          Cancelar
        </Button>
        <Button
          disabled={!canSubmit}
          onClick={() =>
            onSubmit({
              name: name.trim(),
              email: email.trim(),
              position: position.trim(),
              secretariatId: null,
            })
          }
          variant="contained"
        >
          {pending ? "Salvando..." : "Salvar"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
