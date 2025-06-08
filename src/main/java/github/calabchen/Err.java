package github.calabchen;

import java.util.HashMap;
import java.util.Map;

/**
 * @author calabchen
 * @since 2025/5/29
 * 这个类只是包含了报错函数以及错误计数器。
 */
public class Err {
    /**
     * 错误计数器，编译过程中一共有多少个错误
     */
    public static int err = 0;

    /**
     * 错误码到错误信息的映射
     */
    private static final Map<Integer, String> errorMessages = new HashMap<>();

    static {
        // 初始化错误信息
        errorMessages.put(1, "missing program !");
        errorMessages.put(2, "missing program name !");
        errorMessages.put(3, "missing program '{' !");
        errorMessages.put(4, "missing program '}' !");
        errorMessages.put(5, "missing function name !");
        errorMessages.put(6, "missing function '(' !");
        errorMessages.put(7, "missing function ')' !");
        errorMessages.put(8, "missing function '{' !");
        errorMessages.put(10, "missing function '}' !");
        errorMessages.put(11, "function must have one statement !");
        errorMessages.put(12, "function must have return !");
        errorMessages.put(13, "multipul idents in function param_list must be devided by ',' !");
        errorMessages.put(14, "statement should be ended with ';' !");
        errorMessages.put(15, "program must have main function !");
        errorMessages.put(16, "missing main '{' !");
        errorMessages.put(17, "missing main '}' !");
        errorMessages.put(18, "can not know this ident is variable or function name !");
        errorMessages.put(19, "can not find this ident in table");
        errorMessages.put(20, "");
        errorMessages.put(21, "");
        errorMessages.put(22, "");
        errorMessages.put(23, "");
        errorMessages.put(24, "");
        errorMessages.put(25, "");
        errorMessages.put(26, "");
        errorMessages.put(27, "");
        errorMessages.put(28, "");
        errorMessages.put(30, "");
        errorMessages.put(31, "");
        errorMessages.put(32, "");

    }

    /**
     * 报错函数
     *
     * @param errcode 错误码
     */
    public static void report(int errcode) {
        char[] s = new char[L25.lex.cc - 1];
        java.util.Arrays.fill(s, ' ');
        String space = new String(s);

        String errorMessage = errorMessages.getOrDefault(errcode, "Unknown error.");

        System.out.println(errorMessage + space + "!" + errcode);
        L25.fa1.println(errorMessage + space + "!" + errcode);
        err++;
    }
}
