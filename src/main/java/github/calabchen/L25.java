package github.calabchen;

import java.io.*;


public class L25 {

    public static final int al = 10;
    public static final int amax = 2047;
    public static final int cxmax = 500;
    public static final int levmax = 3;
    public static final int nmax = 14;
    public static final int norw = 32;
    public static final int txmax = 100;

    public static PrintStream fa;
    public static PrintStream fa1;
    public static PrintStream fa2;
    public static PrintStream fas;
    public static boolean listswitch;
    public static boolean tableswitch;


    public static Scanner lex;
    public static Parser parser;
    public static Interpreter interp;
    public static Table table;
    public static BufferedReader stdin;


    public L25(BufferedReader fin) {

        table = new Table();
        interp = new Interpreter();
        lex = new Scanner(fin);
        parser = new Parser(lex, table, interp);
    }


    boolean compile() {
        boolean abort = false;

        try {
            L25.fa = new PrintStream("fa.tmp");
            L25.fas = new PrintStream("fas.tmp");
            parser.nextSym();
            parser.parse();
        } catch (Error e) {

            abort = true;
        } catch (IOException e) {
        } finally {
            L25.fa.close();
            L25.fa1.close();
            L25.fas.close();
        }
        if (abort)
            System.exit(0);


        return (Err.err == 0);
    }


    public static void main(String[] args) {

        String fname = "";
        stdin = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader fin;
        try {

            fname = "";
            System.out.print("Input L25 file?   ");
            while (fname.isEmpty())
                fname = stdin.readLine();
            fin = new BufferedReader(new FileReader(fname), 4096);


            fname = "";
            System.out.print("List object code?(Y/N)");
            while (fname.isEmpty())
                fname = stdin.readLine();
            L25.listswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');


            fname = "";
            System.out.print("List symbol table?(Y/N)");
            while (fname.isEmpty())
                fname = stdin.readLine();
            L25.tableswitch = (fname.charAt(0) == 'y' || fname.charAt(0) == 'Y');

            L25.fa1 = new PrintStream("fa1.tmp");
            L25.fa1.println("Input L25 file?   " + fname);


            L25 l25 = new L25(fin);

            if (l25.compile()) {

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
