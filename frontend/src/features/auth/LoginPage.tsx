import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { AuthApiError, type AuthUser, login } from "../../api/auth";
import { navigate } from "../../app/navigation";
import { AUTH_QUERY_KEY } from "./authQuery";

type LoginVariables = {
  email: string;
  password: string;
};

export function LoginPage() {
  const queryClient = useQueryClient();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const loginMutation = useMutation({
    mutationFn: ({
      email: loginEmail,
      password: loginPassword,
    }: LoginVariables) => login(loginEmail, loginPassword),
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
                  email: email.trim(),
                  password,
                });
              }}
            >
              <Box>
                <Typography
                  component="h1"
                  variant="h4"
                  sx={{ fontWeight: 700 }}
                >
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
