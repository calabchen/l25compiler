package github.calabchen;


public class Parser {
    private Scanner lex;
    private Table table;
    private Interpreter interp;

    private final int symnum = Symbol.values().length;


    private SymSet declbegsys, statbegsys, facbegsys;


    private Symbol sym;


    private int dx = 0;


    public Parser(Scanner l, Table t, Interpreter i) {
        lex = l;
        table = t;
        interp = i;


        declbegsys = new SymSet(symnum);
        declbegsys.set(Symbol.programsym);
        declbegsys.set(Symbol.mainsym);
        declbegsys.set(Symbol.letsym);
        declbegsys.set(Symbol.funcsym);
        declbegsys.set(Symbol.returnsym);


        statbegsys = new SymSet(symnum);
        statbegsys.set(Symbol.ifsym);
        statbegsys.set(Symbol.elsesym);
        statbegsys.set(Symbol.whilesym);
        statbegsys.set(Symbol.inputsym);
        statbegsys.set(Symbol.outputsym);


        facbegsys = new SymSet(symnum);
        facbegsys.set(Symbol.ident);
        facbegsys.set(Symbol.number);
        facbegsys.set(Symbol.lparen);

    }


    public void parse() {
        SymSet nxtlev = new SymSet(symnum);
        nxtlev.or(declbegsys);
        nxtlev.or(statbegsys);
        nxtlev.set(Symbol.period);
        parseBlock(0, nxtlev);

        if (sym != Symbol.period)
            Err.report(9);
    }


    public void nextSym() {
        lex.getsym();
        sym = lex.sym;
    }


    void test(SymSet s1, SymSet s2, int errcode) {

        if (!s1.get(sym)) {
            Err.report(errcode);

            while (!s1.get(sym) && !s2.get(sym))
                nextSym();
        }
    }


    public void parseBlock(int lev, SymSet fsys) {


        int dx0, tx0, cx0;
        SymSet nxtlev = new SymSet(symnum);

        dx0 = dx;
        dx = 3;
        tx0 = table.tx;
        table.get(table.tx).adr = interp.cx;

        interp.gen(Fct.JMP, 0, 0);

        if (lev > L25.levmax)
            Err.report(32);


        do {

            if (sym == Symbol.constsym) {
                nextSym();
                // the original do...while(sym == ident) is problematic, thanks to calculous
                // do
                parseConstDeclaration(lev);
                while (sym == Symbol.comma) {
                    nextSym();
                    parseConstDeclaration(lev);
                }

                if (sym == Symbol.semicolon)
                    nextSym();
                else
                    Err.report(5);
                // } while (sym == ident);
            }


            if (sym == Symbol.letsym) {
                nextSym();
                // the original do...while(sym == ident) is problematic, thanks to calculous
                // do {
                parseVarDeclaration(lev);
                while (sym == Symbol.comma) {
                    nextSym();
                    parseVarDeclaration(lev);
                }

                if (sym == Symbol.semicolon)
                    nextSym();
                else
                    Err.report(5);
                // } while (sym == ident);
            }


            while (sym == Symbol.procsym) {
                nextSym();
                if (sym == Symbol.ident) {
                    table.enter(Objekt.procedure, lev, dx);
                    nextSym();
                } else {
                    Err.report(4);
                }

                if (sym == Symbol.semicolon)
                    nextSym();
                else
                    Err.report(5);

                nxtlev = (SymSet) fsys.clone();
                nxtlev.set(Symbol.semicolon);
                parseBlock(lev + 1, nxtlev);

                if (sym == Symbol.semicolon) {
                    nextSym();
                    nxtlev = (SymSet) statbegsys.clone();
                    nxtlev.set(Symbol.ident);
                    nxtlev.set(Symbol.procsym);
                    test(nxtlev, fsys, 6);
                } else {
                    Err.report(5);
                }
            }

            nxtlev = (SymSet) statbegsys.clone();
            nxtlev.set(Symbol.ident);
            test(nxtlev, declbegsys, 7);
        } while (declbegsys.get(sym));


        Table.Item item = table.get(tx0);
        interp.code[item.adr].a = interp.cx;
        item.adr = interp.cx;
        item.size = dx;

        cx0 = interp.cx;
        interp.gen(Fct.INT, 0, dx);

        table.debugTable(tx0);


        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.semicolon);
        nxtlev.set(Symbol.endsym);
        parseStatement(nxtlev, lev);
        interp.gen(Fct.OPR, 0, 0);

        nxtlev = new SymSet(symnum);
        test(fsys, nxtlev, 8);

        interp.listcode(cx0);

        dx = dx0;
        table.tx = tx0;
    }


    void parseConstDeclaration(int lev) {
        if (sym == Symbol.ident) {
            nextSym();
            if (sym == Symbol.eql || sym == Symbol.becomes) {
                if (sym == Symbol.becomes)
                    Err.report(1);
                nextSym();
                if (sym == Symbol.number) {
                    table.enter(Objekt.constant, lev, dx);
                    nextSym();
                } else {
                    Err.report(2);
                }
            } else {
                Err.report(3);
            }
        } else {
            Err.report(4);
        }
    }


    void parseVarDeclaration(int lev) {
        if (sym == Symbol.ident) {

            table.enter(Objekt.variable, lev, dx);
            dx++;
            nextSym();
        } else {
            Err.report(4);
        }
    }


    void parseStatement(SymSet fsys, int lev) {
        SymSet nxtlev;

        switch (sym) {
            case ident:
                parseAssignStatement(fsys, lev);
                break;
            case readsym:
                parseReadStatement(fsys, lev);
                break;
            case writesym:
                parseWriteStatement(fsys, lev);
                break;
            case callsym:
                parseCallStatement(fsys, lev);
                break;
            case ifsym:
                parseIfStatement(fsys, lev);
                break;
            case beginsym:
                parseBeginStatement(fsys, lev);
                break;
            case whilesym:
                parseWhileStatement(fsys, lev);
                break;
            default:
                nxtlev = new SymSet(symnum);
                test(fsys, nxtlev, 19);
                break;
        }
    }


    private void parseWhileStatement(SymSet fsys, int lev) {
        int cx1, cx2;
        SymSet nxtlev;

        cx1 = interp.cx;
        nextSym();
        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.dosym);
        parseCondition(nxtlev, lev);
        cx2 = interp.cx;
        interp.gen(Fct.JPC, 0, 0);
        if (sym == Symbol.dosym)
            nextSym();
        else
            Err.report(18);
        parseStatement(fsys, lev);
        interp.gen(Fct.JMP, 0, cx1);
        interp.code[cx2].a = interp.cx;
    }


    private void parseBeginStatement(SymSet fsys, int lev) {
        SymSet nxtlev;

        nextSym();
        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.semicolon);
        nxtlev.set(Symbol.endsym);
        parseStatement(nxtlev, lev);

        while (statbegsys.get(sym) || sym == Symbol.semicolon) {
            if (sym == Symbol.semicolon)
                nextSym();
            else
                Err.report(10);
            parseStatement(nxtlev, lev);
        }
        if (sym == Symbol.endsym)
            nextSym();
        else
            Err.report(17);
    }


    private void parseIfStatement(SymSet fsys, int lev) {
        int cx1;
        SymSet nxtlev;

        nextSym();
        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.thensym);
        nxtlev.set(Symbol.dosym);
        parseCondition(nxtlev, lev);
        if (sym == Symbol.thensym)
            nextSym();
        else
            Err.report(16);
        cx1 = interp.cx;
        interp.gen(Fct.JPC, 0, 0);
        parseStatement(fsys, lev);
        interp.code[cx1].a = interp.cx;

    }


    private void parseCallStatement(SymSet fsys, int lev) {
        int i;
        nextSym();
        if (sym == Symbol.ident) {
            i = table.position(lex.id);
            if (i == 0) {
                Err.report(11);
            } else {
                Table.Item item = table.get(i);
                if (item.kind == Objekt.procedure)
                    interp.gen(Fct.CAL, lev - item.level, item.adr);
                else
                    Err.report(15);
            }
            nextSym();
        } else {
            Err.report(14);
        }
    }


    private void parseWriteStatement(SymSet fsys, int lev) {
        SymSet nxtlev;

        nextSym();
        if (sym == Symbol.lparen) {
            do {
                nextSym();
                nxtlev = (SymSet) fsys.clone();
                nxtlev.set(Symbol.rparen);
                nxtlev.set(Symbol.comma);
                parseExpression(nxtlev, lev);
                interp.gen(Fct.OPR, 0, 14);
            } while (sym == Symbol.comma);

            if (sym == Symbol.rparen)
                nextSym();
            else
                Err.report(33);
        }
        interp.gen(Fct.OPR, 0, 15);
    }


    private void parseReadStatement(SymSet fsys, int lev) {
        int i;

        nextSym();
        if (sym == Symbol.lparen) {
            do {
                nextSym();
                if (sym == Symbol.ident)
                    i = table.position(lex.id);
                else
                    i = 0;

                if (i == 0) {
                    Err.report(35);
                } else {
                    Table.Item item = table.get(i);
                    if (item.kind != Objekt.variable) {
                        Err.report(32);
                    } else {
                        interp.gen(Fct.OPR, 0, 16);
                        interp.gen(Fct.STO, lev - item.level, item.adr);
                    }
                }

                nextSym();
            } while (sym == Symbol.comma);
        } else {
            Err.report(34);
        }

        if (sym == Symbol.rparen) {
            nextSym();
        } else {
            Err.report(33);
            while (!fsys.get(sym))
                nextSym();
        }
    }


    private void parseAssignStatement(SymSet fsys, int lev) {
        int i;
        SymSet nxtlev;

        i = table.position(lex.id);
        if (i > 0) {
            Table.Item item = table.get(i);
            if (item.kind == Objekt.variable) {
                nextSym();
                if (sym == Symbol.becomes)
                    nextSym();
                else
                    Err.report(13);
                nxtlev = (SymSet) fsys.clone();
                parseExpression(nxtlev, lev);

                interp.gen(Fct.STO, lev - item.level, item.adr);
            } else {
                Err.report(12);
            }
        } else {
            Err.report(11);
        }
    }


    private void parseExpression(SymSet fsys, int lev) {
        Symbol addop;
        SymSet nxtlev;


        if (sym == Symbol.plus || sym == Symbol.minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(nxtlev, lev);
            if (addop == Symbol.minus)
                interp.gen(Fct.OPR, 0, 1);
        } else {
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(nxtlev, lev);
        }


        while (sym == Symbol.plus || sym == Symbol.minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            parseTerm(nxtlev, lev);
            if (addop == Symbol.plus)
                interp.gen(Fct.OPR, 0, 2);
            else
                interp.gen(Fct.OPR, 0, 3);
        }
    }


    private void parseTerm(SymSet fsys, int lev) {
        Symbol mulop;
        SymSet nxtlev;


        nxtlev = (SymSet) fsys.clone();
        nxtlev.set(Symbol.times);
        nxtlev.set(Symbol.slash);
        parseFactor(nxtlev, lev);


        while (sym == Symbol.times || sym == Symbol.slash) {
            mulop = sym;
            nextSym();
            parseFactor(nxtlev, lev);
            if (mulop == Symbol.times)
                interp.gen(Fct.OPR, 0, 4);
            else
                interp.gen(Fct.OPR, 0, 5);
        }
    }


    private void parseFactor(SymSet fsys, int lev) {
        SymSet nxtlev;

        test(facbegsys, fsys, 24);

        if (facbegsys.get(sym)) {
            if (sym == Symbol.ident) {
                int i = table.position(lex.id);
                if (i > 0) {
                    Table.Item item = table.get(i);
                    switch (item.kind) {
                        case constant:
                            interp.gen(Fct.LIT, 0, item.val);
                            break;
                        case variable:
                            interp.gen(Fct.LOD, lev - item.level, item.adr);
                            break;
                        case procedure:
                            Err.report(21);
                            break;
                    }
                } else {
                    Err.report(11);
                }
                nextSym();
            } else if (sym == Symbol.number) {
                int num = lex.num;
                if (num > L25.amax) {
                    Err.report(31);
                    num = 0;
                }
                interp.gen(Fct.LIT, 0, num);
                nextSym();
            } else if (sym == Symbol.lparen) {
                nextSym();
                nxtlev = (SymSet) fsys.clone();
                nxtlev.set(Symbol.rparen);
                parseExpression(nxtlev, lev);
                if (sym == Symbol.rparen)
                    nextSym();
                else
                    Err.report(22);
            } else {
                test(fsys, facbegsys, 23);
            }
        }
    }

    private void parseCondition(SymSet fsys, int lev) {
        Symbol relop;
        SymSet nxtlev;

        if (sym == Symbol.oddsym) {

            nextSym();
            parseExpression(fsys, lev);
            interp.gen(Fct.OPR, 0, 6);
        } else {

            nxtlev = (SymSet) fsys.clone();
            nxtlev.set(Symbol.eql);
            nxtlev.set(Symbol.neq);
            nxtlev.set(Symbol.lss);
            nxtlev.set(Symbol.leq);
            nxtlev.set(Symbol.gtr);
            nxtlev.set(Symbol.geq);
            parseExpression(nxtlev, lev);
            if (sym == Symbol.eql || sym == Symbol.neq
                    || sym == Symbol.lss || sym == Symbol.leq
                    || sym == Symbol.gtr || sym == Symbol.geq) {
                relop = sym;
                nextSym();
                parseExpression(fsys, lev);
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
                Err.report(20);
            }
        }
    }
}
