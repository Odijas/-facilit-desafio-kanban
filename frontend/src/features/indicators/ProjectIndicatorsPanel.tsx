import {
  Alert,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getProjectIndicators, type ProjectStatus } from "../../api/kanban";
import { PROJECT_INDICATORS_QUERY_KEY } from "../kanban/queryKeys";

const STATUS_LABELS: Record<ProjectStatus, string> = {
  NOT_STARTED: "A iniciar",
  IN_PROGRESS: "Em andamento",
  OVERDUE: "Atrasado",
  COMPLETED: "Concluído",
};

export function ProjectIndicatorsPanel() {
  const indicatorsQuery = useQuery({
    queryKey: PROJECT_INDICATORS_QUERY_KEY,
    queryFn: () => getProjectIndicators(),
    retry: false,
  });

  if (indicatorsQuery.isPending) {
    return (
      <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
        <CircularProgress aria-label="Carregando indicadores" size={20} />
        <Typography color="text.secondary">Carregando indicadores</Typography>
      </Stack>
    );
  }

  if (indicatorsQuery.isError) {
    return (
      <Alert severity="error">Não foi possível carregar os indicadores.</Alert>
    );
  }

  return (
    <Stack spacing={2}>
      <Box>
        <Typography component="h2" variant="h5" sx={{ fontWeight: 700 }}>
          Indicadores
        </Typography>
        <Typography color="text.secondary">
          Visão consolidada da carteira de projetos.
        </Typography>
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: {
            xs: "1fr",
            sm: "repeat(2, 1fr)",
            lg: "repeat(3, 1fr)",
          },
        }}
      >
        <IndicatorCard
          label="Total de projetos"
          value={indicatorsQuery.data.totalProjects}
        />
        <IndicatorCard
          label="Projetos com atraso"
          value={indicatorsQuery.data.delayedProjects}
        />
        {indicatorsQuery.data.byStatus.map((indicator) => (
          <IndicatorCard
            key={indicator.status}
            label={STATUS_LABELS[indicator.status]}
            value={indicator.projectCount}
            supportingText={`Média de atraso: ${indicator.averageDelayDays.toFixed(1)} dia(s)`}
          />
        ))}
      </Box>
    </Stack>
  );
}

type IndicatorCardProps = {
  label: string;
  value: number;
  supportingText?: string;
};

function IndicatorCard({ label, value, supportingText }: IndicatorCardProps) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography color="text.secondary" variant="body2">
          {label}
        </Typography>
        <Typography variant="h4" sx={{ fontWeight: 700, mt: 0.5 }}>
          {value}
        </Typography>
        {supportingText !== undefined && (
          <Typography color="text.secondary" variant="body2" sx={{ mt: 0.5 }}>
            {supportingText}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}
