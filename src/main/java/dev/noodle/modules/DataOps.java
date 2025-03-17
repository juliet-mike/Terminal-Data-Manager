package dev.noodle.modules;


import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URLDecoder;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import static dev.noodle.TDM.*;


public class DataOps {
    private static final Logger log = LoggerFactory.getLogger(DataOps.class);

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


    public static TableModel<String> TableModelFromSQL(String FILENAME) throws SQLException {
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

    public static void setTableModelFromSQL(String FILENAME) throws SQLException {
        Table<String> stringTable = getTDMTable().setTableModel(TableModelFromSQL(FILENAME));
        setTDMTableModel(stringTable.getTableModel());

    }

    public static TableModel<String> CustomUpdateTableFromSQL(String query) throws SQLException {
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
    public static void opnRecentFileFromInternal() {
        BasicWindow dialogWindow = new BasicWindow("open recent file");
        Panel recentPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        int limitRecentCount = 10;
        TerminalSize size = new TerminalSize(50, limitRecentCount);
        RadioBoxList<quickOps.Option> selectionBox1 = new RadioBoxList<quickOps.Option>(size);
        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT FILENAME FROM [FileList] ORDER BY ID LIMIT " + limitRecentCount )) {
            //ResultSetMetaData metaData = rs.getMetaData();
            recentPanel.addComponent(selectionBox1);
            int column = 1; int index = 1;
            while (rs.next()) {
                selectionBox1.addItem((new quickOps.Option(index, rs.getString(column))));
                index++;
            }
            recentPanel.addComponent(new Button("Submit", () -> {
                quickOps.Option selectedOption1 = selectionBox1.getCheckedItem();
                if (selectedOption1 != null) {
                    String selectedFileName = selectedOption1.getName();
                    try {
                        setTableModelFromSQL(selectedFileName);
                    } catch (SQLException e) {
                        showErrorDialog("SQL error", e.getMessage());
                    }
                    dialogWindow.close();
                }
            }));
            Button exitButton = new Button("Exit", dialogWindow::close);
            recentPanel.addComponent(exitButton);
            dialogWindow.setComponent(recentPanel);
            globalGui.addWindowAndWait(dialogWindow);

        } catch (SQLException e) {
            showErrorDialog("SQL error", e.getMessage());
        }
    }




}



