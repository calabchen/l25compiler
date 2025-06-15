# L25 Compiler JAVA Edition

## 引言
hello, this is a program for conducting a simple compiler like PL0.

the requirement is to complete L25 grammar.

## Grammar
>  L25 Language Grammar with EBNF description
> 
>  **Support One-dimensional static array and structure**
```text
<programming> = "program" <ident> "{" { <struct_def> } { <func_def> } "main" "{" <stmt_list> "}" "}"

<struct_def> = <struct> <ident> "{" [ <member_list> ] "}"

<member_list> = <ident> { "," <ident> }

<func_def> = "func" <ident> "(" [ <param_list> ] ")" "{" <stmt_list> "return" <expr> ";" "}"

<param_list> = <ident> { "," <ident> }

<stmt_list> = <stmt> ";" { <stmt> ";" }

<stmt>  = <declare_stmt>
        | <assign_stmt>
        | <if_stmt>
        | <while_stmt>
        | <input_stmt>
        | <output_stmt>
        | <func_call>

<declare_stmt>  = "let" <ident> (
                    | "=" ( <expr> | <struct_init_expr>)
                    | ":" <array_suffix> [ "=" <array_init_expr> ]
                    | ":" <struct_suffix> [ "=" <struct_init_expr> ]
                    )
                
<array_suffix> = "[" <array_size> "]"

<array_size> = <number>

<array_init_expr> = "[" [ <expr>  { ","  <expr> } ] "]"

<struct_suffix> = <ident>
 
<struct_init_expr> = "{" [ assign_list ] "}"

assign_list = ident "=" expr { "," ident "=" expr }

<assign_stmt> = <factor> "=" <expr>

<if_stmt> = "if" "(" <bool_expr> ")" "{" <stmt_list> "}" [ "else" "{" <stmt_list> "}" ]

<while_stmt> = "while" "(" <bool_expr> ")" "{" <stmt_list> "}"

<func_call> = <ident> "(" [ <arg_list> ] ")"

<arg_list> = <expr> { "," <expr> }

<input_stmt> = "input" "(" <ident> { "," <ident> } ")"

<output_stmt> = "output" "(" <expr> { "," <expr> } ")"

<bool_expr> = <expr> ("==" | "!=" | "<" | "<=" | ">" | ">=") <expr>

<expr> = [ "+" | "-" ] <term> { ("+" | "-") <term> }

<term> = <factor> { ("*" | "/") <factor> }

<factor> = <ident>                 
         | <ident> "[" <expr> "]"   
         | <ident> "." <ident>      
         | <number>                
         | "(" <expr> ")"           
         | <func_call>              

<ident> = <letter> { <letter> | <digit> }

<number> = <digit> { <digit> }

<letter> = "a" | "b" | ... | "z" | "A" | "B" | ... | "Z"

<digit> = "0" | "1" | ... | "9"

```
