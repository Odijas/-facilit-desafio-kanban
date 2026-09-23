import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import { useEffect, useState } from "react";
import type { ResponsibleInput, Secretariat } from "../../api/kanban";

type ResponsibleDialogProps = {
  open: boolean;
  pending: boolean;
  errorMessage: string | null;
  secretariats: Secretariat[];
  onClose: () => void;
  onSubmit: (input: ResponsibleInput) => void;
};

export function ResponsibleDialog({
  open,
  pending,
  errorMessage,
  secretariats,
  onClose,
  onSubmit,
}: ResponsibleDialogProps) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [position, setPosition] = useState("");
  const [secretariatId, setSecretariatId] = useState("");

  useEffect(() => {
    if (open) {
      setName("");
      setEmail("");
      setPosition("");
      setSecretariatId("");
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
          <TextField
            fullWidth
            label="Secretaria"
            select
            value={secretariatId}
            onChange={(event) => setSecretariatId(event.target.value)}
          >
            <MenuItem value="">Sem secretaria</MenuItem>
            {secretariats.map((secretariat) => (
              <MenuItem key={secretariat.id} value={secretariat.id}>
                {secretariat.name}
              </MenuItem>
            ))}
          </TextField>
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
              secretariatId: secretariatId === "" ? null : secretariatId,
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
