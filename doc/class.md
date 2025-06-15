# Class

## Err Class
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

## Scanner Class
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

## Table Class
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
        Objekt kind;        // 类型:  var or function
        int level;          // 所处层
        int adr;            // 地址
        int size;           // 需要分配的数据区空间
        int length;         // 参数个数或者数组长度
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

## Interpreter class
```java
/**
 * 类P-Code指令类型
 */
enum Fct {
    LIT, OPR, LOD, STO, CAL, INT, JMP, JPC, RET
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
