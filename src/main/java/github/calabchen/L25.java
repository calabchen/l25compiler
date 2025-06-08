package github.calabchen;

import java.io.*;

/**
 * 这个版本的 L25语言 编译器根据 C/java 语言的 PL0 版本改写而成。
 */
public class L25 {
    // 编译程序的常数
    public static final int al = 10;        // 符号的最大长度
    public static final int amax = 2047;    // 最大允许的数值
    public static final int cxmax = 500;    // 最多的虚拟机代码数
    public static final int levmax = 3;     // 最大允许过程嵌套声明层数 [0, levmax]
    public static final int nmax = 14;      // number的最大位数
    public static final int norw = 32;      // 关键字个数
    public static final int txmax = 100;    // 名字表容量

    // 一些全局变量，其他关键的变量分布如下：
    // cx, code : Interpreter
    // dx : Parser
    // tx, table : Table
    public static PrintStream fa;           // 输出虚拟机代码
    public static PrintStream fa1;          // 输出源文件及其各行对应的首地址
    public static PrintStream fa2;          // 输出结果
    public static PrintStream fas;          // 输出名字表
    public static boolean listswitch;       // 显示虚拟机代码与否
    public static boolean tableswitch;      // 显示名字表与否

    // 一个典型的编译器的组成部分
    public static Scanner lex;              // 词法分析器
    public static Parser parser;            // 语法分析器
    public static Interpreter interp;       // 类P-Code解释器以及目标代码生成工具
    public static Table table;              // 名字表

    // 为避免多次创建BufferedReader，使用全局统一的标准输入
    public static BufferedReader stdin;

    /**
     * 构造函数，初始化编译器所有组成部分
     *
     * @param fin L25 源文件的输入流
     */
    public L25(BufferedReader fin) {
        table = new Table();
        interp = new Interpreter();
        lex = new Scanner(fin);
        parser = new Parser(lex, table, interp);
    }

    /**
     * 执行编译动作
     *
     * @return 是否编译成功
     */
    boolean compile() {
        boolean abort = false;

        try {
            L25.fa = new PrintStream("fa.tmp");
            L25.fas = new PrintStream("fas.tmp");
            parser.nextSym();           // 前瞻分析需要预先读入一个符号
            parser.parse();             // 开始语法分析过程（连同语法检查、目标代码生成）
        } catch (Error e) {
            // 如果是发生严重错误则直接中止
            abort = true;
        } catch (IOException e) {
        } finally {
            L25.fa.close();
            L25.fa1.close();
            L25.fas.close();
        }
        if (abort) {
            System.exit(0);
        }

        // 编译成功是指完成编译过程并且没有错误
        return (Err.err == 0);
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {

        String fname = "";
        stdin = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader fin;
        try {
            // 输入文件名
            fname = "D:\\Code\\l25compiler\\src\\main\\java\\github\\calabchen\\";
            String ftest = "";

            System.out.print("Input L25 file?   ");
            while (ftest.isEmpty())
                ftest = stdin.readLine();
            String all_file = fname + ftest;
            fin = new BufferedReader(new FileReader(all_file), 4096);

            // 是否输出虚拟机代码
            fname = "";
            System.out.print("List object code?(Y/N)");
            while (fname.isEmpty())
                fname = stdin.readLine();
            L25.listswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');

            // 是否输出名字表
            fname = "";
            System.out.print("List symbol table?(Y/N)");
            while (fname.isEmpty())
                fname = stdin.readLine();
            L25.tableswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');

            L25.fa1 = new PrintStream("fa1.tmp");
            L25.fa1.println("Input L25 file?   " + fname);

            // 构造编译器并初始化
            L25 l25 = new L25(fin);

            if (l25.compile()) {
                // 如果成功编译则接着解释运行
                L25.fa2 = new PrintStream("fa2.tmp");
                interp.interpret();
                L25.fa2.close();
            } else {
                System.out.print("Errors in L25 program");
            }
        } catch (IOException e) {
            System.out.println("Can't open file!");
        }

        System.out.println();
    }
}
