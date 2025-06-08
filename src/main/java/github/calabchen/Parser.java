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

        int beginlev = 0;
        if (sym == Symbol.programsym) {
            nextSym(); //读进 program name
            parseProgram(beginlev, nxtlev);
        } else {
            Err.report(1);
        }
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
     */
    public void parseProgram(int lev, SymSet fsys) {
        int dx0;                // 保留初始dx
        SymSet nxtlev = new SymSet(symnum);

        table.get(table.tx).adr = interp.cx;
        interp.gen(Fct.JMP, 0, 0);

        if (sym == Symbol.ident) {
            nextSym();//读进 '{'
            if (sym == Symbol.lbrace) {
                nextSym();//读进 声明 或者 main

                do {
                    while (sym == Symbol.funcsym) {
                        nextSym();//读进 function name
                        if (sym == Symbol.ident) {
                            table.enter(Objekt.function, lev + 1, dx);
                            nextSym();//读进 '{'
                            parseFuncDeclaration(lev + 1, nxtlev);
                        } else {
                            Err.report(5);
                        }
                    }
                } while (declbegsys.get(sym));

                if (sym == Symbol.mainsym) {
                    dx0 = dx;                        // 记录本层之前的数据量（以便恢复）
                    dx = 3;
                    table.enter(Objekt.mainfunc, lev + 1, dx);
                    nextSym();//读进 '{'
                    if (sym == Symbol.lbrace) {
                        nextSym();//读进 StatementList
                        parseStatementList(lev + 2, nxtlev);
                        if (sym == Symbol.rbrace) {
                            nextSym();//读进 program '}'

                            if (sym == Symbol.rbrace) {
                                dx = dx0;                            // 恢复堆栈帧计数器
                            } else {
                                Err.report(10);
                            }
                        } else {
                            Err.report(17);
                        }
                    } else {
                        Err.report(16);
                    }
                } else {
                    Err.report(15);
                }


            } else {
                Err.report(3);
            }

        } else {
            Err.report(2);
        }

    }

    private void parseFuncDeclaration(int lev, SymSet fsys) {
        int dx0, tx0, cx0;               // 保留初始dx，tx和cx
        SymSet nxtlev = new SymSet(symnum);

        dx0 = dx;                        // 记录本层之前的数据量（以便恢复）
        dx = 3;
        tx0 = table.tx;                  // 记录本层名字的初始位置（以便恢复）
        table.get(table.tx).adr = interp.cx;

        interp.gen(Fct.JMP, 0, 0);

        if (sym == Symbol.lparen) {
            nextSym();//读进 ParamList 或者 ')'
            parseParamList(lev + 1, nxtlev);
            if (sym == Symbol.rparen) {
                nextSym();//读进 '{'
                if (sym == Symbol.lbrace) {
                    nextSym();//读进 StatementList
                    parseStatementList(lev + 1, nxtlev);
                    if (sym == Symbol.returnsym) {
                        nextSym();//读进 Expression
                        parseExpression(lev, fsys);
                        if (sym == Symbol.semicolon) {
                            nextSym();//读进 '}'
                            if (sym == Symbol.rbrace) {
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

        // 开始生成当前过程代码
        Table.Item item = table.get(tx0);
        interp.code[item.adr].a = interp.cx;
        item.adr = interp.cx;                    // 当前过程代码地址
        item.size = dx;                          // 声明部分中每增加一条声明都会给dx增加1，
        // 声明部分已经结束，dx就是当前过程的堆栈帧大小
        cx0 = interp.cx;
        interp.gen(Fct.INT, 0, dx);            // 生成分配内存代码

        table.debugTable(tx0);

        interp.listcode(cx0);

        dx = dx0;                            // 恢复堆栈帧计数器
        table.tx = tx0;                        // 回复名字表位置
    }

    private void parseParamList(int lev, SymSet fsys) {
        if (sym == Symbol.ident) {
            table.enter(Objekt.variable, lev, dx);
            dx++;
            nextSym();
            while (sym == Symbol.comma) {
                nextSym();//读进 next ident
                if (sym == Symbol.ident) {
                    table.enter(Objekt.variable, lev, dx);
                    dx++;
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
            }
        } while (statbegsys.get(sym));
    }

    private void parseStatement(int lev, SymSet fsys) {
        int i; // table表中的位置
        SymSet nxtlev;

        i = table.position(lex.id);
        switch (sym) {
            case letsym:
                nextSym();//读进 DeclareStatement
                parseDeclareStatement(lev, fsys);
                break;
            case ident:
                if (i > 0) {
                    Table.Item item = table.get(i);
                    if (item.kind == Objekt.variable) {
                        nextSym();//读进 AssignStatement
                        parseAssignStatement(lev, fsys);
                    } else if (item.kind == Objekt.function) {
                        nextSym();//读进 FuncCallStatement
                        parseFuncCallStatement(lev, fsys);
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
                nxtlev = new SymSet(symnum);
                break;
        }
    }

    private void parseOutputStatement(int lev, SymSet fsys) {
        if (sym == Symbol.lparen) {
            nextSym();//读进 '('
            parseExpression(lev, fsys);
            while (sym == Symbol.comma) {
                nextSym();//读进 ','
                parseExpression(lev, fsys);
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
        if (sym == Symbol.lparen) {
            nextSym();//读进 '('
            if (sym == Symbol.ident) {
                nextSym();//读进 ident
                while (sym == Symbol.comma) {
                    nextSym();//读进 ','
                    if (sym == Symbol.ident) {
                        nextSym();
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
        dx = 3;
        if (sym == Symbol.lparen) {
            nextSym();//读进 '('
            parseBoolExpression(lev, fsys);
            if (sym == Symbol.rparen) {
                nextSym();//读进 ')'
                if (sym == Symbol.lbrace) {
                    nextSym();//读进 '{'
                    parseStatementList(lev + 1, fsys);
                    if (sym == Symbol.rbrace) {
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
        dx = 3;

        if (sym == Symbol.lparen) {
            nextSym();//读进 BoolExpression
            parseBoolExpression(lev, fsys);
            if (sym == Symbol.rparen) {
                nextSym();//读进 '{'
                if (sym == Symbol.lbrace) {
                    nextSym();//读进 StatementList
                    parseStatementList(lev + 1, fsys);
                    if (sym == Symbol.rbrace) {
                        nextSym();//读进 '}'

                        if (sym == Symbol.elsesym) {
                            dx = 3;
                            nextSym();//读进 '{'
                            if (sym == Symbol.lbrace) {
                                nextSym();//读进 StatementList
                                parseStatementList(lev + 1, fsys);
                                if (sym == Symbol.rbrace) {
                                    nextSym();
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
        if (sym == Symbol.becomes) {
            nextSym(); //读进 '='
            parseExpression(lev, fsys);
        }
    }

    private void parseDeclareStatement(int lev, SymSet fsys) {

        if (sym == Symbol.ident) {
            table.enter(Objekt.variable, lev, dx);
            dx++;
            nextSym();//读进 var
            if (sym == Symbol.becomes) {
                nextSym(); //读进 '='
                parseExpression(lev, fsys);
            }
        } else {
            Err.report(13);
        }
    }

    private void parseExpression(int lev, SymSet fsys) {
        SymSet nxtlev;

        // 分析[+|-]<项>
        if (sym == Symbol.plus || sym == Symbol.minus) {
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
        } else {
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
        }

        // 分析{<加法运算符><项>}
        while (sym == Symbol.plus || sym == Symbol.minus) {
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(lev, nxtlev);
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
                        nextSym();//读进 var
                    } else if (item.kind == Objekt.function) {
                        nextSym();//读进 func_call
                        parseFuncCallStatement(lev, fsys);
                    } else {
                        Err.report(19);
                    }
                } else {
                    Err.report(18);
                }
            } else if (sym == Symbol.number) {
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
