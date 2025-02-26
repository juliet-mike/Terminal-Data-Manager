package dev.noodle.models;


import com.googlecode.lanterna.gui2.table.Table;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DBops {
    public static void main(String[] args) throws Exception {
        String fileName = "comp.csv";
        String filePath = "H:\\Other computers\\My MacBook Air\\Terminal-Data-Manager\\target\\classes\\comp.csv";
        System.out.println(getDatabaseURL());
        String[] array1 = {"apple", "banana", "cherry"};
        String[] array2 = {"red", "green", "blue"};
        String[] array3 = {"one", "two", "three"};

        // Creating a List to hold the String arrays
        List<String[]> listOfArrays = new ArrayList<>();

        // Adding the String arrays to the List
        listOfArrays.add(array1);
        listOfArrays.add(array2);
        listOfArrays.add(array3);

        //importTable(fileName, filePath, listOfArrays);
        //appendFileToList(fileName, filePath);
        importCSV(fileName, filePath);


    }

    public static String getDatabaseURL() {
        URL resource = DBops.class.getResource("/workingDb.sqlite");

        if (resource == null) {
            System.err.println("Could not find 'workingDb.sqlite' in resources folder!");
        }

        // 2) Convert URL -> File -> Absolute Path
        String dbPath = null;
        try {
            assert resource != null;
            File dbFile = Paths.get(resource.toURI()).toFile();
            dbPath = dbFile.getAbsolutePath();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        String DBurl = "jdbc:sqlite:" + dbPath;
        return DBurl;
    }


    public static void importTable(String FILENAME, String FILEPATH, List<String[]> rows) throws SQLException {
        //String url = "jdbc:sqlite:workingDb.sqlite";
        String url = getDatabaseURL();

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS [")
                .append(FILENAME)
                .append("] (")
                .append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

        // TODO i can almost promise this will throw an error later
        String[] headers = rows.get(0);

        for (int i = 0; i < headers.length; i++) {
            sb.append("\"").append(headers[i]).append("\"");
            sb.append(" TEXT");
            if (i < headers.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");


        String createTableSQL = sb.toString();
        assert url != null;
        Connection conn = DriverManager.getConnection(url);
        // Create the table if it doesn't exist
        conn.createStatement().execute(createTableSQL);
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //addRowsToTable(FILEPATH, rows);

        appendFileToList(FILENAME, FILEPATH);


    }


    public static List<String[]> updateGUI(String FILENAME) throws SQLException {
        List<String[]> tableData = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + FILENAME)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add headers as the first row
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                headers[i - 1] = metaData.getColumnName(i);
            }
            tableData.add(headers);

            // Add row data
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                tableData.add(row);
            }
        }
        return tableData;
    }




    public static String getCell(String tableName, int col, int row) throws SQLException {
        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            int currentRow = 0;

            while (rs.next()) { // Loop through rows
                if (currentRow == row) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    if (col >= 1 && col <= columnCount) {
                        return rs.getString(col); // Get the value
                    } else {
                        throw new IndexOutOfBoundsException("Column index out of range");
                    }
                }
                currentRow++;
            }
        }
        throw new IndexOutOfBoundsException("Row index out of range");
    }




    public static void appendFileToList(String FILENAME, String FILEPATH) throws SQLException {
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        // used to just pass url to the method then put url variable here ^

        // Create the table if it doesn't exist
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

        String insertSQL = "INSERT OR IGNORE INTO FileList (FILENAME, FILEPATH) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, FILENAME);
            pstmt.setString(2, FILEPATH);
            pstmt.executeUpdate();
        } catch (SQLException e) {
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
                    .append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

            // TODO i can almost promise this will throw an error later

            for (int i = 0; i < data.length; i++) {
                sb.append("\"").append(data[i]).append("\"");
                sb.append(" TEXT");
                if (i < data.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(");");

            stmt.execute(sb.toString());

            StringBuilder insertSQL = new StringBuilder("INSERT INTO [" + FILENAME + "] (");
            insertSQL.append(String.join(", ", data)).append(") VALUES (");
            insertSQL.append("?,".repeat(data.length).substring(0, data.length * 2 - 1)).append(")");

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString())) {
                String[] row;
                int batchSize = 500; // Adjust batch size for large CSVs
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



