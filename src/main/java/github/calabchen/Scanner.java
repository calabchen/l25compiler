package github.calabchen;

import java.io.BufferedReader;
import java.io.IOException;


public class Scanner {

    private char ch = ' ';

    private char[] line;

    public int ll = 0;

    public int cc = 0;

    public Symbol sym;

    final private String[] word;

    final private Symbol[] wsym;

    final private Symbol[] ssym;

    final private BufferedReader in;

    public String id;

    public int num;


    public Scanner(BufferedReader input) {
        in = input;


        ssym = new Symbol[256];
        java.util.Arrays.fill(ssym, Symbol.nul);
        ssym['+'] = Symbol.plus;
        ssym['-'] = Symbol.minus;
        ssym['*'] = Symbol.times;
        ssym['/'] = Symbol.slash;
        ssym['('] = Symbol.lparen;
        ssym[')'] = Symbol.rparen;
        ssym['{'] = Symbol.lbrace;
        ssym['}'] = Symbol.rbrace;
        ssym[','] = Symbol.comma;
        ssym['.'] = Symbol.period;
        ssym[';'] = Symbol.semicolon;
        ssym['='] = Symbol.nul;
        ssym['!'] = Symbol.nul;


        word = new String[]{"program", "func", "if",
                "main", "input", "else", "let", "while", "output"};


        wsym = new Symbol[L25.norw];
        wsym[0] = Symbol.programsym;
        wsym[1] = Symbol.funcsym;
        wsym[2] = Symbol.mainsym;
        wsym[3] = Symbol.inputsym;
        wsym[4] = Symbol.outputsym;
        wsym[5] = Symbol.ifsym;
        wsym[6] = Symbol.elsesym;
        wsym[7] = Symbol.whilesym;
        wsym[8] = Symbol.letsym;
    }


    void getch() {
        String l = "";
        try {
            if (cc == ll) {
                while (l.isEmpty())
                    l = in.readLine().toLowerCase() + "\n";
                ll = l.length();
                cc = 0;
                line = l.toCharArray();
                System.out.println(L25.interp.cx + " " + l);
                L25.fa1.println(L25.interp.cx + " " + l);
            }
        } catch (IOException e) {
            throw new Error("program incomplete");
        }
        ch = line[cc];
        cc++;
    }


    public void getsym() {

        while (Character.isWhitespace(ch))
            getch();
        if (ch >= 'a' && ch <= 'z') {
            matchKeywordOrIdentifier();
        } else if (ch >= '0' && ch <= '9') {
            matchNumber();
        } else {

            matchOperator();
        }
    }


    void matchKeywordOrIdentifier() {
        int i;
        StringBuilder sb = new StringBuilder(L25.al);

        do {
            sb.append(ch);
            getch();
        } while (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9');
        id = sb.toString();


        i = java.util.Arrays.binarySearch(word, id);


        if (i < 0) {

            sym = Symbol.ident;
        } else {

            sym = wsym[i];
        }
    }


    void matchNumber() {
        int k = 0;
        sym = Symbol.number;
        num = 0;
        do {
            num = 10 * num + Character.digit(ch, 10);
            k++;
            getch();
        } while (ch >= '0' && ch <= '9');
        k--;
        if (k > L25.nmax)
            Err.report(30);
    }


    void matchOperator() {

        switch (ch) {
            case '=':
                getch();
                if (ch == '=') {
                    sym = Symbol.eql;
                     getch();
                } else {
                    sym = Symbol.becomes;
                }
                break;
            case '!':
                getch();
                if (ch == '=') {
                    sym = Symbol.neq;
                     getch();
                } else {
                    sym = Symbol.nul;
                }
                break;
            case '<':
                getch();
                if (ch == '=') {
                    sym = Symbol.leq;
                    getch();
                } else {
                    sym = Symbol.lss;
                }
                break;
            case '>':
                getch();
                if (ch == '=') {
                    sym = Symbol.geq;
                    getch();
                } else {
                    sym = Symbol.gtr;
                }
                break;
            default:
                sym = ssym[ch];
                getch();
                break;
        }
    }
}
