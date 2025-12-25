package com.hourai.prts.tools;

import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * Simple CSV -> DB importer.
 * Usage: java com.hourai.prts.tools.CsvImporter [dataDir] [jdbcUrl] [dbUser] [dbPass]
 * Defaults:
 *  dataDir = ./data
 *  jdbcUrl = jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
 *  dbUser = root
 *  dbPass = p.r.t.s.data115
 *
 * Assumptions:
 * - CSV file name (without .csv) equals DB table name.
 * - CSV rows do NOT include header row; columns count and order match table definition.
 * - Empty field is stored as NULL.
 */
public class CsvImporter {
    public static void main(String[] args) throws Exception {
        String dataDir = "data";
        String jdbc = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = args.length >= 3 ? args[2] : "root";
        String pass = args.length >= 4 ? args[3] : "p.r.t.s.data115";

        // Support flexible args:
        // - java CsvImporter <csvFilePath> [jdbc] [user] [pass]
        // - java CsvImporter <tableName> [jdbc] [user] [pass]  -> looks for data/<tableName>.csv
        // - java CsvImporter [dataDir] [jdbc] [user] [pass]
        boolean singleFileMode = false;
        Path singleFilePath = null;
        String singleTableName = null;

        if (args.length >= 1) {
            String a0 = args[0];
            Path p0 = Paths.get(a0);
            if (Files.exists(p0) && Files.isRegularFile(p0)) {
                singleFileMode = true;
                singleFilePath = p0;
            } else if (a0.toLowerCase().endsWith(".csv")) {
                // relative to cwd
                Path p = Paths.get(a0);
                if (Files.exists(p) && Files.isRegularFile(p)) { singleFileMode = true; singleFilePath = p; }
            } else if ("questions_onboarding".equalsIgnoreCase(a0) || a0.matches("[a-zA-Z0-9_]+")) {
                // treat as table name: look for data/<name>.csv
                singleTableName = a0;
            } else {
                dataDir = a0;
            }
        }
        if (args.length >= 2) {
            if (singleFileMode) {
                // jdbc provided as second arg
                jdbc = args[1];
                if (args.length >= 3) user = args[2];
                if (args.length >= 4) pass = args[3];
            } else if (singleTableName != null && args.length >= 2) {
                jdbc = args[1];
                if (args.length >= 3) user = args[2];
                if (args.length >= 4) pass = args[3];
            } else {
                jdbc = args[1];
                if (args.length >= 3) user = args[2];
                if (args.length >= 4) pass = args[3];
            }
        }

        Path dir = Paths.get(dataDir);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.err.println("Data directory not found: " + dir.toAbsolutePath());
            System.exit(2);
        }

        try (Connection conn = DriverManager.getConnection(jdbc, user, pass)) {
            DatabaseMetaData md = conn.getMetaData();
            if (singleFileMode && singleFilePath != null) {
                Path f = singleFilePath;
                String fileName = f.getFileName().toString();
                String table = fileName.substring(0, fileName.length() - 4);
                System.out.println("Importing single file " + f + " -> table " + table);
                List<String> cols = getTableColumns(conn, conn.getCatalog(), table);
                if (cols.isEmpty()) {
                    System.err.println("  Skipped: table not found or has no columns: " + table);
                } else importCsvToTable(conn, f, table, cols);
                return;
            }

            if (singleTableName != null) {
                Path f = dir.resolve(singleTableName + ".csv");
                if (!Files.exists(f)) {
                    System.err.println("CSV file for table not found: " + f.toAbsolutePath());
                } else {
                    List<String> cols = getTableColumns(conn, conn.getCatalog(), singleTableName);
                    if (cols.isEmpty()) System.err.println("  Skipped: table not found or has no columns: " + singleTableName);
                    else importCsvToTable(conn, f, singleTableName, cols);
                }
                return;
            }

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.csv")) {
                for (Path f : ds) {
                    String fileName = f.getFileName().toString();
                    String table = fileName.substring(0, fileName.length() - 4);
                    System.out.println("Importing file " + f + " -> table " + table);
                    List<String> cols = getTableColumns(conn, conn.getCatalog(), table);
                    if (cols.isEmpty()) {
                        System.err.println("  Skipped: table not found or has no columns: " + table);
                        continue;
                    }
                    importCsvToTable(conn, f, table, cols);
                }
            }
        }
    }

    private static List<String> getTableColumns(Connection conn, String schema, String table) throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    private static void importCsvToTable(Connection conn, Path csvFile, String table, List<String> cols) {
        String q = buildInsertSql(table, cols.size(), cols);
        System.out.println("  INSERT SQL: " + q);
        int total = 0;
        try (BufferedReader r = Files.newBufferedReader(csvFile)) {
            String line;
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] fields = parseCsvLine(line, cols.size());
                    for (int i = 0; i < cols.size(); i++) {
                        String v = i < fields.length ? fields[i] : null;
                        if (v == null || v.isEmpty()) ps.setNull(i + 1, Types.VARCHAR);
                        else ps.setString(i + 1, v);
                    }
                    try {
                        ps.executeUpdate();
                        total++;
                    } catch (SQLException e) {
                        System.err.println("    row insert error: " + e.getMessage());
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("  Failed importing " + csvFile + ": " + e.getMessage());
            return;
        }
        System.out.println("  Imported rows: " + total);
    }

    private static String buildInsertSql(String table, int colCount, List<String> cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append("`" ).append(table).append("` (");
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('`').append(cols.get(i)).append('`');
        }
        sb.append(") VALUES (");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        sb.append(')');
        return sb.toString();
    }

    // Very small CSV parser supporting quoted fields with double quotes and commas inside quotes.
    private static String[] parseCsvLine(String line, int expected) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) { cur.append(c); esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (c == ',' && !inQuotes) { out.add(cur.toString()); cur.setLength(0); continue; }
            cur.append(c);
        }
        out.add(cur.toString());
        // Trim fields but preserve internal spaces
        for (int i = 0; i < out.size(); i++) {
            String v = out.get(i);
            // unescape double quotes inside
            v = v.replace("\"\"", "\"");
            out.set(i, v.trim());
        }
        // If expected > actual, leave as is (caller will treat missing as null)
        return out.toArray(new String[0]);
    }
}
