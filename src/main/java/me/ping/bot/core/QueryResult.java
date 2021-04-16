package me.ping.bot.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryResult {
    private boolean hasResultSet;
    private ResultSet rs;
    private int affectedRows;

    public QueryResult(ResultSet rs, boolean hasResultSet, int affectedRows){
        this.affectedRows = affectedRows;
        this.hasResultSet = hasResultSet;
        this.rs = rs;
    }

    public boolean hasResultSet() {
        return hasResultSet;
    }

    public ResultSet getRs() {
        return rs;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public void close() {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
