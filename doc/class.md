# L25 编译器组成部分文档

## 目录
- [Scanner 类](#scanner-class) - 负责词法分析
- [Parser 类](#parser-class) - 负责语法分析
- [Table 类](#table-class) - 符号表管理
- [Interpreter 类](#interpreter-class) - 代码解释器与代码生成
- [GUI 类](#gui-class) - 用户界面组件
- [L25 类](#l25-class) - 命令行运行界面组件
- [SymSet 类](#symset-class) - 符号集合工具类
- [Err 类](#err-class) - 错误报告机制

---

## Scanner 类

```java
/**
 * 　各种符号的编码
 */
public enum Symbol {
    nul, ident, number,

    // 运算符
    plus, minus, times, slash,
    eql, neq, lss, leq, gtr, geq,

    // 括号
    lparen, rparen, lbrace, rbrace, lbracket, rbracket,

    // 分隔符
    comma, semicolon, period,

    // 特殊符号
    becomes,
    colon,

    // 关键字
    programsym, mainsym, funcsym,
    ifsym, elsesym, whilesym, inputsym, outputsym,
    letsym, returnsym, structsym
}

/**
 *　　词法分析器负责的工作是从源代码里面读取文法符号，这是PL/0编译器的主要组成部分之一。
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
	private String[] word;
	
	/**
	 * 保留字对应的符号值
	 */
	private Symbol[] wsym;
	
	/**
	 * 单字符的符号值
	 */
	private Symbol[] ssym;

	// 输入流
	private BufferedReader in;

	/**
	 * 标识符名字（如果当前符号是标识符的话）
	 */
	public String id;

	/**
	 * 数值大小（如果当前符号是数字的话）
	 */
	public int num;
	
	/**
	 * 初始化词法分析器
	 * @param input L25 源文件输入流
	 */
	public Scanner(BufferedReader input) {}
	
	/**
	 * 读取一个字符，为减少磁盘I/O次数，每次读取一行
	 */
	void getch() {}
	
	/**
	 * 词法分析，获取一个词法符号，是词法分析器的重点
	 */
	public void getsym() {}
	
	/**
	 * 分析关键字或者一般标识符
	 */
	void matchKeywordOrIdentifier() {}
	
	/**
	 * 分析数字
	 */
	void matchNumber() {}
	
	/**
	 * 分析操作符
	 */
	void matchOperator() {}	
}
```

[返回顶部](#l25-编译器组成部分文档)

---

## Parser 类

```java
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
    public Parser(Scanner l, Table t, Interpreter i) {}

    /**
     * 启动语法分析过程，此前必须先调用一次nextsym()
     *
     * @see #nextSym()
     */
    public void parse() {}


    /**
     * 获得下一个语法符号，这里只是简单调用一下getsym()
     */
    public void nextSym() {}

    /**
     * 分析<主程序>
     * lev:    当前分程序所在层
     * tx:     符号表当前尾指针
     * fsys:   当前模块后继符号集合
     */
    public void parseProgram(int lev, SymSet fsys) {}

    private void parseStructDeclaration(int lev, SymSet fsys) {}

    private void parseMemberList(int lev, String name, SymSet fsys) {}

    private void parseMemberDeclaration(int lev, String name, SymSet fsys) {}

    private void parseFuncDeclaration(int lev, SymSet fsys) {}

    private void parseParamList(int lev, int tx0, SymSet fsys) {}

    private void parseStatementList(int lev, SymSet fsys) {}

    private void parseStatement(int lev, SymSet fsys) {}

    private void parseOutputStatement(int lev, SymSet fsys) {}

    private void parseInputStatement(int lev, SymSet fsys) {}

    private void parseFuncCallStatement(int lev, SymSet fsys) {}

    private void parseArgListStatement(int lev, SymSet fsys) {}

    private void parseWhileStatement(int lev, SymSet fsys) {}

    private void parseIfStatement(int lev, SymSet fsys) {}

    private void parseBoolExpression(int lev, SymSet fsys) {}

    private void parseAssignStatement(int lev, SymSet fsys) {}

    private void parseArrayRef(int lev, SymSet fsys) {}

    private void parseStructRef(int lev, SymSet fsys) {}

    private void parseDeclareStatement(int lev, SymSet fsys) {}

    private void parseStructInitExpression(int lev, int i, SymSet fsys) {}

    private void parseArrayInitExpression(int lev, int i, SymSet fsys) {}

    private void parseExpression(int lev, SymSet fsys) {}

    /**
     * 分析<项>
     *
     * @param fsys 后跟符号集
     * @param lev  当前层次
     */
    private void parseTerm(int lev, SymSet fsys) {}

    /**
     * 分析<因子>
     *
     * @param fsys 后跟符号集
     * @param lev  当前层次
     */
    private void parseFactor(int lev, SymSet fsys) {}
}

```

[返回顶部](#l25-编译器组成部分文档)

---

## Table 类

```java
/**
 * 符号类型，为避免和Java的关键字Object冲突，改成Objekt
 */
enum Objekt {
    variable, function, mainfunc, array, struct
}

/**
 * 　　这个类封装了L25编译器的符号表
 */
public class Table {
    public static class Item {
        String name;        // 名字
        Objekt kind;        // 类型:  var or function or array or struct
        int level;          // 所处层
        int adr;            // 地址
        int size;           // 需要分配的数据区空间
        int paramsize;      // 形参个数，仅function使用
        HashMap<String, Item> memberList = new HashMap<>(L25.membermax);     // 成员个数，仅struct使用
        boolean structDeclared = false;  // 结构体使用，判断是结构体声明还是对象
    }

    /**
     * 名字表，请使用get()函数访问
     *
     * @see #get(int)
     */
    private final Item[] table = new Item[L25.txmax];

    /**
     * 当前名字表项指针，也可以理解为当前有效的名字表大小（table size）
     */
    public int tx = 0;

    /**
     * 获得名字表某一项的内容
     *
     * @param i 名字表中的位置
     * @return 名字表第 i 项的内容
     */
    public Item get(int i) {}

    /**
     * 把某个符号登陆到名字表中
     *
     * @param k   该符号的类型：const, var, procedure
     * @param lev 名字所在的层次
     * @param dx  当前应分配的变量的相对地址，注意调用enter()后dx要加一
     */
    public void enter(Objekt k, int lev, int dx) {}

    /**
     * 打印符号表内容
     *
     * @param start 当前作用域符号表区间的左端
     */
    public void debugTable(int start) {}

    /**
     * 在名字表中查找某个名字的位置
     *
     * @param idt 要查找的名字
     * @return 如果找到则返回名字项的下标，否则返回0
     */
    public int position(String idt) {}
}
```

[返回顶部](#l25-编译器组成部分文档)

---

## Interpreter 类

```java
/**
 * 类P-Code指令类型
 */
enum Fct {
    LIT, OPR, LOD, STO, CAL, INT, JMP, JPC, RET, HLT, STP, LOS
}

/**
 * 　　这个类对应C语言版本中的 fct 枚举类型和 instruction 结构，代表虚拟机指令
 */
class Instruction {
    /**
     * 虚拟机代码指令
     */
    public Fct f;

    /**
     * 引用层与声明层的层次差
     */
    public int l;

    /**
     * 指令参数
     */
    public int a;
}

/**
 * 　　类P-Code代码解释器（含代码生成函数），这个类包含了C语言版中两个重要的全局变量 cx 和 code
 */
public class Interpreter {
    // 解释执行时使用的栈大小
    final int stacksize = 500;
    
    final int STACK_MAX = 999;

    /**
     * 虚拟机代码指针，取值范围[0, cxmax-1]
     */
    public int cx = 0;

    /**
     * 存放虚拟机代码的数组
     */
    public Instruction[] code = new Instruction[L25.cxmax];

    /**
     * 生成虚拟机代码
     *
     * @param x instruction.f
     * @param y instruction.l
     * @param z instruction.a
     */
    public void gen(Fct x, int y, int z) {}

    /**
     * 输出目标代码清单
     *
     * @param start 开始输出的位置
     */
    public void listcode(int start) {}

    /**
     * 解释程序
     */
    public void interpret() {}

    /**
     * 通过给定的层次差来获得该层的堆栈帧基地址
     *
     * @param l 目标层次与当前层次的层次差
     * @param s 运行栈
     * @param b 当前层堆栈帧基地址
     * @return 目标层次的堆栈帧基地址
     */
    private int base(int l, int[] s, int b) {}
}
```

[返回顶部](#l25-编译器组成部分文档)

---

## GUI 类

```java
public class GUI extends JFrame {

    private JPanel mainPanel;
    private JTextArea tmpsWindow;
    private JButton pcodeButton;
    private JButton symbolButton;
    private JButton analysisButton;
    private JTextArea outputWindow;
    private JButton loadFileButton;
    private JButton saveFileButton;
    private JTextField inputFileNameField;
    private JTextArea codeWindow;
    private JButton compileButton;
    private JCheckBox listObjectCodeCheckBox;
    private JCheckBox listSymbolTableCheckBox;
    private JButton runButton;
    private JPanel rightPanel;
    private JPanel rightTopPanel;
    private JPanel leftPanel;
    private JPanel leftTopPanel;
    private JPanel codeRunnerPanel;
    private JScrollPane codeScrollPanel;
    private JScrollPane tmpsScrollPanel;

    public GUI() {
        L25.projectRoot = System.getProperty("user.dir");
        L25.testDir = Paths.get(L25.projectRoot, "l25testcode").toString();

        $$$setupUI$$$(); // 自动从 .form 文件加载 UI 布局
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setVisible(true);
        // 添加事件监听器
        addEventListeners();
    }

    private void $$$setupUI$$$() {
    }

    private void addEventListeners() {}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }

}
```

[返回顶部](#l25-编译器组成部分文档)

---

## L25 Class
```java

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
    public static final int arraymax = 100;    // 数组最大长度
    public static final int membermax = 10;    // 结构体最大成员数

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

    // 设置项目预先设置的目录和文件名
    public static String projectRoot;
    public static String testDir;

    public static String docDir;

    /**
     * 构造函数，初始化编译器所有组成部分
     *
     * @param fin L25 源文件的输入流
     */
    public L25(BufferedReader fin) {}

    /**
     * 执行编译动作
     *
     * @return 是否编译成功
     */
    boolean compile() {}

    /**
     * 主函数
     */
    public static void main(String[] args) {}
}
```

[返回顶部](#l25-编译器组成部分文档)

---

## SymSet 类

```java

/**
 * 把 java.util.BitSet 包装一下，以便于编写代码
 */
public class SymSet extends BitSet {
    /**
     * 这个域没有特别意义
     */
    private static final long serialVersionUID = 8136959240158320958L;

    /**
     * 构造一个符号集合
     *
     * @param nbits 这个集合的容量
     */
    public SymSet(int nbits) {}

    /**
     * 把一个符号放到集合中
     *
     * @param s 要放置的符号
     */
    public void set(Symbol s) {}

    /**
     * 检查一个符号是否在集合中
     *
     * @param s 要检查的符号
     * @return 若符号在集合中，则返回true，否则返回false
     */
    public boolean get(Symbol s) {}
}
```

[返回顶部](#l25-编译器组成部分文档)

---

## Err 类

```java
/**
 *　　这个类包含了报错函数以及错误计数器。
 */
public class Err {
	/**
	 * 错误计数器，编译过程中一共有多少个错误
	 */
	public static int err = 0;
	
	/**
	 * 报错函数
	 * @param errcode 错误码
	 */
	public static void report(int errcode) {}
}
```

[返回顶部](#l25-编译器组成部分文档)

---
