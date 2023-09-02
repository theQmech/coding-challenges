grammar json;

// Tokens
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
NUMBER: [0-9]+;
CHARACTERS: [A-Za-z]+;
WHITESPACE: [ \r\n\t]+ -> skip;

// Rules
start : element EOF;

element : WHITESPACE value WHITESPACE;

value
    : object
    ;

object
    : '{' WHITESPACE '}'
    | '{' members '}'
    ;

members
    : member
    | member ',' members
    ;

member : WHITESPACE string WHITESPACE ':' element;

string :
    '"' CHARACTERS '"'
    ;

// expression
//    : expression op=('*'|'/') expression # MulDiv
//    | expression op=('+'|'-') expression # AddSub
//    | NUMBER                             # Number
//    ;