
//                         TDM - Terminal Data Manager
//        Copyright (C) 2025 juliet-mike (https://github.com/juliet-mike)
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
//
//


package dev.noodle.modules;


import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import dev.noodle.remoteSQL.remoteSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

import static dev.noodle.TDM.*;


public class dataOps {
    private static final Logger log = LoggerFactory.getLogger(dataOps.class);

    public static String getJarDir() {
        String path = dataOps.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        File file = new File(decodedPath);
        return file.getParent();
    }

    public static String getDatabaseURL() {
        String DBurl = "jdbc:sqlite:" + getJarDir() + "/workingDb.db";
        return DBurl;
    }

    public static void TableToInternalSQL(String FILENAME, Table<String> table) throws SQLException {
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS [" + FILENAME + "]");

        if (Objects.equals(table.getTableModel().getColumnLabel(0).toUpperCase(), "ID")){
        System.out.println("YOU HAVE ID IN THE #1 header");
        table.getTableModel().removeColumn(0);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS [")
                .append(FILENAME)
                .append("] (")
                .append("TDM_ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT, ");

        int columnCount = table.getTableModel().getColumnCount();
        int rowCount = table.getTableModel().getRowCount();

        for (int i = 0; i < columnCount; i++) {
            sb.append("[").append(table.getTableModel().getColumnLabel(i)).append("]");
            sb.append(" TEXT");
            if (i < columnCount - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");
        stmt.execute(sb.toString());

        // insert the data post creating and whatever you just did
        for (int j = 0; j < rowCount; j++) {
            StringBuilder insertSQL = new StringBuilder("INSERT INTO [" + FILENAME + "] (");
            //header names
            // start at 1 to avoid the ID column as that will be included anyway and will probably cause SQL exceptions
            //nevermind we are just inserting the whole thing now
            for (int i = 0; i < columnCount; i++) {
                insertSQL.append("[").append(table.getTableModel().getColumnLabel(i)).append("]");
                //insertSQL.append(table.getTableModel().getColumnLabel(i));
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");
            //values
            insertSQL.append(" VALUES (");
            for (int i = 0; i < columnCount; i++) {
                insertSQL.append("'").append(getTDMTableCell(i , j)).append("'");
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");
            //System.out.println(insertSQL);
            stmt.execute(String.valueOf(insertSQL));
            //log.info(String.valueOf(insertSQL));
        }
        conn.close();
    }

    public static void createRecentQueryTable()  {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(getDatabaseURL());
        } catch (SQLException e) {
            showErrorDialog("SQL TDM INTERNAL TABLE ERROR", ("unable to reach internal sqlite db" + e.getMessage()));
        }
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS RecentQueries (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        query TEXT NOT NULL,
        url TEXT,
        dbname TEXT,
        env TEXT,
        username TEXT
        )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
        catch (SQLException e) {
            showErrorDialog("SQL TDM CREATE INTERNAL TABLE ERROR", ("unable to create internal table for recent queries" + e.getMessage()));
        }
    }
    public static void insertValuesRecentQueryTable(String query, String url, String dbname, String env, String username) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(getDatabaseURL());
        }catch (SQLException e) {
            showErrorDialog("Connection Error", ("unable to connect to internal db" + e.getMessage()));
        }
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS RecentQueries (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        query TEXT NOT NULL,
        url TEXT,
        dbname TEXT,
        env TEXT,
        username TEXT
        )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }

        String insertSQL = "INSERT INTO RecentQueries (query, url, dbname, env, username) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, query);
            pstmt.setString(2, url);
            pstmt.setString(3, dbname);
            pstmt.setString(4, env);
            pstmt.setString(5, username);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            showErrorDialog("SQL error: could not insert", e.getMessage());
        }
    }

    public static void appendFileToList(String FILENAME, String FILEPATH) throws SQLException {
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS FileList (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +  // SQLite syntax
                        "  FILENAME TEXT NOT NULL, " +
                        "  FILEPATH TEXT NOT NULL," +
                        " UNIQUE(FILENAME, FILEPATH)" +
                        ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
        String insertSQL = "INSERT OR IGNORE INTO FileList (FILENAME ,FILEPATH) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, FILENAME);
            pstmt.setString(2, FILEPATH);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showErrorDialog("SQL Insert error for" + FILENAME, e.getMessage());
        }
        conn.close();
    }

    public static void importCSV(String FILENAME, String FILEPATH) throws IOException, InterruptedException, CsvValidationException, SQLException {
        CSVReader reader = new CSVReader(new FileReader(FILEPATH));
        String[] data = reader.readNext(); // Read the first row (data)
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        Statement stmt = conn.createStatement();

        stmt.execute("DROP TABLE IF EXISTS [" + FILENAME + "]");

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS [")
                    .append(FILENAME)
                    .append("] (")
                    .append("TDM_ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
            // does it need brackets
            for (int i = 0; i < data.length; i++) {
                sb.append("\"")
                        .append(data[i])
                        .append("\"");
                sb.append(" TEXT");
                if (i < data.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(");");
            //System.out.println(sb);
            stmt.execute(sb.toString());

            StringBuilder insertSQL = new StringBuilder("INSERT INTO [" + FILENAME + "] (");
            //insertSQL.append(String.join(", ", data))
            for (int i = 0; i < data.length; i++) {
                //insertSQL.append("[\"").append(data[i]).append("\"]");
                insertSQL.append("\"")
                        .append(data[i])
                        .append("\"");
                if (i < data.length - 1) {
                    insertSQL.append(", ");
                }
            }
            insertSQL.append(") VALUES (");
            insertSQL.append("?,".repeat(data.length).substring(0, data.length * 2 - 1)).append(")");
            //System.out.println(insertSQL);

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString())) {
                String[] row;
                int batchSize = 100; // Adjust batch size for large CSVs
                int count = 0;

                while ((row = reader.readNext()) != null) {
                    for (int i = 0; i < row.length; i++) {
                        pstmt.setString(i + 1, row[i].trim());
                    }
                    pstmt.addBatch();
                    if (++count % batchSize == 0) {
                        pstmt.executeBatch(); // Execute batch every 'batchSize' rows
                    }
                }
                pstmt.executeBatch(); // Insert remaining records
                //System.out.println("CSV imported successfully into table: " + FILENAME);
            }
            appendFileToList(FILENAME, FILEPATH);
        }


    public static TableModel<String> tableModelFromInternalSQLTable(String FILENAME) throws SQLException {
        Table<String> tableData;
        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + "[" +FILENAME+"]" )) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add headers as the first row
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                headers[i - 1] = metaData.getColumnName(i);
            }
            tableData = new Table<>(headers);
            // Add row data
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                tableData.getTableModel().addRow(row);
            }
        }
        return tableData.getTableModel();
    }

    public static void setTableModelFromInternalDB(String FILENAME) throws SQLException {
        Table<String> stringTable = getTDMTable().setTableModel(tableModelFromInternalSQLTable(FILENAME));
        setTDMTableModel(stringTable.getTableModel());
    }

    public static TableModel<String> CustomGetTableModelFromSQL(String query) throws SQLException {
        Table<String> tableData;
        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add headers as the first row
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                headers[i - 1] = metaData.getColumnName(i);
            }
            tableData = new Table<>(headers);
            // Add row data
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                tableData.getTableModel().addRow(row);
            }
        }
        return tableData.getTableModel();
    }

    public static TableModel<String> CustomGetTableModelFromSQL(String query, Connection connection) throws SQLException {
        Table<String> tableData;
        Connection conn = connection;
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(query);
        {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add headers as the first row
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                headers[i - 1] = metaData.getColumnName(i);
            }
            tableData = new Table<>(headers);
            // Add row data
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                tableData.getTableModel().addRow(row);
            }
        }
        return tableData.getTableModel();
    }


    public static RadioBoxList<remoteSQL.OptionofRecent> recentTableEntriesListFromInternal(String selectCol, String tableName, int recentResultsToShow) {
//        TerminalSize size = new TerminalSize(70, recentResultsToShow);
//        RadioBoxList<remoteSQL.OptionofRecent> selectionBox1 = new RadioBoxList<remoteSQL.OptionofRecent>(size);

        RadioBoxList<remoteSQL.OptionofRecent> selectionBox1 = new RadioBoxList<>();
        selectionBox1.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));

        if (!Objects.equals(selectCol, "*")) {
            // absolutely will not work if you input * outside the method????????
            try (Connection conn = DriverManager.getConnection(getDatabaseURL());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT " + selectCol + " FROM "+ tableName + " ORDER BY ID LIMIT " + recentResultsToShow)) {
                 String[] values = new String[rs.getMetaData().getColumnCount()];
                 int index = 1;
                 while (rs.next()) {
                     for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                         values[i - 1] = rs.getString(i);
                     }
                     selectionBox1.addItem((new remoteSQL.OptionofRecent(index, rs.getString(selectCol), values)));
                     index++;
                 }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                showErrorDialog("SQL error", e.getMessage());

            }
        }else {
            try (Connection conn = DriverManager.getConnection(getDatabaseURL());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY ID LIMIT " + recentResultsToShow)) {
                String[] values = new String[rs.getMetaData().getColumnCount()];
                //int column = 1;
                int index = 1;
                while (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        values[i - 1] = rs.getString(i);
                    }
                    selectionBox1.addItem((new remoteSQL.OptionofRecent(index, rs.getString(selectCol), values)));
                    index++;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                showErrorDialog("SQL error", e.getMessage());
            }
        }
        return selectionBox1;
    }

    public static RadioBoxList<quickOps.Option> recentTableEntriesListFromInternalasQuickOption (String selectCol, String tableName, int recentResultsToShow) {
        TerminalSize size = new TerminalSize(50, recentResultsToShow);
        RadioBoxList<quickOps.Option> selectionBox1 = new RadioBoxList<quickOps.Option>(size);
        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT " + selectCol + " FROM "+ tableName + " ORDER BY ID LIMIT " + recentResultsToShow)) {

           //int column = 1;
            int index = 1;
            while (rs.next()) {
                selectionBox1.addItem((new quickOps.Option(index, rs.getString(selectCol))));
                index++;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            showErrorDialog("SQL error", e.getMessage());
        }
        return selectionBox1;
    }




}



