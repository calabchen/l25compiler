package github.calabchen;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 　　词法分析器负责的工作是从源代码里面读取文法符号，这是L25编译器的主要组成部分之一。
 */
public class Scanner {
    /**
     * 刚刚读入的字符
     */
    private char ch = ' ';
    /**
     * 当前读入的行
     */
    private char[] line;
    /**
     * 当前行的长度（line length）
     */
    public int ll = 0;
    /**
     * 当前字符在当前行中的位置（character counter）
     */
    public int cc = 0;
    /**
     * 当前读入的符号
     */
    public Symbol sym;
    /**
     * 保留字列表（注意保留字的存放顺序）
     */
    final private String[] word;
    /**
     * 保留字对应的符号值
     */
    final private Symbol[] wsym;
    /**
     * 单字符的符号值
     */
    final private Symbol[] ssym;
    // 输入流
    final private BufferedReader in;
    /**
     * 标识符名字（如果当前符号是标识符的话）
     *
     * @see Parser
     * @see Table#enter
     */
    public String id;
    /**
     * 数值大小（如果当前符号是数字的话）
     *
     * @see Parser
     * @see Table#enter
     */
    public int num;

    /**
     * 初始化词法分析器
     *
     * @param input PL/0 源文件输入流
     */
    public Scanner(BufferedReader input) {
        in = input;

        // 设置单字符符号
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
        ssym['['] = Symbol.lbracket;
        ssym[']'] = Symbol.rbracket;
        ssym[','] = Symbol.comma;
        ssym['.'] = Symbol.period;
        ssym[';'] = Symbol.semicolon;
        ssym['='] = Symbol.becomes;
        ssym['!'] = Symbol.nul;
        ssym[':'] = Symbol.colon;

        // 设置保留字名字,按照字母顺序，便于折半查找
        word = new String[]{"else", "func", "if", "input", "let", "main", "output", "program", "return", "struct","while"};
        // 设置保留字符号
        wsym = new Symbol[L25.norw];
        wsym[0] = Symbol.elsesym;
        wsym[1] = Symbol.funcsym;
        wsym[2] = Symbol.ifsym;
        wsym[3] = Symbol.inputsym;
        wsym[4] = Symbol.letsym;
        wsym[5] = Symbol.mainsym;
        wsym[6] = Symbol.outputsym;
        wsym[7] = Symbol.programsym;
        wsym[8] = Symbol.returnsym;
        wsym[9] = Symbol.structsym;
        wsym[10] = Symbol.whilesym;
    }

    /**
     * 读取一个字符，为减少磁盘I/O次数，每次读取一行
     */
    void getch() {
        String l = "";
        try {
            if (cc == ll) {
                while (l.isEmpty()) {
                    l = in.readLine().toLowerCase() + "\n";
                }
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

    /**
     * 词法分析，获取一个词法符号，是词法分析器的重点
     */
    public void getsym() {
        // 跳过所有空白字符
        while (Character.isWhitespace(ch)) getch();
        if (ch >= 'a' && ch <= 'z') {
            // 关键字或者一般标识符
            matchKeywordOrIdentifier();
        } else if (ch >= '0' && ch <= '9') {
            // 数字
            matchNumber();
        } else {
            // 操作符
            matchOperator();
        }
    }

    /**
     * 分析关键字或者一般标识符
     */
    void matchKeywordOrIdentifier() {
        int i;
        StringBuilder sb = new StringBuilder(L25.al);
        // 首先将整个单词读出来
        do {
            sb.append(ch);
            getch();
        } while (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9');
        id = sb.toString();

        // 然后搜索是不是保留字
        i = java.util.Arrays.binarySearch(word, id);

        // 最后形成符号信息
        if (i < 0) {
            // 一般标识符
            sym = Symbol.ident;
        } else {
            // 关键字
            sym = wsym[i];
        }
    }

    /**
     * 分析数字
     */
    void matchNumber() {
        int k = 0;
        sym = Symbol.number;
        num = 0;
        do {
            num = 10 * num + Character.digit(ch, 10);
            k++;
            getch();
        } while (ch >= '0' && ch <= '9');// 获取数字的值
        k--;
        if (k > L25.nmax) {
            Err.report(30);
        }
    }

    /**
     * 分析操作符
     */
    void matchOperator() {
        switch (ch) {
            case '=':// 相等符号或者赋值符号
                getch();
                if (ch == '=') {
                    sym = Symbol.eql;
                    getch();
                } else {
                    sym = Symbol.becomes;
                }
                break;
            case '!': // 不相等符号
                getch();
                if (ch == '=') {
                    sym = Symbol.neq;
                    getch();
                } else {
                    sym = Symbol.nul;
                }
                break;

            case '<': // 小于或者小于等于
                getch();
                if (ch == '=') {
                    sym = Symbol.leq;
                    getch();
                } else {
                    sym = Symbol.lss;
                }
                break;
            case '>':// 大于或者大于等于
                getch();
                if (ch == '=') {
                    sym = Symbol.geq;
                    getch();
                } else {
                    sym = Symbol.gtr;
                }
                break;
            default:// 其他为单字符操作符（如果符号非法则返回nil）
                sym = ssym[ch];
                getch();
                break;
        }
    }
}
