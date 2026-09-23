import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  FormGroup,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import type { Project, ProjectInput, Responsible } from "../../api/kanban";

type ProjectDialogProps = {
  open: boolean;
  project: Project | null;
  responsibles: Responsible[];
  pending: boolean;
  errorMessage: string | null;
  requiredResponsibleId: string | null;
  onClose: () => void;
  onSubmit: (input: ProjectInput) => void;
};

export function ProjectDialog({
  open,
  project,
  responsibles,
  pending,
  errorMessage,
  requiredResponsibleId,
  onClose,
  onSubmit,
}: ProjectDialogProps) {
  const [name, setName] = useState("");
  const [responsibleIds, setResponsibleIds] = useState<string[]>([]);
  const [plannedStart, setPlannedStart] = useState("");
  const [plannedEnd, setPlannedEnd] = useState("");

  useEffect(() => {
    if (!open) {
      return;
    }

    setName(project?.name ?? "");
    const initialIds = project?.responsibleIds ?? [];
    setResponsibleIds(
      requiredResponsibleId !== null &&
        !initialIds.includes(requiredResponsibleId)
        ? [...initialIds, requiredResponsibleId]
        : initialIds,
    );
    setPlannedStart(project?.plannedStart ?? "");
    setPlannedEnd(project?.plannedEnd ?? "");
  }, [open, project, requiredResponsibleId]);

  const canSubmit =
    name.trim().length > 0 && responsibleIds.length > 0 && !pending;

  return (
    <Dialog
      fullWidth
      maxWidth="sm"
      open={open}
      onClose={pending ? undefined : onClose}
    >
      <DialogTitle>
        {project === null ? "Novo projeto" : "Editar projeto"}
      </DialogTitle>
      <DialogContent dividers>
        <Stack spacing={3}>
          {errorMessage !== null && (
            <Alert severity="error">{errorMessage}</Alert>
          )}
          <TextField
            autoFocus
            fullWidth
            label="Nome do projeto"
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField
              fullWidth
              label="Início previsto"
              type="date"
              value={plannedStart}
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={(event) => setPlannedStart(event.target.value)}
            />
            <TextField
              fullWidth
              label="Término previsto"
              type="date"
              value={plannedEnd}
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={(event) => setPlannedEnd(event.target.value)}
            />
          </Stack>
          <Box>
            <Typography sx={{ mb: 1, fontWeight: 600 }}>
              Responsáveis
            </Typography>
            {responsibles.length === 0 ? (
              <Alert severity="info">
                Cadastre um responsável antes de salvar o projeto.
              </Alert>
            ) : (
              <FormGroup>
                {responsibles.map((responsible) => (
                  <FormControlLabel
                    key={responsible.id}
                    control={
                      <Checkbox
                        checked={responsibleIds.includes(responsible.id)}
                        disabled={responsible.id === requiredResponsibleId}
                        onChange={(event) => {
                          setResponsibleIds((current) =>
                            event.target.checked
                              ? [...current, responsible.id]
                              : current.filter((id) => id !== responsible.id),
                          );
                        }}
                      />
                    }
                    label={`${responsible.name} — ${responsible.position}`}
                  />
                ))}
              </FormGroup>
            )}
          </Box>
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
              responsibleIds,
              plannedStart: plannedStart === "" ? null : plannedStart,
              plannedEnd: plannedEnd === "" ? null : plannedEnd,
              actualStart: project?.actualStart ?? null,
              actualEnd: project?.actualEnd ?? null,
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
