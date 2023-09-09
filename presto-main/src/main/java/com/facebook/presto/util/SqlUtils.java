package com.facebook.presto.util;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.sql.SQLException;

public class SqlUtils
{
    /**
     * Run a SQL query as Runnable ignoring any constraint violations.
     * This is a HACK to allow us to support idempotent inserts on
     */
    public static void runIgnoringConstraintViolation(Runnable task)
    {
        try {
            task.run();
        }
        catch (UnableToExecuteStatementException e) {
            if (e.getCause() instanceof SQLException) {
                String state = ((SQLException) e.getCause()).getSQLState();
                if (state.startsWith("23")) {
                    return;
                }
            }
            throw e;
        }
    }
}
