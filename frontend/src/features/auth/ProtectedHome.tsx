import { Alert, Button, Container } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useCallback, useEffect } from "react";
import { AuthApiError, getCurrentUser } from "../../api/auth";
import { navigate } from "../../app/navigation";
import { LoadingState } from "../../components/LoadingState";
import { DashboardPage } from "../dashboard/DashboardPage";
import { AUTH_QUERY_KEY } from "./authQuery";

export function ProtectedHome() {
  const authQuery = useQuery({
    queryKey: AUTH_QUERY_KEY,
    queryFn: () => getCurrentUser(),
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
            <Button onClick={() => authQuery.refetch()}>
              Tentar novamente
            </Button>
          }
          severity="error"
        >
          Não foi possível validar sua sessão.
        </Alert>
      </Container>
    );
  }

  return <DashboardPage user={authQuery.data} />;
}
