package github.calabchen;

/**
 * 符号类型，为避免和Java的关键字Object冲突，改成Objekt
 */
enum Objekt {
    variable, function, mainfunc
}

/**
 * 　　这个类封装了L25编译器的符号表
 */
public class Table {
    public static class Item {
        String name;        // 名字
        Objekt kind;        // 类型: const, var, array, struct or function
        int level;          // 所处层
        int adr;            // 地址
        int size;           // 需要分配的数据区空间
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
    public Item get(int i) {
        if (table[i] == null) {
            table[i] = new Item();
            table[i].name = "";
        }
        return table[i];
    }

    /**
     * 把某个符号登陆到名字表中
     *
     * @param k   该符号的类型：const, var, procedure
     * @param lev 名字所在的层次
     * @param dx  当前应分配的变量的相对地址，注意调用enter()后dx要加一
     */
    public void enter(Objekt k, int lev, int dx) {
        tx++;
        Item item = get(tx);
        item.name = L25.lex.id;                 // 注意id和num都是从词法分析器获得
        item.kind = k;
        switch (k) {
            case variable:                      // 变量名字
                item.level = lev;
                item.adr = dx;
                break;
            case function:                      // 函数名字
                item.level = lev;
                break;
            case mainfunc:                      // 函数名字
                item.level = lev;
                break;
        }
    }

    /**
     * 打印符号表内容
     *
     * @param start 当前作用域符号表区间的左端
     */
    public void debugTable(int start) {
        if (!L25.tableswitch)
            return;
        System.out.println("TABLE:");
        if (start >= tx)
            System.out.println("    NULL");

        for (int i = start + 1; i <= tx; i++) {
            Item currentItem = table[i];

            String kindString;
            String name = currentItem.name;

            switch (currentItem.kind) {
                case variable:
                    kindString = "var";
                    // 格式：序号 | kind | name | lev=level | addr=adr
                    String varLine = String.format("%2d %5s %-5s lev=%2d addr=%2d",
                            i, kindString, name, currentItem.level, currentItem.adr);
                    System.out.println(varLine);
                    L25.fas.println(varLine);
                    break;
                case function:
                    kindString = "func";
                    // 格式：序号 | kind | name | lev=level | size=size
                    String funcLine = String.format("%2d %5s %-5s lev=%2d size=%2d",
                            i, kindString, name, currentItem.level, currentItem.size);
                    System.out.println(funcLine);
                    L25.fas.println(funcLine);
                    break;
                case mainfunc:
                    kindString = "main";
                    // 格式：序号 | kind | name | lev=level | size=size
                    String mainLine = String.format("%2d %5s %-5s lev=%2d size=%2d",
                            i, kindString, name, currentItem.level, currentItem.size);
                    System.out.println(mainLine);
                    L25.fas.println(mainLine);
                    break;
                default:
                    String msg = String.format("%2d OOPS! UNKNOWN ITEM! Kind: %s, Name: %s", i, currentItem.kind, currentItem.name);
                    System.out.println(msg);
                    L25.fas.println(msg);
                    break;
            }
        }
        System.out.println();
    }

    /**
     * 在名字表中查找某个名字的位置
     *
     * @param idt 要查找的名字
     * @return 如果找到则返回名字项的下标，否则返回0
     */
    public int position(String idt) {
        for (int i = tx; i > 0; i--)
            if (get(i).name.equals(idt))
                return i;

        return 0;
    }
}
