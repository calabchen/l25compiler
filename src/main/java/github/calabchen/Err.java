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
        errorMessages.put(18, "unknown ident name !");
        errorMessages.put(19, "can not find this ident in table");
        errorMessages.put(20, "missing if '(' !");
        errorMessages.put(21, "missing if ')' !");
        errorMessages.put(22, "missing if '{' !");
        errorMessages.put(23, "missing if '}' !");
        errorMessages.put(24, "missing else '{' !");
        errorMessages.put(25, "missing else '}' !");
        errorMessages.put(26, "missing while '(' !");
        errorMessages.put(27, "missing while ')' !");
        errorMessages.put(28, "missing while '{' !");
        errorMessages.put(29, "missing while '}' !");
        errorMessages.put(30, "missing func call '(' !");
        errorMessages.put(31, "missing func call ')' !");
        errorMessages.put(32, "missing input '(' !");
        errorMessages.put(33, "missing input ')' !");
        errorMessages.put(34, "multipul idents in input must be devided by ','");
        errorMessages.put(35, "missing output '(' !");
        errorMessages.put(36, "missing output ')' !");
        errorMessages.put(37, "invaild bool symbol !");
        errorMessages.put(38, "missing factor '(' before expression !");
        errorMessages.put(39, "missing factor ')' after expression !");
        errorMessages.put(40, "missing ';' after return !");
        errorMessages.put(41, "invaild array length !");
        errorMessages.put(42, "missing array '[' !");
        errorMessages.put(43, "missing array ']' !");
        errorMessages.put(44, "missing array element '[' !");
        errorMessages.put(45, "missing array element ']' !");
        errorMessages.put(46, "missing struct name !");
        errorMessages.put(47, "missing struct '{' !");
        errorMessages.put(48, "missing struct '}' !");
        errorMessages.put(49, "struct length is too shart !");
        errorMessages.put(50, "struct length is too large !");
        errorMessages.put(51, "missing array length !");
        errorMessages.put(52, "Exceeded the maximum allowed level of nested declarations !");
        errorMessages.put(53, "unknown statement !");
        errorMessages.put(54, "array length is too shart !");
        errorMessages.put(55, "array length is too large !");
        errorMessages.put(56, "missing assignstmt '=' !");
        errorMessages.put(57, "invaild array index !");
        errorMessages.put(58, "need more than one struct member !");
        errorMessages.put(59, "missing struct '.' !");
        errorMessages.put(60, "need member name!");
        errorMessages.put(61, "invaild member name!");
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
