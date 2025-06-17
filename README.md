# L25 Compiler - Java Edition

## 引言

Hello, this is a program for conducting a simple compiler like PL0.

The requirement is to complete the **L25 extension grammar** based on the provided EBNF and implement a full compiler in Java.  
> 📌 本次作业我是 **第三组扩展：支持一维静态数组和结构体（struct）的声明、赋值和访问使用**，完整的扩展文法定义请见 [Extension Grammar](#extension-grammar)，也可以查看语法图文档：[点击查看语法图](doc/L25ExGrpha.md)

---

## 2025年《编译原理与实践》大作业要求

以下是使用EBNF描述的**基本L25语言**，所使用的元符号含义同教材P13。所有终结符用双引号括起表示。

### L25语言语法（EBNF）

```ebnf
<program> = "program" <ident> "{" { <func_def> } "main" "{" <stmt_list> "}" "}"

<func_def> = "func" <ident> "(" [ <param_list> ] ")" "{" <stmt_list> "return" <expr> ";" "}"

<param_list> = <ident> { "," <ident> }

<stmt_list> = <stmt> ";" { <stmt> ";" }

<stmt> = <declare_stmt> | <assign_stmt> | <if_stmt> | <while_stmt> | <input_stmt> | <output_stmt> | <func_call>

<declare_stmt> = "let" <ident> [ "=" <expr> ]

<assign_stmt> = <ident> "=" <expr>

<if_stmt> = "if" "(" <bool_expr> ")" "{" <stmt_list> "}" [ "else" "{" <stmt_list> "}" ]

<while_stmt> = "while" "(" <bool_expr> ")" "{" <stmt_list> "}"

<func_call> = <ident> "(" [ <arg_list> ] ")"

<arg_list> = <expr> { "," <expr> }

<input_stmt> = "input" "(" <ident> { "," <ident> } ")"

<output_stmt> = "output" "(" <expr> { "," <expr> } ")"

<bool_expr> = <expr> ("==" | "!=" | "<" | "<=" | ">" | ">=") <expr>

<expr> = [ "+" | "-" ] <term> { ("+" | "-") <term> }

<term> = <factor> { ("*" | "/") <factor> }

<factor> = <ident> | <number> | "(" <expr> ")" | <func_call>

<ident> = <letter> { <letter> | <digit> }

<number> = <digit> { <digit> }

<letter> = "a" | "b" | ... | "z" | "A" | "B" | ... | "Z"

<digit> = "0" | "1" | ... | "9"
```

### 示例代码

```txt
program MyApp {
    func add(a, b) {
        let sum = a + b;
        return sum;
    }
    func square(x) {
        let result = x * x;
        return result;
    }
    main {
        let x = 5;
        let y = add(x, 10);
        let z = square(y);
        if (z > 50) {
            output(z);
        } else {
            output(0);
        };
    }
}
```

### 注意事项

1. 函数体中必须包含 `return` 语句。
2. `main` 或普通语句块中不能使用 `return`。
3. 每个 `stmt_list` 至少包含一条语句。

---

## 作业要求

1. 要求每位同学对（扩展后的）L25语言，使用 C/C++/Java/Python/flex/bison 编写其编译器，要求完成词法分析、语法分析、出错处理、代码生成和解释程序。满分为100分。

2. 使用该语言编写至少三个有一定逻辑功能的程序（如计算阶乘等），并使用自己编写的编译器进行编译，输出正确的结果。

3. 文档及程序皆不能抄袭其他同学的。抄袭者与被抄袭者将获得相同成绩。同学们有义务保护自己的劳动成果不被盗用。同时，鼓励发表意见、交流讨论，这与抄袭是不同的。

---

## 作业提交

此次作业需提交到FTP服务器上（无需在超星上提交），分为以下三个子目录：

- **文档**：说明编译器的设计与运行方式，包括：
  - 扩展后的文法定义
  - 代码结构
  - 运行方式
  - 测试结果截图等
- **程序**：包含源程序和可执行程序
- **测试**：包含测试用例

---

## 作业检查

检查时间为每周三上午上机时间，地点为机房，持续4周。由教师和助教检查作业：

- 被检查者会被要求执行自己准备的测试用例，也可能需要执行教师或助教提供的测试用例。
- 测试必须得到正确结果。
- 需要就源程序及文档回答相关问题。
- 若无法完成上述任务，则不能通过检查。

为了避免冲突，学生将被分成若干组，每组需完成指定的语言扩展。分组及扩展要求如下：

| 组别  | 扩展内容                            |
|-----|---------------------------------|
| 第一组 | 无扩展                             |
| 第二组 | 支持字符串类型 `str` 及其基本运算（`+`, `*`）  |
| 第三组 | 支持一维静态数组和结构体（`struct`）的声明、赋值和访问 |
| 第四组 | 引入 `try-catch` 语句，用于捕获除零错误      |

---

## 评分规则

1. **基础得分（75~80分）**
   - 文档、程序、测试用例齐全，并完成所在分组的指定扩展点。
   - 无界面：75分；有良好用户界面：80分。

2. **加分项（最多加25分）**
   - 实现以下功能可申请加分（需主动演示）：
     - 支持指针类型（参考C语言）
     - 支持 `map` 和 `set` 类型及其操作
     - 使用 ANTLR、JavaCC、LLVM 等业界编译工具或框架实现

3. **不合格标准**
   - 超过规定期限未上传作业到FTP
   - 未接受作业检查
   - 有抄袭行为
   - 不能提供源程序
   - 无文档说明

---

## 其他说明

如有未尽事宜，以任课老师的规定为准。祝大家顺利完成本次大作业！


##  Extension Grammar

 **Support One-dimensional static array and structure**

```text
<program> = "program" <ident> "{" { <struct_def> } { <func_def> } "main" "{" <stmt_list> "}" "}"

<struct_def> = <struct> <ident> "{" [ <member_list> ] "}"

<member_list> = <member_dec> { "," <member_dec> }

<member_dec> = <ident> | <ident> ":" "[" <number> "]"

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
                    | "="  <expr>
                    | ":" <array_suffix> [ "=" <array_init_expr> ]
                    | ":" <struct_suffix> [ "=" <struct_init_expr> ]
                    )
                
<array_suffix> = "[" <number> "]"

<array_init_expr> = "[" <expr>  { ","  <expr> }  "]"

<array_ref> = <ident> "[" <expr> "]" 

<struct_suffix> = <ident>
 
<struct_init_expr> = "{" <expr> { ","  <expr> }  "}"

<struct_ref> = <ident> "." <ident> 
               | <ident> "." <ident> "[" <expr> "]" 

<assign_stmt> = <factor> "=" <expr>

<if_stmt> = "if" "(" <bool_expr> ")" "{" <stmt_list> "}" [ "else" "{" <stmt_list> "}" ]

<while_stmt> = "while" "(" <bool_expr> ")" "{" <stmt_list> "}"

<func_call> = <ident> "(" [ <arg_list> ] ")"

<arg_list> = <expr> { "," <expr> }

<input_stmt> = "input" "(" <lvalue> { "," <lvalue> } ")"

<output_stmt> = "output" "(" <expr> { "," <expr> } ")"

<bool_expr> = <expr> ("==" | "!=" | "<" | "<=" | ">" | ">=") <expr>

<expr> = [ "+" | "-" ] <term> { ("+" | "-") <term> }

<term> = <factor> { ("*" | "/") <factor> }

<factor> = <ident>                   
         | <number>                
         | "(" <expr> ")"           
         | <func_call>   
         | <array_ref>
         | <struct_ref>    
         
<lvalue> = <ident> 
         | <array_ref>
         | <struct_ref>      

<ident> = <letter> { <letter> | <digit> }

<number> = <digit> { <digit> }

<letter> = "a" | "b" | ... | "z" | "A" | "B" | ... | "Z"

<digit> = "0" | "1" | ... | "9"

```

___

##  Extension Code

```txt
program MyApp {
    
    struct point{a, b, c : [2]}

    func add(a, b) {
        let sum = a + b;
        return sum;
    }
    
    main {
        let x = 5;
        let y = add(x, 10);
        
        let arr : [3] = [1,2,3];
        let p : point = {1,2,3,4};
        
        arr[0]= 4;
        p.a = 5;
        p.c[0] = 6;
        
        let i = 2;
        while(i >= 0){
          output(arr[i]);  
        };
        
        input(arr[2]);
        output(arr[2]);
        
        input(p.b);
        output(p.b);
        
        input(p.c[1]);
        output(p.c[1]);
    }
}
```

___

### 虚拟机指令集

| 指令  | 参数 | 功能描述                              |
|-----|----|-----------------------------------|
| LIT | a  | 将常量 `a` 的值取到栈顶                    |
| OPR | a  | 数学、逻辑运算 (见下表)                     |
| LOD | a  | 取相对当前过程的数据基地址为 `a` 的内存的值到栈顶       |
| STO | a  | 栈顶的值存入变量，存储到相对当前过程的数据基地址为 `a` 的内存 |
| CAL | a  | 调用子过程                             |
| INT | a  | 分配数据区空间                           |
| JMP | a  | 直接跳转                              |
| JPC | a  | 条件跳转 (当栈顶为 `0` 时跳转)               |
| RET | -  | 函数调用结束后返回                         |
| HLT | -  | 程序结束                              |
| STP | l  | 将次栈顶值作为地址，将栈顶的值存储到指定层级和偏移的地址      |
| LOS | l  | 加载某个偏移位置上的值到栈顶                    |

---

### OPR 操作码

| 代码 | 操作            |
|----|---------------|
| 0  | 保留（未使用）       |
| 1  | 栈顶元素取反        |
| 2  | 次栈顶项加上栈顶项     |
| 3  | 次栈顶项减去栈顶项     |
| 4  | 次栈顶项乘以栈顶项     |
| 5  | 次栈顶项除以栈顶项     |
| 6  | 次栈顶项取余 `2`    |
| 8  | 次栈顶项与栈顶项是否相等  |
| 9  | 次栈顶项与栈顶项是否不等  |
| 10 | 次栈顶项是否小于栈顶项   |
| 11 | 次栈顶项是否大于等于栈顶项 |
| 12 | 次栈顶项是否大于栈顶项   |
| 13 | 次栈顶项是否小于等于栈顶项 |
| 14 | 输出栈顶值         |
| 15 | 输出换行符         |
| 16 | 读入一个输入置于栈顶    |
