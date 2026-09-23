# F2-L2 — EXECUÇÃO

Data: 2026-09-22

## Baseline promovida

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] F2-L1: gate isolado terminou com `Resultado: exit code 0`; health permaneceu `UP`.
[CONCLUSÃO] F2-L1 é GREEN e constitui a baseline do F2-L2.
```

## Executado neste ambiente

```text
[EXECUTADO] extração integral do candidato F2-L1 e leitura dos arquivos diretamente envolvidos.
[EXECUTADO] mapeamento dos consumidores dos endpoints de autenticação.
[EXECUTADO] implementação do cliente de autenticação, fluxo de login/logout, proteção de rota, loading/error e testes unitários.
[EXECUTADO] scan estático dos arquivos novos: nenhum `any`, type assertion `as`, `localStorage` ou `sessionStorage` introduzido.
[EXECUTADO] `tsc` disponível neste ambiente foi usado apenas como parser parcial; como `node_modules` não está materializado, os erros observados foram de módulos/tipos externos ausentes. Isso NÃO é reportado como typecheck GREEN.
```

## Não executado neste ambiente

`[DESCONHECIDO]` pnpm/Biome/Vitest/Vite com as dependências reais, bem como Docker/browser contra o backend, aguardam `VERIFICACAO-USUARIO.md`.

## Correção após gate RED do usuário

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] `pnpm lint` inicialmente falhou por formatação não aplicada; após formatação, `pnpm test` expôs 4 falhas e `pnpm build`/typecheck expôs 6 erros MUI v9.
[VERIFICADO] Material UI 9 removeu system props diretas de Stack/Typography; `alignItems` e `fontWeight` foram movidos para `sx`.
[VERIFICADO] `@types/react` 19.3.0 marca `FormEvent` como deprecated; o submit agora usa inferência de tipo no `onSubmit`, sem importar o tipo obsoleto.
[VERIFICADO] Vitest usa `globals: false` por padrão; Testing Library só faz auto-cleanup quando `afterEach` global existe. O setup agora registra `cleanup()` explicitamente com `afterEach` do Vitest.
[VERIFICADO] queries de label dos campos obrigatórios passaram a usar matcher regex, compatível com o texto acessível que inclui o marcador de obrigatoriedade.
[VERIFICADO] testes de cookie CSRF não atribuem mais `document.cookie`; o getter é controlado por `vi.spyOn`, eliminando os warnings `noDocumentCookie`.
[CORREÇÃO] o gate agora executa `pnpm format` antes de lint/typecheck/test/build, conforme solicitado pelo usuário.
[DESCONHECIDO] GREEN final depende de nova execução local do gate completo.
```
