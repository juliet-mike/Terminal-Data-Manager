package dev.noodle.models;



import com.googlecode.lanterna.gui2.table.Table;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URLDecoder;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import static dev.noodle.TDM.*;




public class DataOps {

    public static String getJarDir() {
        String path = DataOps.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return "";
        }
        File file = new File(decodedPath);
        return file.getParent();
    }

    public static String getDatabaseURL() {
        String DBurl = "jdbc:sqlite:" + getJarDir() + "/workingDb.db";
        return DBurl;
    }


    public static void TabletoSQL(String FILENAME, Table<String> table) throws SQLException {
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS [" + FILENAME + "]");

        if (Objects.equals(table.getTableModel().getColumnLabel(0), "ID")){
        System.out.println("YOU HAVE ID IN THE #1 header");
        table.getTableModel().removeColumn(0);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS [")
                .append(FILENAME)
                .append("] (")
                .append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

        int columnCount = table.getTableModel().getColumnCount();
        int rowCount = table.getTableModel().getRowCount();

        for (int i = 0; i < columnCount; i++) {
//                if (isSQLKeyword(data[i])) {
//                    data[i] = "SANITIZED_col"+ i ;
//                }
            sb.append("[").append(table.getTableModel().getColumnLabel(i)).append("]");
            sb.append(" TEXT");
            if (i < columnCount - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");
        //System.out.println(sb);
        stmt.execute(sb.toString());

        // insert the data post creating and whatever you just did
        for (int j = 0; j < rowCount; j++) {
            StringBuilder insertSQL = new StringBuilder("INSERT INTO [" + FILENAME + "] (");
            //header names
            // start at 1 to avoid the ID column as that will be included anyway and will probably cause SQL exceptions
            for (int i = 0; i < columnCount; i++) {
                insertSQL.append(table.getTableModel().getColumnLabel(i));
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");
            //values
            insertSQL.append(" VALUES (");
            for (int i = 0; i < columnCount; i++) {
                insertSQL.append("'").append(getTableCell(i , j)).append("'");
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");
            //System.out.println(insertSQL);
            stmt.execute(String.valueOf(insertSQL));
        }
        conn.close();
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
            //todo add error message support for methods in this class


            System.out.println("SQL error: " + e.getMessage());
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
                    .append("ID INTEGER PRIMARY KEY AUTOINCREMENT, ");

            // TODO i can almost promise this will throw an error later
                    // lol it did cause SQL syntax thinks one of the headers is a SQL command ugh
                    // and sanitizing refuses to work maybe because of how im stacking all these strings together?

            for (int i = 0; i < data.length; i++) {
//                if (isSQLKeyword(data[i])) {
//                    data[i] = "SANITIZED_col"+ i ;
//                }
                sb.append("[").append(data[i]).append("]");
                sb.append(" TEXT");
                if (i < data.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(");");
            stmt.execute(sb.toString());

            StringBuilder insertSQL = new StringBuilder("INSERT INTO [" + FILENAME + "] (");
            insertSQL.append(String.join(", ", data))
                    .append(") VALUES (");
            insertSQL.append("?,".repeat(data.length).substring(0, data.length * 2 - 1)).append(")");

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


    }



