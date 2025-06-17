package github.calabchen;

/**
 * 　　语法分析器。这是PL/0分析器中最重要的部分，在语法分析的过程中穿插着语法错误检查和目标代码生成。
 */
public class Parser {
    private Scanner lex;                    // 对词法分析器的引用
    private Table table;                    // 对符号表的引用
    private Interpreter interp;                // 对目标代码生成器的引用

    private final int symnum = Symbol.values().length;

    // 表示声明开始的符号集合、表示语句开始的符号集合、表示因子开始的符号集合
    // 实际上这就是声明、语句和因子的FIRST集合
    private SymSet declbegsys, statbegsys, facbegsys;

    /**
     * 当前符号，由nextsym()读入
     *
     * @see #nextSym()
     */
    private Symbol sym;

    /**
     * 当前作用域的堆栈帧大小，或者说数据大小（data size）
     */
    private int dx = 0;

    /**
     * 构造并初始化语法分析器，这里包含了C语言版本中init()函数的一部分代码
     *
     * @param l 编译器的词法分析器
     * @param t 编译器的符号表
     * @param i 编译器的目标代码生成器
     */
    public Parser(Scanner l, Table t, Interpreter i) {
        lex = l;
        table = t;
        interp = i;

        // 设置声明开始符号集
        declbegsys = new SymSet(symnum);
        declbegsys.set(Symbol.funcsym);
        declbegsys.set(Symbol.structsym);

        // 设置语句开始符号集
        statbegsys = new SymSet(symnum);
        statbegsys.set(Symbol.letsym);
        statbegsys.set(Symbol.ident);// assign or func_call
        statbegsys.set(Symbol.ifsym);
        statbegsys.set(Symbol.whilesym);
        statbegsys.set(Symbol.inputsym);
        statbegsys.set(Symbol.outputsym);

        // 设置因子开始符号集
        facbegsys = new SymSet(symnum);
        facbegsys.set(Symbol.ident); // ident or func_call
        facbegsys.set(Symbol.number);
        facbegsys.set(Symbol.lparen);
        facbegsys.set(Symbol.minus);
    }

    /**
     * 启动语法分析过程，此前必须先调用一次nextsym()
     *
     * @see #nextSym()
     */
    public void parse() {
        SymSet nxtlev = new SymSet(symnum);
        nxtlev.or(declbegsys);
        nxtlev.or(statbegsys);

        if (sym == Symbol.programsym) {
            nextSym(); //读进 program name
            if (sym == Symbol.ident) {
                nextSym();//读进 '{'
                if (sym == Symbol.lbrace) {
                    nextSym();//读进 声明 或者 main
                    parseProgram(0, nxtlev);
                    if (sym == Symbol.rbrace) {
                        System.out.println("Program parsed successfully!");
                    } else {
                        Err.report(4);
                    }
                } else {
                    Err.report(3);
                }
            } else {
                Err.report(2);
            }
        } else {
            Err.report(1);
        }

        interp.listcode(0);
    }


    /**
     * 获得下一个语法符号，这里只是简单调用一下getsym()
     */
    public void nextSym() {
        lex.getsym();
        sym = lex.sym;
    }

    /**
     * 分析<主程序>
     * lev:    当前分程序所在层
     * tx:     符号表当前尾指针
     * fsys:   当前模块后继符号集合
     */
    public void parseProgram(int lev, SymSet fsys) {
        dx = 3;                 // 初始化为3（静态链、动态链、返回地址）
        int jmpToMainAddr = interp.cx;
        interp.gen(Fct.JMP, 0, 0);  // 稍后回填跳转到 main 的入口

        if (lev > L25.levmax) {
            Err.report(52);
        }

        do {
            while (sym == Symbol.structsym) {
                nextSym();//读进 struct name
                parseStructDeclaration(lev + 1, fsys);
            }
            while (sym == Symbol.funcsym) {
                nextSym();//读进 function name
                parseFuncDeclaration(lev + 1, fsys);
            }
        } while (declbegsys.get(sym));

        if (sym == Symbol.mainsym) {
            int mainStartAddr = interp.cx; // 记录 INT 指令位置，稍后回填 dx
            interp.gen(Fct.INT, 0, 0);    // 占位：实际 dx 等语句处理完后再填
            interp.setCode(jmpToMainAddr, Fct.JMP, lev + 1, mainStartAddr); // 回填跳转地址

            int mainDx0 = dx;
            table.enter(Objekt.mainfunc, lev + 1, dx); // main 的名字表项
            int mainTx0 = table.tx;  // main 的符号表起始点

            nextSym();//读进 '{'
            if (sym == Symbol.lbrace) {
                nextSym(); // 读入 main 的语句体
                parseStatementList(lev + 2, fsys);

                // 填写 main 名字表信息
                Table.Item item = table.get(mainTx0);
                item.adr = mainStartAddr;
                item.size = dx;
                interp.setCode(mainStartAddr, Fct.INT, lev + 1, dx); // 回填内存分配大小

                if (sym == Symbol.rbrace) {
                    table.debugTable(mainTx0);
                    // 恢复作用域指针
                    dx = mainDx0;
                    table.tx = mainTx0;

                    nextSym();//读进 program '}'

                    // 程序结束，生成HLT指令
                    interp.gen(Fct.HLT, 0, 0);
                } else {
                    Err.report(17);
                }
            } else {
                Err.report(16);
            }
        } else {
            Err.report(15);
        }
    }

    private void parseStructDeclaration(int lev, SymSet fsys) {
        if (sym == Symbol.ident) {
            table.enter(Objekt.struct, lev, dx);
            String structName = lex.id;

            nextSym();//读进 '{'
            if (sym == Symbol.lbrace) {
                nextSym();//读进 MemberList
                parseMemberList(lev + 1, structName, fsys);
                if (sym == Symbol.rbrace) {
                    nextSym();
                } else {
                    Err.report(48);
                }
            } else {
                Err.report(47);
            }
        } else {
            Err.report(46);
        }
    }

    private void parseMemberList(int lev, String name, SymSet fsys) {
        int memberDx0 = dx;
        dx = 0;

        // 结构体内部至少要有一个成员
        if (sym == Symbol.ident) {
            parseMemberDeclaration(lev, name, fsys);
        } else {
            Err.report(60); // 错误：此处应为member name
        }

        while (sym == Symbol.comma) {
            nextSym();
            if (sym == Symbol.ident) {
                parseMemberDeclaration(lev, name, fsys);
            } else {
                Err.report(34); // 错误：此处应为member name
            }
        }
        dx = memberDx0;
    }

    private void parseMemberDeclaration(int lev, String name, SymSet fsys) {
        int i = table.position(name);
        Table.Item item = table.get(i);

        String memberName = lex.id;
        Table.Item member = new Table.Item();

        nextSym();
        // 2. 判断成员是数组还是普通变量
        if (sym == Symbol.colon) {
            nextSym(); // 读入 '['
            if (sym == Symbol.lbracket) {
                nextSym(); // 读入数组长度
                if (sym == Symbol.number) {
                    member.name = memberName;
                    member.kind = Objekt.array;
                    member.size = lex.num;
                    member.adr = dx;
                    dx += lex.num;

                    item.memberList.put(memberName, member);
                    item.size = item.size + lex.num;

                    nextSym();
                    if (sym == Symbol.rbracket) {
                        nextSym(); // 成功，读入下一个符号
                    } else {
                        Err.report(43); // 缺少 ']'
                    }

                } else {
                    Err.report(51); // 缺少数组长度
                }
            } else {
                Err.report(42); // 缺少 '['
            }
        } else {
            // --- 处理普通变量成员 ---
            // 如果不是冒号，那它就是普通变量
            member.name = memberName;
            member.kind = Objekt.variable;
            member.adr = dx;
            dx++;

            item.memberList.put(memberName, member);
            item.size = item.size + 1;
        }
    }

    private void parseFuncDeclaration(int lev, SymSet fsys) {
        dx = 3;
        SymSet nxtlev;
        if (sym == Symbol.ident) {
            int funcStartAddr = interp.cx; // 记录 INT 指令位置，稍后回填 dx
            interp.gen(Fct.INT, 0, 0);    // 占位：实际 dx 等语句处理完后再填

            int funcDx0 = dx;
            table.enter(Objekt.function, lev, dx);
            int funcTx0 = table.tx;  // func 的符号表起始点

            nextSym();//读进 '{'
            if (sym == Symbol.lparen) {
                nextSym();//读进 ParamList 或者 ')'
                if (sym != Symbol.rparen) {
                    parseParamList(lev + 1, funcTx0, fsys);
                }

                if (sym == Symbol.rparen) {
                    nextSym();//读进 '{'
                    if (sym == Symbol.lbrace) {
                        nextSym();//读进 StatementList
                        parseStatementList(lev + 1, fsys);
                        if (sym == Symbol.returnsym) {
                            nextSym();//读进 Expression

                            // 解析返回表达式
                            nxtlev = fsys;
                            nxtlev.set(Symbol.semicolon);
                            parseExpression(lev + 1, nxtlev);

                            interp.gen(Fct.RET, 0, 0); // 函数返回值处理
                            if (sym == Symbol.semicolon) {
                                nextSym();//读进 '}'

                                // 填写 func 名字表信息
                                Table.Item item = table.get(funcTx0);
                                item.adr = funcStartAddr;
                                item.size = dx;
                                interp.setCode(funcStartAddr, Fct.INT, lev, dx); // 回填内存分配大小

                                if (sym == Symbol.rbrace) {
                                    table.debugTable(funcTx0);
                                    // 恢复作用域指针
                                    dx = funcDx0;
                                    table.tx = funcTx0;

                                    nextSym();//读进 next function name or main
                                } else {
                                    Err.report(10);
                                }
                            } else {
                                Err.report(40);
                            }
                        } else {
                            Err.report(12);
                        }
                    } else {
                        Err.report(8);
                    }
                } else {
                    Err.report(7);
                }
            } else {
                Err.report(6);
            }
        } else {
            Err.report(5);
        }
    }

    private void parseParamList(int lev, int tx0, SymSet fsys) {
        Table.Item item = table.get(tx0);
        if (sym == Symbol.ident) {
            table.enter(Objekt.variable, lev, dx);
            dx++;
            item.paramsize++;
            nextSym();
            while (sym == Symbol.comma) {
                nextSym();//读进 next ident
                if (sym == Symbol.ident) {
                    table.enter(Objekt.variable, lev, dx);
                    dx++;
                    item.paramsize++;
                    nextSym();
                } else {
                    Err.report(13);
                }
            }
        }
    }

    private void parseStatementList(int lev, SymSet fsys) {
        do {
            parseStatement(lev, fsys);
            if (sym == Symbol.semicolon) {
                nextSym();
            } else {
                Err.report(14);
                break;
            }
        } while (statbegsys.get(sym));
    }

    private void parseStatement(int lev, SymSet fsys) {
        switch (sym) {
            case letsym:
                nextSym();//读进 DeclareStatement
                parseDeclareStatement(lev, fsys);
                break;
            case ident:
                int i = table.position(lex.id); // table表中的位置
                if (i > 0) {
                    Table.Item item = table.get(i);
                    if (item.kind == Objekt.variable || item.kind == Objekt.array || item.kind == Objekt.struct) {
                        parseAssignStatement(lev, fsys);
                    } else if (item.kind == Objekt.function) {
                        nextSym();//读进 FuncCallStatement
                        parseFuncCallStatement(lev, fsys);
                        interp.gen(Fct.CAL, item.paramsize, item.adr);
                    } else {
                        Err.report(18);
                    }
                } else {
                    Err.report(19);
                }
                break;
            case ifsym:
                nextSym();//读进 '('
                parseIfStatement(lev, fsys);
                break;
            case whilesym:
                nextSym();//读进 '('
                parseWhileStatement(lev, fsys);
                break;
            case inputsym:
                nextSym();//读进 '('
                parseInputStatement(lev, fsys);
                break;
            case outputsym:
                nextSym();//读进 '('
                parseOutputStatement(lev, fsys);
                break;
            default:
                Err.report(53);// 非法语句
                nextSym();// 跳过错误符号
                break;
        }
    }

    private void parseOutputStatement(int lev, SymSet fsys) {
        if (sym == Symbol.lparen) {
            nextSym();//读进 '('
            parseExpression(lev, fsys);
            interp.gen(Fct.OPR, 0, 14);
            interp.gen(Fct.OPR, 0, 15);

            while (sym == Symbol.comma) {
                nextSym();//读进 ','
                parseExpression(lev, fsys);

                interp.gen(Fct.OPR, 0, 14);
                interp.gen(Fct.OPR, 0, 15);
            }
            if (sym == Symbol.rparen) {
                nextSym();//读进 ')'
            } else {
                Err.report(36);
            }
        } else {
            Err.report(35);
        }
    }

    private void parseInputStatement(int lev, SymSet fsys) {
        int i;
        Table.Item item;
        if (sym == Symbol.lparen) {
            nextSym();//读进 '('

            if (sym == Symbol.ident) {
                nextSym();//读进 ident

                i = table.position(lex.id);
                item = table.get(i);

                if (item.kind == Objekt.variable) {
                    interp.gen(Fct.OPR, 0, 16);
                    interp.gen(Fct.STO, lev - item.level, item.adr);
                } else if (item.kind == Objekt.array) {
                    parseArrayRef(lev, fsys);
                    interp.gen(Fct.OPR, 0, 16);
                    interp.gen(Fct.STP, lev - item.level, 0);
                } else if (item.kind == Objekt.struct) {
                    parseStructRef(lev, fsys);
                    interp.gen(Fct.OPR, 0, 16);
                    interp.gen(Fct.STP, lev - item.level, 0);
                } else {
                    Err.report(18);
                }

                while (sym == Symbol.comma) {
                    nextSym();//读进 ','
                    if (sym == Symbol.ident) {
                        nextSym();

                        i = table.position(lex.id);
                        item = table.get(i);

                        if (item.kind == Objekt.variable) {
                            interp.gen(Fct.OPR, 0, 16);
                            interp.gen(Fct.STO, lev - item.level, item.adr);
                        } else if (item.kind == Objekt.array) {
                            parseArrayRef(lev, fsys);
                            interp.gen(Fct.OPR, 0, 16);
                            interp.gen(Fct.STP, lev - item.level, 0);
                        } else if (item.kind == Objekt.struct) {
                            parseStructRef(lev, fsys);
                            interp.gen(Fct.OPR, 0, 16);
                            interp.gen(Fct.STP, lev - item.level, 0);
                        } else {
                            Err.report(18);
                        }

                    } else {
                        Err.report(34);
                    }
                }
            }

            if (sym == Symbol.rparen) {
                nextSym();//读进 ')'
            } else {
                Err.report(33);
            }
        } else {
            Err.report(32);
        }
    }

    private void parseFuncCallStatement(int lev, SymSet fsys) {
        if (sym == Symbol.lparen) {
            nextSym(); //读进 '('
            parseArgListStatement(lev, fsys);
            if (sym == Symbol.rparen) {
                nextSym();//读进 ')'
            } else {
                Err.report(31);
            }
        } else {
            Err.report(30);
        }
    }

    private void parseArgListStatement(int lev, SymSet fsys) {
        parseExpression(lev, fsys);
        while (sym == Symbol.comma) {
            nextSym();//读进 ','
            parseExpression(lev, fsys);
        }
    }

    private void parseWhileStatement(int lev, SymSet fsys) {
        int whileStartAddr = interp.cx; // 记录判断条件操作的位置
        int whileDx0 = dx;
        int whileTx0 = table.tx;  // whlie 的符号表起始点
        dx = 3;

        if (sym == Symbol.lparen) {
            nextSym();//读进 '('
            parseBoolExpression(lev, fsys);
            if (sym == Symbol.rparen) {
                nextSym();//读进 ')'

                int calStartAddr = interp.cx;
                interp.gen(Fct.JPC, 0, 0); // 占位：实际 dx 等语句处理完后再填
                interp.gen(Fct.CAL, 0, calStartAddr + 2);
                interp.gen(Fct.INT, 0, 0); // 占位：实际 dx 等语句处理完后再填
                if (sym == Symbol.lbrace) {
                    nextSym();//读进 '{'
                    parseStatementList(lev + 1, fsys);

                    interp.gen(Fct.RET, 0, 1);
                    interp.gen(Fct.JMP, 0, whileStartAddr); // 循环体结束后跳转到判断条件

                    // 填写 while 名字表信息
//                    Table.Item item = table.get(whileTx0);
//                    item.adr = whileStartAddr;
//                    item.size = dx;
                    interp.setCode(calStartAddr, Fct.JPC, 0, interp.cx); // 回填内存分配大小
                    interp.setCode(calStartAddr + 2, Fct.INT, lev, dx); // 回填内存分配大小

                    if (sym == Symbol.rbrace) {
//                        table.debugTable(whileTx0);
                        // 恢复作用域指针
                        dx = whileDx0;
                        table.tx = whileTx0;

                        nextSym();//读进 '}'
                    } else {
                        Err.report(29);
                    }
                } else {
                    Err.report(28);
                }
            } else {
                Err.report(27);
            }
        } else {
            Err.report(26);
        }
    }

    private void parseIfStatement(int lev, SymSet fsys) {
        int cx1, cx2;

        int ifTx0 = table.tx;  // whlie 的符号表起始点
        int ifDx0 = dx;
        if (sym == Symbol.lparen) {
            nextSym();//读进 BoolExpression
            parseBoolExpression(lev, fsys);
            if (sym == Symbol.rparen) {
                nextSym();//读进 '{'

                cx1 = interp.cx;
                interp.gen(Fct.JPC, 0, 0); // 占位：实际 dx 等语句处理完后再填
                interp.gen(Fct.CAL, 0, cx1 + 2);
                interp.gen(Fct.INT, 0, 0); // 占位：实际 dx 等语句处理完后再填

                if (sym == Symbol.lbrace) {
                    nextSym();//读进 StatementList

                    table.tx = ifTx0;
                    dx = 3;

                    parseStatementList(lev + 1, fsys);
                    interp.gen(Fct.RET, 0, 1);

                    interp.setCode(cx1, Fct.JPC, 0, interp.cx); // 回填内存分配大小
                    interp.setCode(cx1 + 2, Fct.INT, lev, dx); // 回填内存分配大小

                    if (sym == Symbol.rbrace) {
                        nextSym();//读进 '}'
//                        table.debugTable(ifTx0);
                        // 恢复作用域指针
                        dx = ifDx0;
                        table.tx = ifTx0;

                        int elseTx0 = table.tx;  // else 的符号表起始点
                        int elseDx0 = dx;
                        if (sym == Symbol.elsesym) {
                            nextSym();//读进 '{'

                            cx2 = interp.cx;
                            interp.gen(Fct.JMP, 0, 0);
                            interp.setCode(cx1, Fct.JPC, 0, cx2 + 1); // 回填内存分配大小
                            interp.gen(Fct.CAL, 0, cx2 + 2);
                            interp.gen(Fct.INT, 0, 0);
                            if (sym == Symbol.lbrace) {
                                nextSym();//读进 StatementList

                                table.tx = elseTx0;
                                dx = 3;

                                parseStatementList(lev + 1, fsys);
                                interp.gen(Fct.RET, 0, 1);

                                interp.setCode(cx2, Fct.JMP, 0, interp.cx); // 回填内存分配大小
                                interp.setCode(cx2 + 2, Fct.INT, lev, dx); // 回填内存分配大小

                                if (sym == Symbol.rbrace) {
                                    nextSym();
//                                    table.debugTable(elseTx0);
                                    // 恢复作用域指针
                                    dx = elseDx0;
                                    table.tx = elseTx0;
                                } else {
                                    Err.report(25);
                                }
                            } else {
                                Err.report(24);
                            }
                        }
                    } else {
                        Err.report(23);
                    }
                } else {
                    Err.report(22);
                }
            } else {
                Err.report(21);
            }
        } else {
            Err.report(20);
        }
    }

    private void parseBoolExpression(int lev, SymSet fsys) {
        Symbol relop;
        SymSet nxtlev;

        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.eql);
        nxtlev.set(Symbol.neq);
        nxtlev.set(Symbol.lss);
        nxtlev.set(Symbol.leq);
        nxtlev.set(Symbol.gtr);
        nxtlev.set(Symbol.geq);

        parseExpression(lev, nxtlev);
        if (sym == Symbol.eql || sym == Symbol.neq || sym == Symbol.lss || sym == Symbol.leq || sym == Symbol.gtr || sym == Symbol.geq) {
            relop = sym;
            nextSym();
            parseExpression(lev, fsys);
            switch (relop) {
                case eql:
                    interp.gen(Fct.OPR, 0, 8);
                    break;
                case neq:
                    interp.gen(Fct.OPR, 0, 9);
                    break;
                case lss:
                    interp.gen(Fct.OPR, 0, 10);
                    break;
                case geq:
                    interp.gen(Fct.OPR, 0, 11);
                    break;
                case gtr:
                    interp.gen(Fct.OPR, 0, 12);
                    break;
                case leq:
                    interp.gen(Fct.OPR, 0, 13);
                    break;
            }
        } else {
            Err.report(37);
        }
    }

    private void parseAssignStatement(int lev, SymSet fsys) {
        int i = table.position(lex.id);
        Table.Item item = table.get(i);

        switch (item.kind) {
            case variable:
                nextSym();
                break;
            case array:
                nextSym();
                parseArrayRef(lev, fsys);
                break;
            case struct:
                nextSym();
                parseStructRef(lev, fsys);
                break;
            default:
                Err.report(18);
                break;
        }

        if (sym == Symbol.becomes) {
            nextSym(); //读进 '='
            parseExpression(lev, fsys);
            switch (item.kind) {
                case variable -> interp.gen(Fct.STO, lev - item.level, item.adr);
                case array, struct -> interp.gen(Fct.STP, lev - item.level, 0);
                default -> Err.report(18);
            }
        } else {
            Err.report(56);
        }
    }

    private void parseArrayRef(int lev, SymSet fsys) {
        int i = table.position(lex.id);
        Table.Item item = table.get(i);

        if (sym == Symbol.lbracket) {
            nextSym();
            interp.gen(Fct.LIT, 0, item.adr);
            parseExpression(lev, fsys);
            interp.gen(Fct.OPR, 0, 2);
            if (sym == Symbol.rbracket) {
                nextSym();
            } else {
                Err.report(45); // 缺少"]"
            }
        } else {
            Err.report(44); // 缺少"["
        }
    }

    private void parseStructRef(int lev, SymSet fsys) {
        int i = table.position(lex.id);
        Table.Item item = table.get(i);

        if (sym == Symbol.period) {
            nextSym();
            if (sym == Symbol.ident) {
                if (item.memberList.containsKey(lex.id)) {
                    Table.Item memberName = item.memberList.get(lex.id);

                    if (memberName.kind == Objekt.variable) {
                        interp.gen(Fct.LIT, 0, item.adr + memberName.adr);
                        nextSym();
                    } else if (memberName.kind == Objekt.array) {
                        nextSym();
                        if (sym == Symbol.lbracket) {
                            nextSym();
                            parseExpression(lev, fsys);
                            interp.gen(Fct.LIT, 0, item.adr + memberName.adr);
                            interp.gen(Fct.OPR, 0, 2);
                            if (sym == Symbol.rbracket) {
                                nextSym();
                            } else {
                                Err.report(45);// 缺少"]"
                            }
                        } else {
                            Err.report(44); // 缺少"["
                        }
                    }
                } else {
                    Err.report(61);
                }
            } else {
                Err.report(60);
            }
        } else {
            Err.report(59);
        }
    }

    private void parseDeclareStatement(int lev, SymSet fsys) {
        int i;
        Table.Item itemi;

        if (sym == Symbol.ident) {
            table.enter(Objekt.unknown, lev, dx);
            dx++;
            i = table.position(lex.id);
            itemi = table.get(i);

            nextSym();//读进 '=' 或者 ‘:’
            if (sym == Symbol.becomes) {
                itemi.kind = Objekt.variable;
                nextSym(); //读进 Expression
                parseExpression(lev, fsys);
                interp.gen(Fct.STO, lev - itemi.level, itemi.adr);
            } else if (sym == Symbol.colon) {
                nextSym(); //读进 '[' or struct

                int s = table.position(lex.id);
                Table.Item items = table.get(s);

                if (sym == Symbol.lbracket) {
                    // array
                    itemi.kind = Objekt.array;
                    nextSym();//读入数组长度
                    if (sym == Symbol.number) {
                        if (lex.num > L25.arraymax || lex.num <= 0) {
                            Err.report(41);
                        } else {
                            dx += lex.num - 1;
                            itemi.size = lex.num;
                            nextSym();
                            if (sym == Symbol.rbracket) {
                                nextSym();
                            } else {
                                Err.report(43);
                            }
                        }
                    } else {
                        Err.report(51);
                    }

                    if (sym == Symbol.becomes) {
                        nextSym();
                        parseArrayInitExpression(lev, i, fsys);
                    }

                } else if (sym == Symbol.ident && items.kind == Objekt.struct && !items.structDeclared) {
                    // struct
                    itemi.kind = Objekt.struct;
                    itemi.size = items.size;
                    itemi.memberList = items.memberList;
                    itemi.structDeclared = true;
                    dx += itemi.size - 1;

                    nextSym();
                    if (sym == Symbol.becomes) {
                        nextSym();
                        parseStructInitExpression(lev, i, fsys);
                    }

                } else {
                    Err.report(42);
                }
            } else {
                itemi.kind = Objekt.variable;
            }
        } else {
            Err.report(13);
        }
    }

    private void parseStructInitExpression(int lev, int i, SymSet fsys) {
        Table.Item item;
        int cnt;

        if (sym == Symbol.lbrace) {
            item = table.get(i);
            cnt = 0;

            nextSym();//读进 Expression
            parseExpression(lev, fsys);

            cnt++;
            interp.gen(Fct.STO, 0, item.adr + cnt - 1);

            while (sym == Symbol.comma) {
                nextSym();//读进 ','
                parseExpression(lev, fsys);

                cnt++;
                interp.gen(Fct.STO, 0, item.adr + cnt - 1);
            }

            if (cnt < item.size) {
                Err.report(49);
            } else if (cnt > item.size) {
                Err.report(50);
            }

            if (sym == Symbol.rbrace) {
                nextSym();
            } else {
                Err.report(48);
            }
        } else {
            Err.report(47);
        }
    }

    private void parseArrayInitExpression(int lev, int i, SymSet fsys) {
        Table.Item item;
        int cnt;

        if (sym == Symbol.lbracket) {
            item = table.get(i);
            cnt = 0;

            nextSym();//读进 Expression
            parseExpression(lev, fsys);

            cnt++;
            interp.gen(Fct.STO, 0, item.adr + cnt - 1);

            while (sym == Symbol.comma) {
                nextSym();//读进 ','
                parseExpression(lev, fsys);

                cnt++;
                interp.gen(Fct.STO, 0, item.adr + cnt - 1);
            }

            if (cnt < item.size) {
                Err.report(54);
            } else if (cnt > item.size) {
                Err.report(55);
            }

            if (sym == Symbol.rbracket) {
                nextSym();
            } else {
                Err.report(43);
            }
        } else {
            Err.report(42);
        }
    }

    private void parseExpression(int lev, SymSet fsys) {
        Symbol addop;
        SymSet nxtlev;

        // 分析[+|-]<项>
        if (sym == Symbol.plus || sym == Symbol.minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
            if (addop == Symbol.minus)
                interp.gen(Fct.OPR, 0, 1);
        } else {
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
        }

        // 分析{<加法运算符><项>}
        while (sym == Symbol.plus || sym == Symbol.minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
            if (addop == Symbol.plus)
                interp.gen(Fct.OPR, 0, 2);
            else
                interp.gen(Fct.OPR, 0, 3);
        }
    }

    /**
     * 分析<项>
     *
     * @param fsys 后跟符号集
     * @param lev  当前层次
     */
    private void parseTerm(int lev, SymSet fsys) {
        Symbol mulop;
        SymSet nxtlev;

        // 分析<因子>
        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.times);
        nxtlev.set(Symbol.slash);
        parseFactor(lev, nxtlev);

        // 分析{<乘法运算符><因子>}
        while (sym == Symbol.times || sym == Symbol.slash) {
            mulop = sym;
            nextSym();
            parseFactor(lev, nxtlev);
            if (mulop == Symbol.times) interp.gen(Fct.OPR, 0, 4);
            else interp.gen(Fct.OPR, 0, 5);
        }
    }

    /**
     * 分析<因子>
     *
     * @param fsys 后跟符号集
     * @param lev  当前层次
     */
    private void parseFactor(int lev, SymSet fsys) {
        int i;
        SymSet nxtlev;
        i = table.position(lex.id);

        if (facbegsys.get(sym)) {
            if (sym == Symbol.ident) {
                if (i > 0) {
                    Table.Item item = table.get(i);
                    if (item.kind == Objekt.variable) {
                        interp.gen(Fct.LOD, lev - item.level, item.adr);
                        nextSym();//读进 var
                    } else if (item.kind == Objekt.function) {
                        nextSym();//读进 func_call
                        parseFuncCallStatement(lev, fsys);
                        interp.gen(Fct.CAL, item.paramsize, item.adr);
                    } else if (item.kind == Objekt.array) {
                        nextSym();//读进 '['
                        parseArrayRef(lev, fsys);
                        interp.gen(Fct.LOS, lev - item.level, 0);
                    } else if (item.kind == Objekt.struct) {
                        nextSym();//读进 '.'
                        parseStructRef(lev, fsys);
                        interp.gen(Fct.LOS, lev - item.level, 0);
                    } else {
                        Err.report(19);
                    }
                } else {
                    Err.report(18);
                }
            } else if (sym == Symbol.number) {
                int num = lex.num;
                if (num > L25.amax) {
                    Err.report(41);
                    num = 0;
                }
                interp.gen(Fct.LIT, 0, num);
                nextSym(); //读进 number
            } else if (sym == Symbol.lparen) {
                nextSym(); //读进 '('
                nxtlev = (SymSet) fsys.clone();
                nxtlev.set(Symbol.rparen);
                parseExpression(lev, nxtlev);
                if (sym == Symbol.rparen) {
                    nextSym();//读进 ')'
                } else {
                    Err.report(39);
                }
            } else {
                Err.report(38);
            }
        }
    }
}
