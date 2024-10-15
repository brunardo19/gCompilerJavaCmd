package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        // Leer el archivo de texto
        String filePath = "src/main/java/org/example/test.txt";
        String fileContent = "";
        try {
            fileContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return; // Salir si el archivo no se puede leer
        }

        // ----------- ANALIZADOR LÉXICO -----------

        CharStream input = CharStreams.fromString(fileContent);
        gLexer lexer = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        gParser parser = new gParser(tokens);

        // ----------- ANALIZADOR SINTÁCTICO -----------
        // Sobreescribir el listener de errores
        parser.removeErrorListeners();

        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                System.err.println("Error sintáctico en la línea " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        ParseTree tree = parser.program(); // Invocar la regla inicial y guardar el arbol en tree

        MyVisitor visitor = new MyVisitor(); //Crear el visitor para interpretar
        visitor.visit(tree);

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("El analizador sintáctico encontró: " + parser.getNumberOfSyntaxErrors() + " errores");
        } else {
            System.out.println();// Mostrar tabla de símbolos global
            System.out.println("Tabla de Símbolos Global:");
            for (Map.Entry<String, MyVisitor.Symbol> entry : visitor.symbolTable.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("No hay errores en el analizador sintáctico");
        }
    }
}