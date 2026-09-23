import { Button, MenuItem, Stack, TextField } from "@mui/material";
import type {
  ProjectFilters,
  ProjectStatus,
  Responsible,
  Secretariat,
} from "../../api/kanban";

const STATUS_OPTIONS: ReadonlyArray<{ value: ProjectStatus; label: string }> = [
  { value: "NOT_STARTED", label: "A iniciar" },
  { value: "IN_PROGRESS", label: "Em andamento" },
  { value: "OVERDUE", label: "Atrasado" },
  { value: "COMPLETED", label: "Concluído" },
];

function parseProjectStatus(value: string): ProjectStatus | undefined {
  if (
    value === "NOT_STARTED" ||
    value === "IN_PROGRESS" ||
    value === "OVERDUE" ||
    value === "COMPLETED"
  ) {
    return value;
  }
  return undefined;
}

type ProjectFiltersBarProps = {
  filters: ProjectFilters;
  responsibles: Responsible[];
  secretariats: Secretariat[];
  onChange: (filters: ProjectFilters) => void;
};

export function ProjectFiltersBar({
  filters,
  responsibles,
  secretariats,
  onChange,
}: ProjectFiltersBarProps) {
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
        <TextField
          fullWidth
          label="Buscar projeto"
          value={filters.text ?? ""}
          onChange={(event) =>
            onChange({ ...filters, text: event.target.value || undefined })
          }
        />
        <TextField
          fullWidth
          label="Status"
          select
          value={filters.status ?? ""}
          onChange={(event) => {
            const value = event.target.value;
            onChange({
              ...filters,
              status: parseProjectStatus(value),
            });
          }}
        >
          <MenuItem value="">Todos</MenuItem>
          {STATUS_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          fullWidth
          label="Secretaria"
          select
          value={filters.secretariatId ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              secretariatId: event.target.value || undefined,
            })
          }
        >
          <MenuItem value="">Todas</MenuItem>
          {secretariats.map((secretariat) => (
            <MenuItem key={secretariat.id} value={secretariat.id}>
              {secretariat.name}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          fullWidth
          label="Responsável"
          select
          value={filters.responsibleId ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              responsibleId: event.target.value || undefined,
            })
          }
        >
          <MenuItem value="">Todos</MenuItem>
          {responsibles.map((responsible) => (
            <MenuItem key={responsible.id} value={responsible.id}>
              {responsible.name}
            </MenuItem>
          ))}
        </TextField>
      </Stack>
      <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
        <TextField
          fullWidth
          label="Período previsto de"
          type="date"
          value={filters.plannedFrom ?? ""}
          slotProps={{ inputLabel: { shrink: true } }}
          onChange={(event) =>
            onChange({
              ...filters,
              plannedFrom: event.target.value || undefined,
            })
          }
        />
        <TextField
          fullWidth
          label="Período previsto até"
          type="date"
          value={filters.plannedTo ?? ""}
          slotProps={{ inputLabel: { shrink: true } }}
          onChange={(event) =>
            onChange({
              ...filters,
              plannedTo: event.target.value || undefined,
            })
          }
        />
        <Button onClick={() => onChange({})} variant="text">
          Limpar filtros
        </Button>
      </Stack>
    </Stack>
  );
}
