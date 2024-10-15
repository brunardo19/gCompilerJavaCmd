grammar g;

program:
   statement*;

statement:
   variable_declaration ';'
   | variable_assign ';'
   | variable_update ';'
   | if_statement
   | while_loop
   | do_while
   | for_loop;

if_statement:
   IF '(' logical_operation ')' '{' program '}' (ELSE '{' program '}')?;

while_loop:
   WHILE '(' logical_operation ')' '{' program '}';

do_while:
   DO '{' program '}' WHILE '(' logical_operation ')' ';';

for_loop:
   FOR '(' variable_assign ';' logical_operation ';' variable_update ')' '{' program '}';

variable_declaration:
   type ID
   | type ID '=' math_expression;

variable_update:
   ID ('++' | '--')
   | ID ('+=' | '-=') math_expression;

variable_assign:
   ID '=' math_expression;

math_expression
   : term (('+' | '-') term)*;

term:
   power_expr (('*' | '/') power_expr)*;

power_expr:
   factor ('^' factor) | factor;

factor:
   ID
   | number
   | '(' math_expression ')';

number:
   ('+'|'-')? INT
   |('+'|'-')? DOUBLE;

type:
   INT_TYPE
   | DOUBLE_TYPE;

logical_operation:
   logical_term ( '||' logical_term)*;

logical_term:
   logical_factor ( '&&' logical_factor)*;

logical_factor:
   boolean
   | '!' logical_factor
   | '(' logical_operation ')';

boolean:
   math_expression comparison_operator math_expression
   | TRUE
   | FALSE
   | ID;

comparison_operator:
   '==' | '!=' | '<' | '>' | '<=' | '>=';

// Reserved keywords
IF         : 'if';
ELSE       : 'else';
WHILE      : 'while';
DO         : 'do';
FOR        : 'for';
TRUE       : 'true';
FALSE      : 'false';
INT_TYPE   : 'int';
DOUBLE_TYPE: 'double';


ID: [a-zA-Z] [a-zA-Z0-9_$]* ;


INT: ( [1-9] [0-9]* | '0' );


DOUBLE: ( [1-9] [0-9]* | '0' ) '.' [0-9]+;


MATH_OP: ('+'|'-'|'*'|'/'|'%'|'^');
LOG_OP: ('&&'|'||'|'!');
COMP_OP: ('<'|'>'|'<='|'>='|'=='|'!=');
ASSIGN: '=';


PUNCT: ('('|')'|'{'|'}'|';');


WS: [ \t\r\n]+ -> skip;