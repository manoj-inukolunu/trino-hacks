package com.facebook.presto.operator.scalar;

import org.testng.annotations.Test;

import static com.facebook.presto.operator.scalar.FunctionAssertions.assertFunction;

public class TestStringFunctions
{
    @Test
    public void testChr()
    {
        assertFunction("CHR(65)", "A");
        assertFunction("CHR(65)", "A");
        assertFunction("CHR(0)", "\0");
    }

    @Test
    public void testConcat()
    {
        assertFunction("CONCAT('hello', ' world')", "hello world");
        assertFunction("CONCAT('', '')", "");
        assertFunction("CONCAT('what', '')", "what");
        assertFunction("CONCAT('', 'what')", "what");
        assertFunction("CONCAT(CONCAT('this', ' is'), ' cool')", "this is cool");
        assertFunction("CONCAT('this', CONCAT(' is', ' cool'))", "this is cool");
    }

    @Test
    public void testLength()
    {
        assertFunction("LENGTH('')", 0);
        assertFunction("LENGTH('hello')", 5);
        assertFunction("LENGTH('Quadratically')", 13);
    }

    @Test
    public void testReverse()
    {
        assertFunction("REVERSE('')", "");
        assertFunction("REVERSE('hello')", "olleh");
        assertFunction("REVERSE('Quadratically')", "yllacitardauQ");
        assertFunction("REVERSE('racecar')", "racecar");
    }

    @Test
    public void testSubstring()
    {
        assertFunction("SUBSTR('Quadratically', 5, 6)", "ratica");
        assertFunction("SUBSTR('Quadratically', 5, 10)", "ratically");
        assertFunction("SUBSTR('Quadratically', 5, 50)", "ratically");
        assertFunction("SUBSTR('Quadratically', 50, 10)", "");
        assertFunction("SUBSTR('Quadratically', -5, 4)", "call");
        assertFunction("SUBSTR('Quadratically', -5, 40)", "cally");
        assertFunction("SUBSTR('Quadratically', -50, 4)", "");
        assertFunction("SUBSTR('Quadratically', 0, 4)", "");
        assertFunction("SUBSTR('Quadratically', 5, 0)", "");
    }

    @Test
    public void testLeftTrim()
    {
        assertFunction("LTRIM('')", "");
        assertFunction("LTRIM('   ')", "");
        assertFunction("LTRIM('  hello  ')", "hello  ");
        assertFunction("LTRIM('  hello')", "hello");
        assertFunction("LTRIM('hello  ')", "hello  ");
        assertFunction("LTRIM(' hello world ')", "hello world ");
    }

    @Test
    public void testRightTrim()
    {
        assertFunction("RTRIM('')", "");
        assertFunction("RTRIM('   ')", "");
        assertFunction("RTRIM('  hello  ')", "  hello");
        assertFunction("RTRIM('  hello')", "  hello");
        assertFunction("RTRIM('hello  ')", "hello");
        assertFunction("RTRIM(' hello world ')", " hello world");
    }

    @Test
    public void testTrim()
    {
        assertFunction("TRIM('')", "");
        assertFunction("TRIM('   ')", "");
        assertFunction("TRIM('  hello  ')", "hello");
        assertFunction("TRIM('  hello')", "hello");
        assertFunction("TRIM('hello  ')", "hello");
        assertFunction("TRIM(' hello world ')", "hello world");
    }

    @Test
    public void testLower()
    {
        assertFunction("LOWER('')", "");
        assertFunction("LOWER('Hello World')", "hello world");
        assertFunction("LOWER('WHAT!!')", "what!!");
    }

    @Test
    public void testUpper()
    {
        assertFunction("UPPER('')", "");
        assertFunction("UPPER('Hello World')", "HELLO WORLD");
        assertFunction("UPPER('what!!')", "WHAT!!");
    }
}
