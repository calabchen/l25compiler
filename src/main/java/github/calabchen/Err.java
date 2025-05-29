package github.calabchen;

/**
 * @author calabchen
 * @since 2025/5/29
 */
public class Err {
    public static int err = 0;

	public static void report(int errcode) {
		char[] s = new char[L25.lex.cc-1];
		java.util.Arrays.fill(s, ' ');
		String space = new String(s);
		System.out.println("****" + space + "!" + errcode);
		L25.fa1.println("****" + space + "!" + errcode);
		err ++;
	}
}
