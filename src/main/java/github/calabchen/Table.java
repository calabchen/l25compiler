package github.calabchen;


enum Objekt {
    constant, variable, procedure
}


public class Table {

    public class Item {
        String name;
        Objekt kind;
        int val;
        int level;
        int adr;
        int size;
    }


    private Item[] table = new Item[L25.txmax];


    public int tx = 0;


    public Item get(int i) {
        if (table[i] == null) {
            table[i] = new Item();
            table[i].name = "";
        }
        return table[i];
    }


    public void enter(Objekt k, int lev, int dx) {
        tx++;
        Item item = get(tx);
        item.name = L25.lex.id;
        item.kind = k;
        switch (k) {
            case constant:
                if (L25.lex.num > L25.amax) {
                    Err.report(31);
                    item.val = 0;
                } else {
                    item.val = L25.lex.num;
                }
                break;
            case variable:
                item.level = lev;
                item.adr = dx;
                break;
            case procedure:
                item.level = lev;
                break;
        }
    }


    public void debugTable(int start) {
        if (!L25.tableswitch)
            return;
        System.out.println("TABLE:");
        if (start >= tx)
            System.out.println("    NULL");
        for (int i = start + 1; i <= tx; i++) {
            String msg = "OOPS! UNKNOWN TABLE ITEM!";
            switch (table[i].kind) {
                case constant:
                    msg = "    " + i + " const " + table[i].name + " val=" + table[i].val;
                    break;
                case variable:
                    msg = "    " + i + " var   " + table[i].name + " lev=" + table[i].level + " addr=" + table[i].adr;
                    break;
                case procedure:
                    msg = "    " + i + " proc  " + table[i].name + " lev=" + table[i].level + " addr=" + table[i].adr + " size=" + table[i].size;
                    break;
            }
            System.out.println(msg);
            L25.fas.println(msg);
        }
        System.out.println();
    }

    public int position(String idt) {
        for (int i = tx; i > 0; i--)
            if (get(i).name.equals(idt))
                return i;

        return 0;
    }
}
