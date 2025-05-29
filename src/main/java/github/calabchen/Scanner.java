package github.calabchen;

import java.io.BufferedReader;
import java.io.IOException;


public class Scanner {

    private char ch = ' ';


    private char[] line;

    public int ll = 0;


    public int cc = 0;


    public Symbol sym;

    private String[] word;


    private Symbol[] wsym;


    private Symbol[] ssym;

    private BufferedReader in;

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
        ssym['='] = Symbol.eql;
        ssym[','] = Symbol.comma;
        ssym['.'] = Symbol.period;
        ssym['#'] = Symbol.neq;
        ssym[';'] = Symbol.semicolon;


        word = new String[]{"begin", "call", "const", "do", "end", "if",
                "odd", "procedure", "read", "then", "var", "while", "write"};


        wsym = new Symbol[L25.norw];
        wsym[0] = Symbol.beginsym;
        wsym[1] = Symbol.callsym;
        wsym[2] = Symbol.constsym;
        wsym[3] = Symbol.dosym;
        wsym[4] = Symbol.endsym;
        wsym[5] = Symbol.ifsym;
        wsym[6] = Symbol.oddsym;
        wsym[7] = Symbol.procsym;
        wsym[8] = Symbol.readsym;
        wsym[9] = Symbol.thensym;
        wsym[10] = Symbol.varsym;
        wsym[11] = Symbol.whilesym;
        wsym[12] = Symbol.writesym;
    }


    void getch() {
        String l = "";
        try {
            if (cc == ll) {
                while (l.equals(""))
                    l = in.readLine().toLowerCase() + "\n";
                ll = l.length();
                cc = 0;
                line = l.toCharArray();
                System.out.println(L25.interp.cx + " " + l);
                L25.fa1.println(L25.interp.cx + " " + l);
            }
        } catch (IOException e) {
            throw new Error("program imcomplete");
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
            case ':':
                getch();
                if (ch == '=') {
                    sym = Symbol.becomes;
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
                if (sym != Symbol.period)
                    getch();
                break;
        }
    }
}
