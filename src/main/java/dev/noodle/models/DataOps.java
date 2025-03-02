package dev.noodle.models;



import com.googlecode.lanterna.gui2.table.Table;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import static dev.noodle.TDM.*;
import static dev.noodle.models.CreateSQLiteDB.getJarDir;



public class DataOps {


    public static String getDatabaseURL() {
        String DBurl = "jdbc:sqlite:" + getJarDir() + "/workingDb.db";
        return DBurl;
    }


    public static void TabletoSQL(String FILENAME) throws SQLException {
        Connection conn = DriverManager.getConnection(getDatabaseURL());
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS" + FILENAME );

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS [")
                .append(FILENAME)
                .append("] (")
                .append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        //todo need to execute above sb statement *** after header names are added!!!!

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

        stmt.execute(sb.toString());

        // insert the data post creating and whatever you just did
        for (int j = 0; j < rowCount; j++) {
            StringBuilder insertSQL = new StringBuilder("INSERT INTO " + FILENAME + " (");
            //header names
            // start at 1 to avoid the ID column as that will be included anyway and will probably cause SQL exceptions
            for (int i = 1; i < columnCount; i++) {
                insertSQL.append(table.getTableModel().getColumnLabel(i));
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");

            //values
            insertSQL.append(" VALUES (");
            for (int i = 1; i < columnCount; i++) {
                insertSQL.append(getTableCell(i , j));
                if (i < columnCount - 1) {
                    insertSQL.append(" ,");
                }
            }
            insertSQL.append(")");
            stmt.execute(insertSQL.toString());
        }

        conn.close();
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

        String insertSQL = "INSERT OR IGNORE INTO FileList (FILENAME ,FILEPATH) VALUES (?, ?)";
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
                    .append("ID INTEGER PRIMARY KEY AUTOINCREMENT, ");

            // TODO i can almost promise this will throw an error later
                    // lol it did cause SQL syntax thinks one of the headers is a SQL command ugh

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
        public static void exportTabletoFile(String path) throws IOException, InterruptedException, CsvValidationException, SQLException {
            try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
                int columnCount = table.getTableModel().getColumnCount();
                int rowCount = table.getTableModel().getRowCount();

                String[] headers = new String[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    headers[col] = table.getTableModel().getColumnLabel(col);
                }
                writer.writeNext(headers);

                // Write table data
                for (int row = 0; row < rowCount; row++) {
                    List<String> rowData = new ArrayList<>();
                    for (int col = 0; col < columnCount; col++) {
                        rowData.add(table.getTableModel().getCell(col, row));
                    }
                    writer.writeNext(rowData.toArray(new String[0]));
                }
                //System.out.println("CSV file saved at: " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }



