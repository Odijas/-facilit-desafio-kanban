import {
  Alert,
  AppBar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Container,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type AuthUser, logout } from "../../api/auth";
import { getHealthStatus } from "../../api/health";
import { navigate } from "../../app/navigation";
import { AUTH_QUERY_KEY } from "../auth/authQuery";
import { ProjectIndicatorsPanel } from "../indicators/ProjectIndicatorsPanel";
import { KanbanBoard } from "../kanban/KanbanBoard";
import { SecretariatPanel } from "../secretariats/SecretariatPanel";

type DashboardPageProps = {
  user: AuthUser;
};

export function DashboardPage({ user }: DashboardPageProps) {
  const queryClient = useQueryClient();
  const healthQuery = useQuery({
    queryKey: ["health"],
    queryFn: () => getHealthStatus(),
    retry: false,
  });
  const logoutMutation = useMutation({
    mutationFn: () => logout(),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: AUTH_QUERY_KEY });
      navigate("/login", true);
    },
  });

  return (
    <Box
      component="main"
      sx={{ minHeight: "100vh", bgcolor: "background.default" }}
    >
      <AppBar color="inherit" elevation={0} position="static">
        <Toolbar sx={{ borderBottom: 1, borderColor: "divider", gap: 2 }}>
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
            Facilit Kanban
          </Typography>
          <Typography color="text.secondary" variant="body2">
            {user.email}
          </Typography>
          <Button
            disabled={logoutMutation.isPending}
            onClick={() => logoutMutation.mutate()}
            variant="outlined"
          >
            {logoutMutation.isPending ? "Saindo..." : "Sair"}
          </Button>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 6 }}>
        <Stack spacing={3}>
          {logoutMutation.isError && (
            <Alert severity="error">Não foi possível encerrar a sessão.</Alert>
          )}
          <Box>
            <Typography component="h1" variant="h4" sx={{ fontWeight: 700 }}>
              Painel administrativo
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              Gerencie projetos e responsáveis no quadro Kanban.
            </Typography>
          </Box>

          <Card variant="outlined">
            <CardContent>
              <Stack
                direction={{ xs: "column", sm: "row" }}
                spacing={2}
                sx={{ alignItems: { sm: "center" } }}
              >
                <Typography sx={{ fontWeight: 600 }}>API</Typography>
                {healthQuery.isPending && <Chip label="Verificando backend" />}
                {healthQuery.isError && (
                  <Alert severity="error">Backend indisponível</Alert>
                )}
                {healthQuery.isSuccess && (
                  <Chip
                    color="success"
                    label={`Backend ${healthQuery.data.status}`}
                  />
                )}
              </Stack>
            </CardContent>
          </Card>

          <ProjectIndicatorsPanel />
          <SecretariatPanel />
          <KanbanBoard />
        </Stack>
      </Container>
    </Box>
  );
}
