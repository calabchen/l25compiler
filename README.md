# L25 Compiler JAVA Edition

## 引言
hello, this is a program for conducting a simple compiler like PL0.

the requirement is to complete L25 grammar.

## Grammar
>  L25 Language Grammar with EBNF description
> 
>  **Support One-dimensional static array and structure**
```text
<programming> = "program" <ident> "{" { const_def } { <struct_def> } { <func_def> } "main" "{" <stmt_list> "}" "}"

<const_def> = "const" <ident> "=" <number> ";"

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
                    | ":" <array_type_suffix> [ "=" <array_init_expr> ]
                    )
                
<array_type_suffix> = "[" <array_size> "]"

<array_size> = <expr>

<array_init_expr> = "[" [ ( <expr> | <struct_init_expr> ) { "," ( <expr> | <struct_init_expr> ) } ] "]" .

struct_init_expr = ident "{" [ assign_list] "}" .

assign_list = ident "=" expr { "," ident "=" expr } .

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

<factor> = <ident>                  (* 简单变量，例如 x, y, z *)
         | <ident> "[" <expr> "]"   (* 数组元素访问，索引可以是表达式。例如 arr2[i], p2.node[j] *)
         | <ident> "." <ident>      (* 结构体成员访问，例如 p1.val, p2.val *)
         | <number>                 (* 数字字面量，例如 0, 1, 2 *)
         | "(" <expr> ")"           (* 带括号的表达式 *)
         | <func_call>              (* 函数调用，例如 add(a, b) *)

<ident> = <letter> { <letter> | <digit> }

<number> = <digit> { <digit> }

<letter> = "a" | "b" | ... | "z" | "A" | "B" | ... | "Z"

<digit> = "0" | "1" | ... | "9"

```
