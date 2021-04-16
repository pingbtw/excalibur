package me.ping.bot.core;

import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;

import java.sql.*;

public class DbHandler {
    public static enum QueryType {
        SELECT,
        UPDATE,
        INSERT,
        DELETE,
        CREATE
    }

    private Connection connection;

    private static DbHandler db = null;

    private DbHandler() {
        openConnection();
    }

    private void openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DbHandler getInstance() {
        if (db == null)
            db = new DbHandler();
        return db;
    }

    /**
     * @param query  SQL query
     * @param type   Database.QueryType.
     * @param params String|int|long|float|double
     * @return QueryResult
     * @throws InvalidDataTypeException
     * @throws ParameterCountMismatchException
     *
     * do not call connection.close() or stmt.close() in this method;
     * it will invalidate the ResultSet, so we call it indirectly
     * after we handle the result.
     */
    public QueryResult query(String query, QueryType type, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {

        int expectedParams = StringUtils.countMatches(query, "?");

        if (expectedParams != params.length)
            throw new ParameterCountMismatchException("Number of variables doesn't match number of parameters in prepared statement");

        PreparedStatement stmt = null;
        QueryResult queryResult = null;
        try {
            if (connection == null) {
                openConnection();
            }
            stmt = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer)
                    stmt.setInt(i + 1, (Integer) params[i]);
                else if (params[i] instanceof String)
                    stmt.setString(i + 1, (String) params[i]);
                else if (params[i] instanceof Double)
                    stmt.setDouble(i + 1, (Double) params[i]);
                else if (params[i] instanceof Float)
                    stmt.setFloat(i + 1, (Float) params[i]);
                else if (params[i] instanceof Long)
                    stmt.setLong(i + 1, (Long) params[i]);
                else
                    throw new InvalidDataTypeException("Unknown data type for query at parameter " + (i + 2));
            }
            ResultSet rs = null;
            int affectedRows = 0;

            switch (type) {
                case SELECT:
                    rs = stmt.executeQuery();
                    break;
                case INSERT:
                case UPDATE:
                case DELETE:
                case CREATE:
                    affectedRows = stmt.executeUpdate();
                    break;
            }

            if (rs == null) {
                queryResult = new QueryResult(null, false, affectedRows);
            } else {
                queryResult = new QueryResult(rs, true, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResult;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // convenience methods
    public QueryResult select(String query, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {
        return query(query, QueryType.SELECT, params);
    }

    public QueryResult update(String query, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {
        return query(query, QueryType.UPDATE, params);
    }

    public QueryResult insert(String query, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {
        return query(query, QueryType.INSERT, params);
    }

    public QueryResult delete(String query, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {
        return query(query, QueryType.DELETE, params);
    }

    public QueryResult create(String query, Object... params)
            throws InvalidDataTypeException, ParameterCountMismatchException {
        return query(query, QueryType.CREATE, params);
    }
}
