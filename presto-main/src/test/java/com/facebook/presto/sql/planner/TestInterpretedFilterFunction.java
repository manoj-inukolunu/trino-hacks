/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.sql.planner;

import com.facebook.presto.metadata.TestingMetadata;
import com.facebook.presto.operator.Input;
import com.facebook.presto.sql.analyzer.Session;
import com.facebook.presto.sql.analyzer.Symbol;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.ComparisonExpression;
import com.facebook.presto.sql.tree.Expression;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static com.facebook.presto.sql.analyzer.Session.DEFAULT_CATALOG;
import static com.facebook.presto.sql.analyzer.Session.DEFAULT_SCHEMA;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class TestInterpretedFilterFunction
{

    @Test
    public void testNullLiteral()
    {
        assertFilter("null", false);
    }

    @Test
    public void testBooleanLiteral()
    {
        assertFilter("true", true);
        assertFilter("false", false);
    }

    @Test
    public void testNotExpression()
    {
        assertFilter("not true", false);
        assertFilter("not false", true);
        assertFilter("not null", false);
    }

    @Test
    public void testAndExpression()
    {
        assertFilter("true and true", true);
        assertFilter("true and false", false);
        assertFilter("true and null", false);

        assertFilter("false and true", false);
        assertFilter("false and false", false);
        assertFilter("false and null", false);

        assertFilter("null and true", false);
        assertFilter("null and false", false);
        assertFilter("null and null", false);
    }

    @Test
    public void testORExpression()
    {
        assertFilter("true or true", true);
        assertFilter("true or false", true);
        assertFilter("true or null", true);

        assertFilter("false or true", true);
        assertFilter("false or false", false);
        assertFilter("false or null", false);

        assertFilter("null or true", true);
        assertFilter("null or false", false);
        assertFilter("null or null", false);
    }

    @Test
    public void testIsNullExpression()
    {
        assertFilter("null is null", true);
        assertFilter("42 is null", false);
    }

    @Test
    public void testIsNotNullExpression()
    {
        assertFilter("42 is not null", true);
        assertFilter("null is not null", false);
    }

    @Test
    public void testComparisonExpression()
    {
        assertFilter("42 = 42", true);
        assertFilter("42 = 42.0", true);
        assertFilter("42.42 = 42.42", true);
        assertFilter("'foo' = 'foo'", true);

        assertFilter("42 = 87", false);
        assertFilter("42 = 22.2", false);
        assertFilter("42.42 = 22.2", false);
        assertFilter("'foo' = 'bar'", false);

        assertFilter("42 != 87", true);
        assertFilter("42 != 22.2", true);
        assertFilter("42.42 != 22.22", true);
        assertFilter("'foo' != 'bar'", true);

        assertFilter("42 != 42", false);
        assertFilter("42 != 42.0", false);
        assertFilter("42.42 != 42.42", false);
        assertFilter("'foo' != 'foo'", false);

        assertFilter("42 < 88", true);
        assertFilter("42 < 88.8", true);
        assertFilter("42.42 < 88.8", true);
        assertFilter("'bar' < 'foo'", true);

        assertFilter("88 < 42", false);
        assertFilter("88 < 42.42", false);
        assertFilter("88.8 < 42.42", false);
        assertFilter("'foo' < 'bar'", false);

        assertFilter("42 <= 88", true);
        assertFilter("42 <= 88.8", true);
        assertFilter("42.42 <= 88.8", true);
        assertFilter("'bar' <= 'foo'", true);

        assertFilter("42 <= 42", true);
        assertFilter("42 <= 42.0", true);
        assertFilter("42.42 <= 42.42", true);
        assertFilter("'foo' <= 'foo'", true);

        assertFilter("88 <= 42", false);
        assertFilter("88 <= 42.42", false);
        assertFilter("88.8 <= 42.42", false);
        assertFilter("'foo' <= 'bar'", false);

        assertFilter("88 >= 42", true);
        assertFilter("88.8 >= 42.0", true);
        assertFilter("88.8 >= 42.42", true);
        assertFilter("'foo' >= 'bar'", true);

        assertFilter("42 >= 88", false);
        assertFilter("42.42 >= 88.0", false);
        assertFilter("42.42 >= 88.88", false);
        assertFilter("'bar' >= 'foo'", false);

        assertFilter("88 >= 42",  true);
        assertFilter("88.8 >= 42.0", true);
        assertFilter("88.8 >= 42.42", true);
        assertFilter("'foo' >= 'bar'", true);
        assertFilter("42 >= 42", true);
        assertFilter("42 >= 42.0", true);
        assertFilter("42.42 >= 42.42", true);
        assertFilter("'foo' >= 'foo'", true);

        assertFilter("42 >= 88", false);
        assertFilter("42.42 >= 88.0", false);
        assertFilter("42.42 >= 88.88", false);
        assertFilter("'bar' >= 'foo'", false);
    }

    @Test
    public void testComparisonExpressionWithNulls()
    {
        for (ComparisonExpression.Type type : ComparisonExpression.Type.values()) {
            assertFilter(format("NULL %s NULL", type.getValue()), false);

            assertFilter(format("42 %s NULL", type.getValue()), false);
            assertFilter(format("NULL %s 42", type.getValue()), false);

            assertFilter(format("11.1 %s NULL", type.getValue()), false);
            assertFilter(format("NULL %s 11.1", type.getValue()), false);
        }
    }

    public static void assertFilter(String expression, boolean expectedValue)
    {
        Expression parsed = SqlParser.createExpression(expression);
        Session session = new Session(null, DEFAULT_CATALOG, DEFAULT_SCHEMA);
        InterpretedFilterFunction filterFunction = new InterpretedFilterFunction(parsed, ImmutableMap.<Symbol, Input>of(), new TestingMetadata(), session);
        boolean result = filterFunction.filter();
        assertEquals(result, expectedValue);
    }
}
