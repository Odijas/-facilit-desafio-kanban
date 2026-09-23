# F2-L3 — RISCOS E LACUNAS

Data: 2026-09-22

- `[EXECUTADO PELO USUÁRIO]` Biome e typecheck ficaram GREEN antes do último RED; Vitest ficou 18 pass / 3 fail por vazamento do `MutationFunctionContext` na fronteira React Query → API.
- `[CORRIGIDO]` callbacks de query/mutation agora adaptam explicitamente chamadas da camada API; os três testes existentes permanecem como prova de regressão esperada.
- `[DESCONHECIDO]` Biome 2.5.14, typecheck semântico, Vitest completo, build, backend e Docker após a correção estrutural dependem do novo gate local: este ambiente não possui `node_modules`, não consegue obter `pnpm@12.5.1` do registry npm e também não resolveu `github.com` para baixar o binário oficial do Biome. O gate executa `biome check --write .` antes das verificações.
- `[VERIFICADO]` o drag-and-drop é nativo do navegador; experiência touch/teclado não é equivalente ao mouse e permanece limitação do diferencial.
- `[VERIFICADO]` React Router não está instalado. O roteamento atual foi isolado em `app/navigation.ts`; adicionar dependência sem resolver/validar lockfile foi evitado.
- `[VERIFICADO]` frontend Docker continua usando Vite dev server; entrega estática de produção permanece para o freeze final.
