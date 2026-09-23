// Regra do projeto: código em src/ sem `any`, sem asserção `as` e sem non-null `!`.
// Usa o compilador TypeScript já instalado para inspecionar a AST.
import fs from "node:fs";
import path from "node:path";
import ts from "typescript";

const sourceRoot = path.resolve("src");
const sourceFiles = [];

function collect(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      collect(absolute);
    } else if (entry.name.endsWith(".ts") || entry.name.endsWith(".tsx")) {
      sourceFiles.push(absolute);
    }
  }
}

collect(sourceRoot);

const forbidden = new Set([
  ts.SyntaxKind.AnyKeyword,
  ts.SyntaxKind.AsExpression,
  ts.SyntaxKind.NonNullExpression,
]);
const violations = [];

for (const filename of sourceFiles) {
  const source = ts.createSourceFile(
    filename,
    fs.readFileSync(filename, "utf8"),
    ts.ScriptTarget.Latest,
    true,
    filename.endsWith(".tsx") ? ts.ScriptKind.TSX : ts.ScriptKind.TS,
  );
  const visit = (node) => {
    if (forbidden.has(node.kind)) {
      const position = source.getLineAndCharacterOfPosition(
        node.getStart(source),
      );
      violations.push(
        `${path.relative(sourceRoot, filename)}:${position.line + 1}`,
      );
    }
    ts.forEachChild(node, visit);
  };
  visit(source);
}

if (violations.length > 0) {
  console.error(violations.join("\n"));
  process.exit(1);
}

console.log(`STRICT_TYPES_GREEN ${sourceFiles.length}`);
