package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static io.github.mletkin.jemforth.engine.harness.Line.line;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Fixture;

// SP@ omitted, no direct Stack access is allowed
// OFFSET omitted, it is pointless in this implementation
// CONVERT omitted, it's not used for interpretation

public class Forth83Test {

    Fixture<Forth83Engine> fixture = fixture(new Forth83Engine());

    @ParameterizedTest
    @MethodSource("testData")
    public void test(String name) {
        Assertions.assertThat(fixture.engine().getDictionary().find(name))
                .as("word [" + name + "] not defined").isNotNull();
    }

    @Test
    public void isForth83() {
        fixture.test(line("FORTH-83"));
    }

    private static final String[] required = { //

            // Nucleus
            "!", "*", "*/", "*/MOD", "+", "+!", "-", "/", "/MOD", "0<", "0=", "0>", "1+", "1-", "2+", //
            "2-", "2/", "<", "=", ">", ">R", "?DUP", "@", "ABS", "AND", "C!", "C@", "CMOVE", //
            "CMOVE>", "COUNT", "D+", "D<", "DEPTH", "DNEGATE", "DROP", "DUP", "EXECUTE", //
            "EXIT", "FILL", "I", "J", "MAX", "MIN", "MOD", "NEGATE", "NOT", "OR", "OVER", "PICK", //
            "R>", "R@", "ROLL", "ROT", "SWAP", "U<", "UM*", "UM/MOD", "XOR", //

            // Device Layer
            "BLOCK", "BUFFER", "CR", "EMIT", "EXPECT", "FLUSH", "KEY", "SAVE-BUFFERS", //
            "SPACE", "SPACES", "TYPE", "UPDATE", //

            // Interpreter Layer
            "#", "#>", "#S", "#TIB", "'", "(", "-TRAILING", ".", ".(", "<#", ">BODY", ">IN", //
            "ABORT", "BASE", "BLK", /* "CONVERT", */ "DECIMAL", "DEFINITIONS", "FIND", //
            "FORGET", "FORTH", "FORTH-83", "HERE", "HOLD", "LOAD", "PAD", "QUIT", "SIGN", //
            "SPAN", "TIB", "U.", "WORD", //

            // Compiler layer
            "+LOOP", ",", ".", ":", ";", "ABORT\"", "ALLOT", "BEGIN", "COMPILE", "CONSTANT", //
            "CREATE", "DO", "DOES>", "ELSE", "IF", "IMMEDIATE", "LEAVE", "LITERAL", "LOOP", //
            "REPEAT", "STATE", "THEN", "UNTIL", "VARIABLE", "VOCABULARY", "WHILE", //
            "[']", "[COMPILE]", "]" };

    private static final String[] double_number = { //
            // Nucleus layer
            "2!", "2@", "2DROP", "2DUP", "2OVER", "2ROT", "2SWAP", "D+", "D-", "D0=", "D2/", //
            "D<", "D=", "DABS", "DMAX", "DMIN", "DNEGATE", "DU<",

            // Interpreter layer
            "D.", "D.R",

            // Compiler layer
            "2CONSTANT", "2VARIABLE" };

    private static final String[] system_ext = { //
            // Nucleus layer
            "BRANCH", "?BRANCH",

            // Interpreter layer
            "CONTEXT", "CURRENT",

            // Compiler layer
            "<MARK", "<RESOLVE", ">MARK", ">RESOLVE" };

    private static final String[] ctrl_ref = { //
            "-->", ".R", "2*", "BL", "C,", "DUMP", "EDITOR", "EMPTY-BUFFERS", "END", "ERASE", "HEX", //
            "INTERPRET", "K", "LIST", "OCTAL", /* "OFFSET", */ //
            "QUERY", "RECURSE", "SCR", /* "SP@", */ "THRU", "U.R" };

    public static Stream<String> testData() {
        return Stream.of(required, double_number, system_ext, ctrl_ref).flatMap(Stream::of);
    }

}
