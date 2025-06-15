package github.calabchen;

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
