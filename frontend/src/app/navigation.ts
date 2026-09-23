import { useSyncExternalStore } from "react";

function subscribeToNavigation(onStoreChange: () => void): () => void {
  window.addEventListener("popstate", onStoreChange);
  return () => window.removeEventListener("popstate", onStoreChange);
}

export function usePathname(): string {
  return useSyncExternalStore(
    subscribeToNavigation,
    () => window.location.pathname,
    () => "/",
  );
}

export function navigate(pathname: string, replace = false): void {
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
