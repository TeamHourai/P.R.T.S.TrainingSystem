package com.hourai.prts.dao;

import java.sql.*;

public final class DbCompat {
    private static final String URL = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "p.r.t.s.data115";

    private DbCompat() {}

    public static boolean tableExists(String table) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getTables(null, null, table, new String[]{"TABLE"})) {
                if (rs.next()) return true;
            }
            // try case-insensitive
            try (ResultSet rs = md.getTables(null, null, table.toUpperCase(), new String[]{"TABLE"})) {
                if (rs.next()) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean columnExists(String table, String column) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, column)) {
                if (rs.next()) return true;
            }
            try (ResultSet rs = md.getColumns(null, null, table, column.toUpperCase())) {
                if (rs.next()) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isAutoIncrement(String table, String column) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, column)) {
                if (rs.next()) {
                    try {
                        String ia = rs.getString("IS_AUTOINCREMENT");
                        if (ia != null) return "YES".equalsIgnoreCase(ia);
                    } catch (Exception ignored) {}
                }
            }
            try (ResultSet rs = md.getColumns(null, null, table, column.toUpperCase())) {
                if (rs.next()) {
                    try {
                        String ia = rs.getString("IS_AUTOINCREMENT");
                        if (ia != null) return "YES".equalsIgnoreCase(ia);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isNullable(String table, String column) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, column)) {
                if (rs.next()) {
                    try {
                        int n = rs.getInt("NULLABLE");
                        return n == DatabaseMetaData.columnNullable;
                    } catch (Exception ignored) {}
                }
            }
            try (ResultSet rs = md.getColumns(null, null, table, column.toUpperCase())) {
                if (rs.next()) {
                    try {
                        int n = rs.getInt("NULLABLE");
                        return n == DatabaseMetaData.columnNullable;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
