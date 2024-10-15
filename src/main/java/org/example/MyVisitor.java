package org.example;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

public class MyVisitor extends gBaseVisitor<Object> {

    // Tabla de símbolos
    Map<String, Symbol> symbolTable = new HashMap<>();
    Stack<Map<String, Symbol>> symbolTableStack = new Stack<>();


    void enterScope() {
        System.out.println("EnterScope");
        if (symbolTableStack.empty()) {
            symbolTableStack.push(symbolTable);
        } else {
            symbolTableStack.push(new HashMap<>(symbolTable)); // Hereda el alcance padre
        }
    }

    void exitScope() {
        System.out.println("ExitScope");
        symbolTable = symbolTableStack.pop(); // Restaura el alcance padre
    }

    /**
     * Clase para representar los símbolos en la tabla de símbolos.
     */
    static class Symbol {
        String name;
        String type;
        Object value;

        Symbol(String name, String type) {
            this.name = name;
            this.type = type;
        }

        Symbol(String name, String type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Symbol{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    /**
     * Verifica si un identificador está definido en el alcance actual.
     * @param id El identificador a verificar.
     * @return true si el identificador está definido, false en caso contrario.
     */
    boolean isDefined(String id) {
        return symbolTableStack.peek().containsKey(id);
    }

    /**
     * Evalúa una condición booleana.
     * @param left El operando izquierdo.
     * @param operator El operador de comparación.
     * @param right El operando derecho.
     * @return El resultado de la comparación.
     */
    Object evalCondition(Object left, String operator, Object right) {
        if (left instanceof Number && right instanceof Number) {
            double leftVal = ((Number) left).doubleValue();
            double rightVal = ((Number) right).doubleValue();
            switch (operator) {
                case "==":
                    return leftVal == rightVal;
                case "!=":
                    return leftVal != rightVal;
                case "<":
                    return leftVal < rightVal;
                case ">":
                    return leftVal > rightVal;
                case "<=":
                    return leftVal <= rightVal;
                case ">=":
                    return leftVal >= rightVal;
                default:
                    return false; // No debería suceder en una gramática correcta
            }
        } else if (left instanceof Boolean && right instanceof Boolean) {
            boolean leftb = ((Boolean) left).booleanValue();
            boolean rightb = ((Boolean) right).booleanValue();
            switch (operator) {
                case "==":
                    return leftb == rightb;
                case "!=":
                    return leftb != rightb;
                default:
                    return false; // No debería suceder
            }
        } else {
            System.err.println("Error de tipos en comparación: " + left + " " + operator + " " + right);
            return false; // O lanzar una excepción
        }
    }

    /**
     * Verifica la compatibilidad de tipos en una asignación.
     * @param id El identificador de la variable.
     * @param value El valor a asignar.
     * @return true si los tipos son compatibles, false en caso contrario.
     */
    Boolean checkAssignmentCompatibility(String id, Object value) {
        if (isDefined(id)) {
            String varType = symbolTableStack.peek().get(id).type;
            if (varType.equals("int") && !(value instanceof Integer)) {
                if (((Number) value).doubleValue() != Math.rint(((Number) value).doubleValue())) {
                    System.err.println("Error de tipo: No se puede asignar un valor no entero a la variable int '" + id + "'");
                    return false;
                } else {
                    return true;
                }
            } else if (varType.equals("double") && !(value instanceof Double || value instanceof Integer)) { // Permite la conversión de int a double
                System.err.println("Error de tipo: No se puede asignar un valor no numérico a la variable double '" + id + "'");
                return false;
            }
        }
        return true;
    }

    @Override
    public Object visitProgram(gParser.ProgramContext ctx) {
        enterScope(); // Entra al alcance global
        for (gParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        exitScope(); // Sale del alcance global
        return null;
    }

    @Override
    public Object visitVariable_declaration(gParser.Variable_declarationContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();

        if (isDefined(id)) {
            System.err.println("Error: Variable '" + id + "' ya declarada.");
        } else {
            if (ctx.math_expression() != null) { // Declaración con inicialización
                Object value = visit(ctx.math_expression());
                if (checkAssignmentCompatibility(id, value)) {
                    symbolTableStack.peek().put(id, new Symbol(id, type, value));
                    System.out.println("Declaración de variable: " + symbolTableStack.peek().get(id));
                }
            } else { // Declaración sin inicialización
                symbolTableStack.peek().put(id, new Symbol(id, type));
                System.out.println("Declaración de variable: " + symbolTableStack.peek().get(id));
            }
        }
        return null;
    }

    @Override
    public Object visitVariable_update(gParser.Variable_updateContext ctx) {
        String id = ctx.ID().getText();

        // Obtiene el símbolo
        Symbol symbol = symbolTableStack.peek().get(id);

        if (symbol == null) {
            System.err.println("Error: Variable '" + id + "' no declarada.");
            return null;
        }

        Number currentValue = (Number) symbol.value;

        // Determina la operación
        String operator = ctx.getChild(1).getText();

        switch (operator) {
            case "++":
                symbol.value = currentValue.doubleValue() + 1;
                break;

            case "--":
                symbol.value = currentValue.doubleValue() - 1;
                break;

            case "+=":
                Object exprValueAdd = visit(ctx.math_expression());
                if (exprValueAdd instanceof Number) {
                    symbol.value = currentValue.doubleValue() + ((Number) exprValueAdd).doubleValue();
                } else {
                    System.err.println("Error: La expresión en la asignación += debe ser numérica.");
                    return null;
                }
                break;

            case "-=":
                Object exprValueSubtract = visit(ctx.math_expression());
                if (exprValueSubtract instanceof Number) {
                    symbol.value = currentValue.doubleValue() - ((Number) exprValueSubtract).doubleValue();
                } else {
                    System.err.println("Error: La expresión en la asignación -= debe ser numérica.");
                    return null;
                }
                break;

            default:
                System.err.println("Error: Operador '" + operator + "' no soportado.");
                return null;
        }

        // Imprime la variable actualizada
        System.out.println("Actualización de variable: " + symbol);
        return null;
    }

    @Override
    public Object visitVariable_assign(gParser.Variable_assignContext ctx) {
        String id = ctx.ID().getText();
        Object value = visit(ctx.math_expression()); // Evalúa la expresión del lado derecho

        if (!isDefined(id)) {
            System.err.println("Error: Variable '" + id + "' no declarada.");
        } else {
            if (checkAssignmentCompatibility(id, value)) {
                symbolTableStack.peek().get(id).value = value;
                System.out.println("Asignación de variable: " + symbolTableStack.peek().get(id));
            }
        }
        return null;
    }

    @Override
    public Object visitIf_statement(gParser.If_statementContext ctx) {
        Boolean condition = (Boolean) visit(ctx.logical_operation());
        if (condition) {
            visit(ctx.program(0)); // Ejecuta el bloque 'if'
        } else if (ctx.program().size() > 1) { // Verifica si hay un bloque 'else'
            visit(ctx.program(1)); // Ejecuta el bloque 'else'
        }
        return null; // Las sentencias if no tienen valor de retorno
    }

    @Override
    public Object visitDo_while(gParser.Do_whileContext ctx) {
        do {
            visit(ctx.program()); // Ejecuta el cuerpo del bucle
        } while ((Boolean) visit(ctx.logical_operation()));
        return null; // Los bucles do-while no tienen valor de retorno
    }

    @Override
    public Object visitFor_loop(gParser.For_loopContext ctx) {
        enterScope();
        visit(ctx.variable_assign()); // Inicialización

        while ((Boolean) visit(ctx.logical_operation())) {
            visit(ctx.program()); // Ejecuta el cuerpo del bucle
            visit(ctx.variable_update()); // Expresión de actualización
        }
        exitScope();

        return null; // Los bucles for no tienen valor de retorno
    }

    @Override
    public Object visitMath_expression(gParser.Math_expressionContext ctx) {
        Double value = (Double) visit(ctx.term(0));
        for (int i = 1; i < ctx.term().size(); i++) {
            Double termValue = (Double) visit(ctx.term(i));
            if (ctx.getChild(2 * i - 1).getText().equals("+")) {
                value += termValue;
            } else { // Debe ser '-'
                value -= termValue;
            }
        }
        return value;
    }

    @Override
    public Object visitTerm(gParser.TermContext ctx) {
        Double value = (Double) visit(ctx.power_expr(0));
        for (int i = 1; i < ctx.power_expr().size(); i++) {
            Double factorValue = (Double) visit(ctx.power_expr(i));
            if (ctx.getChild(2 * i - 1).getText().equals("*")) {
                value *= factorValue;
            } else { // Debe ser '/'
                value /= factorValue;
            }
        }
        return value;
    }

    @Override
    public Object visitPower_expr(gParser.Power_exprContext ctx) {
        double value = ((Number) visit(ctx.factor(0))).doubleValue();
        if (ctx.factor().size() > 1) { // Si hay una operación de potencia
            double exponent = ((Number) visit(ctx.factor(1))).doubleValue();
            value = Math.pow(value, exponent);
        }
        return value;
    }

    @Override
    public Object visitFactor(gParser.FactorContext ctx) {
        if (ctx.ID() != null) { // El factor es un ID (variable)
            String id = ctx.ID().getText();
            if (!isDefined(id)) {
                System.err.println("Error: Variable '" + id + "' no declarada.");
                return 0.0;
            } else {
                Object val = symbolTableStack.peek().get(id).value;
                if (val == null) {
                    System.err.println("Error: Variable '" + id + "' no inicializada.");
                    return 0.0;
                } else if (val instanceof Double) {
                    return (Double) val;
                } else if (val instanceof Integer) {
                    return ((Integer) val).doubleValue();
                } else {
                    System.err.println("Error: Variable '" + id + "' no es una variable numérica");
                    return 0.0;
                }
            }
        } else if (ctx.number() != null) { // El factor es un número
            return visit(ctx.number());
        } else { // El factor es una expresión entre paréntesis
            return visit(ctx.math_expression());
        }
    }

    @Override
    public Object visitNumber(gParser.NumberContext ctx) {
        if (ctx.INT() != null) {
            if (ctx.getChild(0).getText().equals("-")) {
                return -Integer.parseInt(ctx.INT().getText());
            } else {
                return Integer.parseInt(ctx.INT().getText());
            }
        } else { // Es double
            if (ctx.getChild(0).getText().equals("-")) {
                return -Double.parseDouble(ctx.DOUBLE().getText());
            } else {
                return Double.parseDouble(ctx.DOUBLE().getText());
            }
        }
    }

    @Override
    public Object visitType(gParser.TypeContext ctx) {
        return ctx.getText(); // Devuelve el tipo como una cadena ("int" o "double")
    }

    @Override
    public Object visitLogical_operation(gParser.Logical_operationContext ctx) {
        Boolean value = (Boolean) visit(ctx.logical_term(0));
        for (int i = 1; i < ctx.logical_term().size(); i++) {
            Boolean termValue = (Boolean) visit(ctx.logical_term(i));
            value = value || termValue; // Aplica el operador ||
        }
        return value;
    }

    @Override
    public Object visitLogical_term(gParser.Logical_termContext ctx) {
        Boolean value = (Boolean) visit(ctx.logical_factor(0));
        for (int i = 1; i < ctx.logical_factor().size(); i++) {
            Boolean factorValue = (Boolean) visit(ctx.logical_factor(i));
            value = value && factorValue; // Aplica el operador &&
        }
        return value;
    }

    @Override
    public Object visitLogical_factor(gParser.Logical_factorContext ctx) {
        if (ctx.boolean_() != null) {
            return visit(ctx.boolean_());
        } else if (ctx.logical_factor() != null) { // Negación (!)
            Boolean value = (Boolean) visit(ctx.logical_factor());
            return !value;
        } else { // Operación lógica entre paréntesis
            return visit(ctx.logical_operation());
        }
    }

    @Override
    public Object visitBoolean(gParser.BooleanContext ctx) {
        if (ctx.math_expression() != null) { // Comparación entre expresiones matemáticas
            Double left = (Double) visit(ctx.math_expression(0));
            Double right = (Double) visit(ctx.math_expression(1));
            String operator = ctx.comparison_operator().getText();
            return evalCondition(left, operator, right);
        } else if (ctx.TRUE() != null) {
            return true;
        } else { // Debe ser FALSE
            return false;
        }
    }
}