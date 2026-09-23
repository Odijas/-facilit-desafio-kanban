import { usePathname } from "./app/navigation";
import { LoginPage } from "./features/auth/LoginPage";
import { ProtectedHome } from "./features/auth/ProtectedHome";
import { NotFoundPage } from "./pages/NotFoundPage";

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
