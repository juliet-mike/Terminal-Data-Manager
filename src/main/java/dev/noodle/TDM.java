package dev.noodle;

import bsh.EvalError;
import bsh.Interpreter;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import dev.noodle.models.DataOps;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static dev.noodle.models.DataOps.getDatabaseURL;



public class TDM {


    public static Table<String> table = new Table<>( "1", "2", "3", "4", "5");
    // should this be its own class? or have a method encasing all of these in TDM????
    public static Table<String> getTable() {
        return table;
    }
    public static String getTablecell(int x, int y) {
        return table.getTableModel().getCell(x, y);
    }
    public static List<String> getTablerow(int row) {
        List<String> rowData = table.getTableModel().getRow(row);;
        return rowData;
    }
    public static List<String> getTableColumn(int column) {
        List<String> columnData = new ArrayList<>();
        TableModel<String> model = table.getTableModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            columnData.add(model.getCell(column, row));
        }
        return columnData;
    }


    public static void main(String[] args) throws IOException {
        Theme theme = LanternaThemes.getRegisteredTheme("bigsnake");

        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;

        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();

            // Create a Panel to hold components (Buttons, Labels, TextBoxes, etc.)
            Panel panel = new Panel();
            panel.setLayoutManager(new BorderLayout());  // Using BorderLayout to manage component positioning

            // Create a GUI manager and set the screen for the window
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.setTheme(theme);



            Panel topSubPanel = new Panel();
            panel.addComponent(topSubPanel.withBorder(Borders.doubleLineBevel()), BorderLayout.Location.TOP);
            topSubPanel.setLayoutManager(new GridLayout(6));
            topSubPanel.setTheme(theme);

            MenuBar menubar = new MenuBar();
            topSubPanel.addComponent(menubar);

            // array because I want to have a recent file list implemented soon that or a list of files that can be selected to bring into view
            Path[] selectedFileNames = new Path[5];
            Path[] selectedFilePaths = new Path[5];

            // setup memory usage monitoring
            Label memoryLabel;
            topSubPanel.addComponent(memoryLabel = new Label("memory usage"), GridLayout.createHorizontallyEndAlignedLayoutData(3));
            Thread memoryThread = new Thread(() -> {
                try {
                    MemoryTracker.getMemoryTracking(memoryLabel);
                } catch (InterruptedException e) {
                    System.out.println("Memory tracker init failed");
                    throw new RuntimeException(e);
                }
            });
            memoryThread.setDaemon(true);
            memoryThread.start();


            //shows active/ loading file -- interacted with by openFileDialog() method
            Label selectedFileLabel0;
            topSubPanel.addComponent(selectedFileLabel0 = new Label("Select a file"), GridLayout.createHorizontallyEndAlignedLayoutData(4));


            Menu fileMenu = new Menu("File");
            Label finalSelectedFileLabel = selectedFileLabel0;
            fileMenu.add(new MenuItem("Import New File", () -> {
                try {
                    openFileDialog(0, finalSelectedFileLabel, selectedFilePaths, selectedFileNames, gui);
                } catch (SQLException | CsvValidationException | IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));

            //new method - open PATH dialog after having a user input popup
            fileMenu.add(new MenuItem("Save to Internal Memory", () -> System.out.println("Save As")));

            fileMenu.add(new MenuItem("Save As CSV", () -> openSaveDialog(gui)));




            Menu settingsMenu = new Menu("Settings");

            settingsMenu.add(new MenuItem("User Preferences", () -> System.out.println("User Preferences selected")));

            fileMenu.add(settingsMenu);
            Screen finalScreen = screen;
            Screen finalScreen1 = screen;
            fileMenu.add(new MenuItem("Exit", () -> {
                // First, close the screen.
                try {
                    //finalScreen.close();
                    shutDown(finalScreen);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Then exit the application.
            }));
            //Menu openMenu = new Menu("Save to internal memory");
            Menu helpMenu = new Menu("Help");
            Menu scriptMenu = new Menu("Scripts");
            scriptMenu.add(new MenuItem("Open Script Editor", () -> showFullPageScriptDialog(gui, "Script Editor")));

            // add above objects to menu object
            menubar.add(fileMenu).add(scriptMenu).add(helpMenu); //.add(openMenu);

            // init new panel
            Panel leftSubPanel = new Panel();
            leftSubPanel.setTheme(theme);
            leftSubPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            panel.addComponent(leftSubPanel.withBorder(Borders.singleLine()), BorderLayout.Location.LEFT); // Place button at the bottom of the panel
            leftSubPanel.addComponent(new Label("Quick Operations").setForegroundColor(TextColor.ANSI.BLUE)); // maybe move this to the panel title


            TerminalSize size = new TerminalSize(17, 20);
            ActionListBox dataframeActionListBox = new ActionListBox(size);

            dataframeActionListBox.addItem("Quick CAL", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Add", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Subtract", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Multiply", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Divide", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Exponent", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("ABS", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Round", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });

            dataframeActionListBox.addItem("Regression", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Sort", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Rename Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Merge Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Merge Table", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Split Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Contains", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("If/then/else", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Add Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Remove Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("Rename Column", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });
            dataframeActionListBox.addItem("TrimText", new Runnable() {
                @Override
                public void run() {
                    // Code to run when action activated
                }
            });


            leftSubPanel.addComponent(dataframeActionListBox.withBorder(Borders.singleLine()));
            //content for leftSubPanel ends here

            // init mid - ie center panel
            Panel MidSubPanel = new Panel();
            MidSubPanel.setTheme(theme);
            MidSubPanel.setLayoutManager(new GridLayout(1));
            panel.addComponent(MidSubPanel.withBorder(Borders.doubleLine()), BorderLayout.Location.CENTER);
            // Create the table and store it in a variable

           // Table<String> table = new Table<>( "1", "2", "3", "4", "5");
            // Add the table to the panel
            MidSubPanel.addComponent(table.withBorder(Borders.doubleLine()));
            // Add a row to the table

            for (int i = 0; i < 50; i++) {
                table.getTableModel().addRow("1", "2", "3", "4", "5");
            }


            table.setCellSelection(true);
            table.setSelectAction(new Runnable() {
                @Override
                public void run() {
                    int selectedRow = table.getSelectedRow();
                    int selectedCol = table.getSelectedColumn();
                    String cellValue = table.getTableModel().getCell(selectedCol, selectedRow);
                    String result = new TextInputDialogBuilder()
                            .setTitle("Cell Wizard")
                            .setDescription("Modify Cell Value?")
                            .setInitialContent(cellValue)
                            .build()
                            .showDialog(gui);

                    if (result != null) {
                        //System.out.println("User input: " + result);
                        //todo set user input to new cell value and write to db
                        //or check if there have been changes in x number of seconds then write?
                        table.getTableModel().setCell(selectedCol, selectedRow, result);
                    } else {


                    }
                }
            });
            // mid/center panel ends here
/*
            // init right panel
            Panel RightSubPanel = new Panel();
            RightSubPanel.setTheme(theme);
            RightSubPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            panel.addComponent(RightSubPanel.withBorder(Borders.singleLine()), BorderLayout.Location.RIGHT); // Place button at the bottom of the panel
            //RightSubPanel.addComponent(new Label("Data query"));

            //RightSubPanel.addComponent(new EmptySpace(new TerminalSize(0, 0)));

            RightSubPanel.addComponent(new Label("Variable Stats").setForegroundColor(TextColor.ANSI.BLUE));
            Table<String> varStatTable1 = new Table<>("VAR", "OBS", "MISS", "UNIQ", "RNG");
            varStatTable1.getTableModel().addRow("Age","500", "0","100","18-80");
            varStatTable1.getTableModel().addRow("Gender","500","0","2","N/A");
            varStatTable1.getTableModel().addRow("Income","480","20","400","$20K");
            varStatTable1.getTableModel().addRow("Education","500","0","5","0-4");
            varStatTable1.getTableModel().addRow("Region","500","0","7","N/A");
            varStatTable1.getTableModel().addRow("Marital","500","0","3","N/A");
            varStatTable1.getTableModel().addRow("Employment","490","10","6","N/A");
            //TerminalSize statTable1Size = varStatTable1.getSize();
            RightSubPanel.addComponent(varStatTable1.withBorder(Borders.singleLine()));

            Table<String> varStatTable2 = new Table<>("VAR", "AVG", "MED", "MEAN", "MAX");
            varStatTable2.getTableModel().addRow("Age","35.5","35","35.5","80");
            varStatTable2.getTableModel().addRow("Income","55000","54000","55000","15");
            varStatTable2.getTableModel().addRow("Score","75.5","75","75.5","100");
            varStatTable2.getTableModel().addRow("Height","5.8","5.8","5.8","6.5");
            varStatTable2.getTableModel().addRow("Weight","150","150","150","200");
            varStatTable2.getTableModel().addRow("Experience","10","10","10","30");
            varStatTable2.getTableModel().addRow("Rating","4.2","4.2","4.2","5.0");
            RightSubPanel.addComponent(varStatTable2.withBorder(Borders.singleLine()));

 */

//            Panel shellPanel = new Panel();
//            Theme shellTheme = LanternaThemes.getRegisteredTheme("default");
//            shellPanel.setTheme(shellTheme);
//            shellPanel.setLayoutManager(new GridLayout(1));
//            panel.addComponent(shellPanel.withBorder(Borders.singleLine()),BorderLayout.Location.BOTTOM);
//            shellPanel.addComponent(new Label("Enter BeanShell Commands:"));
//            TextBox outputBox = new TextBox(new TerminalSize(50, 5));
//            shellPanel.addComponent(outputBox);
//            outputBox.setReadOnly(true);
//            //shellPanel.addComponent(new Label("CTRL B to activate input"));
//            TextBox inputBox = new TextBox(new TerminalSize(50, 3));
//            shellPanel.addComponent(inputBox);

            // Toggle Listener State



            // Create the window to hold the panel (this is the actual window that appears on the screen)
            BasicWindow mainWindow = new BasicWindow("TDM v 0.01");

            // Set the window to be full-screen and expand its content
            mainWindow.setHints(Arrays.asList(Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW));

            // Add the panel to the window
            mainWindow.setComponent(panel);

            // Add the main window to the GUI and wait for user interaction
            gui.addWindowAndWait(mainWindow);

            // Stop the screen once the GUI has exited
            screen.stopScreen();

        }
         finally {
            if(screen != null) {
                try {
                    /*
                    The close() call here will restore the terminal by exiting from private mode which was done in
                    the call to startScreen(), and also restore things like echo mode
                     */
                    screen.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



    public static Object parseValue(String value) {
        try {
            // Try parsing as an integer
            return Integer.parseInt(value);
        } catch (NumberFormatException e1) {
            try {
                // Try parsing as a float
                return Float.parseFloat(value);
            } catch (NumberFormatException e2) {
                try {
                    // Try parsing as a double
                    return Double.parseDouble(value);
                } catch (NumberFormatException e3) {
                    // Return as string if it's none of the above
                    return value;
                }
            }
        }
    }
    private static void shutDown(Screen screen) throws IOException {
        System.out.println("Shutting down TDM");
        screen.close();
        System.exit(0);
    };

    private static class MemoryTracker {
        public static void getMemoryTracking(Label memoryLabel) throws InterruptedException {
                while (true) {
                        int mb = 1024 * 1024;
                        Runtime instance = Runtime.getRuntime();
                        long totalMemory = instance.totalMemory();
                        long freeMemory = instance.freeMemory();
                        long usedMemory = totalMemory - freeMemory;
                        // Ensure UI updates happen on the UI thread
                        memoryLabel.setText("Memory: " + (usedMemory / mb + " MiB"));
                        Thread.sleep(5000);
                }
        }
    }
    private static void openFileDialog(int index, Label selectedFileLabels, Path[] selectedFilePaths, Path[] selectedFileNames, WindowBasedTextGUI gui) throws SQLException, CsvValidationException, IOException, InterruptedException {
        //todo use the index for recent imports list
        String input = String.valueOf(new FileDialogBuilder()
                .setTitle("Import File")
                .setDescription("Choose a file")
                .setActionLabel("Import")
                .build()
                .showDialog(gui));

        if (!Objects.equals(input, "null")) {
            Path path = Paths.get(input);
            selectedFilePaths[index] = path.toAbsolutePath();
            selectedFileNames[index] = path.getFileName();
            selectedFileLabels.setText(String.valueOf(selectedFileNames[index]));
            //System.out.println("Selected file: " + selectedFileNames[index]);
            //System.out.println("Selected path: " + selectedFilePaths[index]);
            //updateTable(String.valueOf(selectedFileNames[index]));

                try {
                    DataOps.importCSV(String.valueOf(selectedFileNames[index]), String.valueOf(selectedFilePaths[index]));
                } catch (IOException e) {
                    showErrorDialog(gui, "IO error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    showErrorDialog(gui, "Interrupted exception", String.valueOf((e)));
                    //throw new RuntimeException(e);
                } catch (SQLException e) {
                    showErrorDialog(gui, "SQL Error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                }
                try {
                    table.setTableModel(updateTable(String.valueOf(selectedFileNames[index])));
                } catch (SQLException e) {
                    showErrorDialog(gui, "SQL Error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                }
        }
    }
    private static void openSaveDialog( WindowBasedTextGUI gui) {
        //todo ask user what they want to name the file first
        //todo also figure out how to use opencsv to save back to csv and read from db

        String result = new TextInputDialogBuilder()
                .setTitle("Save File as CSV")
                .setDescription("please input a filename")
                .build()
                .showDialog(gui);

        String filename = "TDump.csv";
        if (result != null) {
            if (result.contains(".csv")) {
                filename = result;
            }
            else {
                filename = result + ".csv";
                //System.out.println(filename);
            }
        }

        String directory = String.valueOf(new DirectoryDialogBuilder()
                .setTitle("Select directory to save in")
                .setDescription("Choose a directory")
                .setActionLabel("Select")
                .build()
                .showDialog(gui));

        if (directory != null) {
            String path = directory + File.separator + filename;
            //selectedFilePaths[index] = path.toAbsolutePath();
            //System.out.println("Selected save path: " + selectedFilePaths[index]);
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
                showErrorDialog(gui, "File Save Error", String.valueOf((e)));
                //e.printStackTrace();
            }


        }
    }


    public static TableModel<String> updateTable(String FILENAME) throws SQLException {
        //Table<String> tableData = new Table<>();
//        try (Connection conn = DriverManager.getConnection(getDatabaseURL());
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';")) {
//
//            System.out.println("Tables in the database:");
//            while (rs.next()) {
//                System.out.println(rs.getString("name"));
//            }
//        } catch (SQLException e) {
//            System.err.println("Error: " + e.getMessage());
//        }

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
            //tableData.getTableModel().addRow(headers);

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
    public static void showFullPageScriptDialog(WindowBasedTextGUI gui, String title) {

        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(1));

        TextBox textBox = new TextBox("import dev.noodle.*", TextBox.Style.MULTI_LINE)
                .setLayoutData(GridLayout.createLayoutData(
                        GridLayout.Alignment.FILL, // Fill horizontally
                        GridLayout.Alignment.FILL, // Fill vertically
                        true,  // Grab extra horizontal space
                        true   // Grab extra vertical space
                ))
                .setVerticalFocusSwitching(true);


        panel.addComponent(textBox);

        //change mode with dropdown? for py and beanshell as well as actuall shell terminal

        // TODO process input with beanshell and add save ability to sqlite

        // Submit button
        final String[] result = {null};
        Button submitButton = new Button("Submit", () -> {
            Interpreter i = new Interpreter();
            result[0] = textBox.getText();
            //textBox.getText();
            try {
                Object BSHresult = i.eval(textBox.getText());
                showErrorDialog(gui, "Beanshell Script result", String.valueOf((BSHresult)));
            } catch (EvalError e) {
                showErrorDialog(gui, "Beanshell Script error", (e + "\n" +(e.getErrorText() +"\n")));
                //throw new RuntimeException(e);
            }

//TODO - add program and dependency classpath to beanshell http://beanshell.org/manual/bshmanual.html#Adding_BeanShell_Commands


        });
        Button exitButton = new Button("Exit", () -> {
            dialogWindow.close();
        });


        panel.addComponent(submitButton);
        panel.addComponent(exitButton);
        dialogWindow.setComponent(panel);
        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN)); // Make it full-page

        gui.addWindowAndWait(dialogWindow);
    }
    public static void showErrorDialog(WindowBasedTextGUI textGUI, String title, String message) {
        MessageDialog.showMessageDialog(
                textGUI,
                title,
                message,
                MessageDialogButton.OK
        );
    }


}

