package github.calabchen;

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

    public Instruction() {
    }

    public Instruction(Fct f, int l, int a) {
        this.f = f;
        this.l = l;
        this.a = a;
    }

    @Override
    public String toString() {
        return f + " " + l + " " + a;
    }
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
    public void gen(Fct x, int y, int z) {
        if (cx >= L25.cxmax) {
            throw new Error("Program too long");
        }

        code[cx] = new Instruction();
        code[cx].f = x;
        code[cx].l = y;
        code[cx].a = z;
        cx++;
//        System.out.println("cx= " + cx);
    }

    /**
     * 输出目标代码清单
     *
     * @param start 开始输出的位置
     */
    public void listcode(int start) {
        if (L25.listswitch) {
            for (int i = start; i < cx; i++) {
                String msg = i + " " + code[i].f + " " + code[i].l + " " + code[i].a;
                System.out.println(msg);
                L25.fa.println(msg);
            }
        }
    }

    /**
     * 解释程序
     */
    public void interpret() {
        int p, b, t;                        // 指令指针，指令基址，栈顶指针
        Instruction i;                      // 存放当前指令
        int[] s = new int[stacksize];       // 栈

        System.out.println("start L25");
//        t = b = p = 0;
        p = 0;
        b = 1;
        t = 0;
        s[0] = 0; // s[0]不用
        // main program's three link units set to 0
        s[1] = s[2] = s[3] = 0;
        do {
            i = code[p];                    // 读当前指令
            p++;
            switch (i.f) {
                case LIT:                // 将常量a的值取到栈顶
                    if (t + 1 > STACK_MAX) throw new RuntimeException("Stack overflow");
                    t++;
                    s[t] = i.a;
                    break;
                case OPR:                // 数学、逻辑运算
                    switch (i.a) {
//                        case 0:
//                            t = b;
//                            p = s[t + 2];
//                            b = s[t + 1];
//                            break;
                        case 1:         // 栈顶元素取反
                            s[t - 1] = -s[t - 1];
                            break;
                        case 2:         // 次栈顶项加上栈顶项，退两个栈元素，相加值进栈
                            t--;
                            s[t] = s[t] + s[t + 1];
                            break;
                        case 3:
                            t--;        // 次栈顶项减去栈顶项，退两个栈元素，相减值进栈
                            s[t] = s[t] - s[t + 1];
                            break;
                        case 4:         // 次栈顶项乘以栈顶项
                            t--;
                            s[t] = s[t] * s[t + 1];
                            break;
                        case 5:         // 次栈顶项除以栈顶项
                            t--;
                            if (s[t + 1] == 0) {
                                throw new RuntimeException("Division by zero");
                            }
                            s[t] = s[t] / s[t + 1];
                            break;
                        case 6:         // 次栈顶项取余栈顶项
                            s[t] = s[t] % 2;
                            break;
                        case 8:         // 次栈顶项与栈顶项是否相等
                            t--;
                            s[t] = (s[t] == s[t + 1] ? 1 : 0);
                            break;
                        case 9:         // 次栈顶项与栈顶项是否不等
                            t--;
                            s[t] = (s[t] != s[t + 1] ? 1 : 0);
                            break;
                        case 10:        // 次栈顶项是否小于栈顶项
                            t--;
                            s[t] = (s[t] < s[t + 1] ? 1 : 0);
                            break;
                        case 11:        // 次栈顶项是否大于等于栈顶项
                            t--;
                            s[t] = (s[t] >= s[t + 1] ? 1 : 0);
                            break;
                        case 12:        // 次栈顶项是否大于栈顶项
                            t--;
                            s[t] = (s[t] > s[t + 1] ? 1 : 0);
                            break;
                        case 13:         // 次栈顶项是否小于等于栈顶项
                            t--;
                            s[t] = (s[t] <= s[t + 1] ? 1 : 0);
                            break;
                        case 14:         // 栈顶值输出
                            System.out.print(s[t]);
                            L25.fa2.print(s[t]);
                            t--;
                            break;
                        case 15:        // 输出换行符
                            System.out.println();
                            L25.fa2.println();
                            break;
                        case 16:        // 读入一个输入置于栈顶
                            if (t + 1 > STACK_MAX) throw new RuntimeException("Stack overflow");
                            System.out.print("input?");
                            L25.fa2.print("input?");
                            int inputValue;
                            try {
                                inputValue = Integer.parseInt(L25.stdin.readLine());
                            } catch (Exception e) {
                                System.out.println("Invalid number");
                                inputValue = 0;
                            }
                            L25.fa2.println(inputValue);
                            s[++t] = inputValue;  // 输入值压栈
                            break;
                    }
                    break;
                case LOD:                // 取相对当前过程的数据基地址为a的内存的值到栈顶
                    t++;
                    s[t] = s[base(i.l, s, b) + i.a];
                    break;
                case STO:                // 栈顶的值存到相对当前过程的数据基地址为a的内存
                    s[base(i.l, s, b) + i.a] = s[t];
                    t--;
                    break;
                case CAL:// 调用子过程
                    for (int j = i.l; j > 0; j--) {
                        s[t - i.l + 3 + j] = s[t - i.l + j];
                    }
                    t = t - i.l;
                    s[t + 1] = b;
                    s[t + 2] = b;
                    s[t + 3] = p;
                    b = t + 1;
                    p = i.a;
                    break;
                case INT:            // 分配内存
                    if (i.a > 0 && t + i.a > STACK_MAX) throw new RuntimeException("Stack overflow");
                    t += i.a;
                    break;
                case JMP:                // 直接跳转
                    p = i.a;
                    break;
                case JPC:                // 条件跳转（当栈顶为0的时候跳转）
                    if (s[t] == 0)
                        p = i.a;
                    t--;
                    break;
                case RET:               // 函数调用结束后返回
                    if (i.a == 1) {  // 主函数返回
                        t = b - 1;
                        b = s[t + 2];
                    } else {  // 普通函数返回
                        int returnValue = s[t]; // 保存返回值
                        // 恢复调用者环境
                        int newBase = s[b + 1]; // 动态链（调用者基址）
                        int returnAddress = s[b + 2]; // 返回地址
                        // 计算调用者栈顶位置（调用前栈顶 + 返回值）
                        t = b;
                        s[t] = returnValue; // 将返回值放到调用者栈顶
                        b = newBase; // 恢复调用者基址
                        p = returnAddress; // 设置返回地址
                    }
                    break;
                case HLT:
                    return;
                case STP:             // 将次栈顶值作为地址，将栈顶的值存储到该地址
//                    if (s[t - 1] + b > i.a + i.l || s[t - 1] + b < i.a) {
//                        Err.report(57);  // 数组越界
//                    }
//                    s[s[t - 1] + b] = s[t];
//                    t--;
                    int level_diff1 = i.l;
                    int value1 = s[t];
                    int offset1 = s[t - 1];
                    t -= 2;
                    int frame_base1 = base(level_diff1, s, b);
                    s[frame_base1 + offset1] = value1;
                    break;
                case LOS:             // l 表示数组长度，a表示数组基址
//                    if (s[t] + b > i.a + i.l || s[t] + b <= i.a) {
//                        Err.report(57); // 数组越界
//                    }
//                    int tmp = s[t];
//                    t++;
//                    s[t] = s[tmp + b];
                    int level_diff2 = i.l;
                    int offset2 = s[t];
                    int frame_base2 = base(level_diff2, s, b);
                    s[t] = s[frame_base2 + offset2];
                    break;
            }
        } while (p != 0);
    }

    /**
     * 通过给定的层次差来获得该层的堆栈帧基地址
     *
     * @param l 目标层次与当前层次的层次差
     * @param s 运行栈
     * @param b 当前层堆栈帧基地址
     * @return 目标层次的堆栈帧基地址
     */
    private int base(int l, int[] s, int b) {
        int b1 = b;
        while (l > 0) {
            b1 = s[b1];
            l--;
        }
        return b1;
    }

    public void setCode(int addr, Fct f, int l, int a) {
        if (addr >= 0 && addr < code.length) {
            code[addr] = new Instruction(f, l, a);
        } else {
            System.err.println("Error: setCode address out of bounds: " + addr);
        }
    }

}
