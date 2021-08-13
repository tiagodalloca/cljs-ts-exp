import * as ts from 'typescript';

let fDeclaration =

    ts.factory.createFunctionDeclaration(
        /*decorators*/ undefined,
        /*modifiers*/[ts.factory.createToken(ts.SyntaxKind.ExportKeyword)],
        /*asteriskToken*/ undefined,
            "factorial",
        /*typeParameters*/ undefined,
            [ts.factory.createParameterDeclaration(
            /*decorators*/ undefined,
            /*modifiers*/ undefined,
            /*dotDotDotToken*/ undefined,
            /*parameterName*/ "n",
            /*questionMark*/ undefined,
            /*typeNode*/ ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword),
            )],
        /*returnType*/ ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword),
        /*body -> none bc it's just the type declaration */ undefined
    )

const resultFile = ts.createSourceFile("out/exp1.d.ts", "", ts.ScriptTarget.Latest, /*setParentNodes*/ false, ts.ScriptKind.TS);
const printer = ts.createPrinter({ newLine: ts.NewLineKind.LineFeed });
const result = printer.printNode(ts.EmitHint.Unspecified, fDeclaration, resultFile);
console.log(result);