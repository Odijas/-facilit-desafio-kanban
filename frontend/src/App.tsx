import {
  Alert,
  AppBar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Stack,
  TextField,
  Toolbar,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState, useSyncExternalStore } from "react";
import {
  AuthApiError,
  type AuthUser,
  getCurrentUser,
  login,
  logout,
} from "./api/auth";
import { getHealthStatus } from "./api/health";

const AUTH_QUERY_KEY = ["auth", "me"];

function subscribeToNavigation(onStoreChange: () => void): () => void {
  window.addEventListener("popstate", onStoreChange);
  return () => window.removeEventListener("popstate", onStoreChange);
}

function usePathname(): string {
  return useSyncExternalStore(
    subscribeToNavigation,
    () => window.location.pathname,
    () => "/",
  );
}

function navigate(pathname: string, replace = false): void {
  if (window.location.pathname === pathname) {
    return;
  }

  if (replace) {
    window.history.replaceState(null, "", pathname);
  } else {
    window.history.pushState(null, "", pathname);
  }
  window.dispatchEvent(new PopStateEvent("popstate"));
}

function LoadingState({ label }: { label: string }) {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2} sx={{ alignItems: "center" }}>
        <CircularProgress aria-label={label} />
        <Typography color="text.secondary">{label}</Typography>
      </Stack>
    </Container>
  );
}

function LoginPage() {
  const queryClient = useQueryClient();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const loginMutation = useMutation({
    mutationFn: ({
      loginEmail,
      loginPassword,
    }: {
      loginEmail: string;
      loginPassword: string;
    }) => login(loginEmail, loginPassword),
    onSuccess: (user) => {
      queryClient.setQueryData<AuthUser>(AUTH_QUERY_KEY, user);
      navigate("/", true);
    },
  });

  const invalidCredentials =
    loginMutation.error instanceof AuthApiError &&
    loginMutation.error.status === 401;

  return (
    <Box
      component="main"
      sx={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        bgcolor: "background.default",
        px: 2,
      }}
    >
      <Container maxWidth="xs">
        <Card variant="outlined">
          <CardContent sx={{ p: 4 }}>
            <Stack
              component="form"
              spacing={3}
              onSubmit={(event) => {
                event.preventDefault();
                loginMutation.mutate({
                  loginEmail: email.trim(),
                  loginPassword: password,
                });
              }}
            >
              <Box>
                <Typography component="h1" variant="h4" sx={{ fontWeight: 700 }}>
                  Facilit Kanban
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Acesse o painel administrativo.
                </Typography>
              </Box>

              {loginMutation.isError && (
                <Alert severity="error">
                  {invalidCredentials
                    ? "E-mail ou senha inválidos."
                    : "Não foi possível realizar o login."}
                </Alert>
              )}

              <TextField
                autoComplete="username"
                label="E-mail"
                name="email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
              <TextField
                autoComplete="current-password"
                label="Senha"
                name="password"
                type="password"
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <Button
                disabled={loginMutation.isPending}
                size="large"
                type="submit"
                variant="contained"
              >
                {loginMutation.isPending ? "Entrando..." : "Entrar"}
              </Button>
            </Stack>
          </CardContent>
        </Card>
      </Container>
    </Box>
  );
}

function Dashboard({ user }: { user: AuthUser }) {
  const queryClient = useQueryClient();
  const healthQuery = useQuery({
    queryKey: ["health"],
    queryFn: getHealthStatus,
    retry: false,
  });
  const logoutMutation = useMutation({
    mutationFn: logout,
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
              Fundação autenticada pronta para receber o quadro Kanban.
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
        </Stack>
      </Container>
    </Box>
  );
}

function ProtectedHome() {
  const authQuery = useQuery({
    queryKey: AUTH_QUERY_KEY,
    queryFn: getCurrentUser,
    retry: false,
    staleTime: Number.POSITIVE_INFINITY,
  });

  const redirectToLogin = useCallback(() => navigate("/login", true), []);

  useEffect(() => {
    if (
      authQuery.error instanceof AuthApiError &&
      authQuery.error.status === 401
    ) {
      redirectToLogin();
    }
  }, [authQuery.error, redirectToLogin]);

  if (authQuery.isPending) {
    return <LoadingState label="Carregando sessão" />;
  }

  if (authQuery.isError) {
    if (
      authQuery.error instanceof AuthApiError &&
      authQuery.error.status === 401
    ) {
      return <LoadingState label="Redirecionando para o login" />;
    }

    return (
      <Container maxWidth="sm" sx={{ py: 10 }}>
        <Alert
          action={
            <Button onClick={() => authQuery.refetch()}>Tentar novamente</Button>
          }
          severity="error"
        >
          Não foi possível validar sua sessão.
        </Alert>
      </Container>
    );
  }

  return <Dashboard user={authQuery.data} />;
}

function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2}>
        <Typography component="h1" variant="h4">
          Página não encontrada
        </Typography>
        <Button onClick={() => navigate("/", true)} variant="contained">
          Voltar ao início
        </Button>
      </Stack>
    </Container>
  );
}

export function App() {
  const pathname = usePathname();

  if (pathname === "/login") {
    return <LoginPage />;
  }

  if (pathname === "/") {
    return <ProtectedHome />;
  }

  return <NotFoundPage />;
}
