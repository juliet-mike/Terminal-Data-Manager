package dev.noodle;

import bsh.*;
import bsh.classpath.BshClassLoader;
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
import dev.noodle.modules.DataOps;
import dev.noodle.modules.quickOps;


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


import static dev.noodle.modules.DataOps.*;
import static dev.noodle.modules.quickOps.*;




public class TDM {

    public static Table<String> table = new Table<>( "1", "2", "3", "4", "5");
    public static Path selectedFileNames = Path.of("default"); //= new Path;
    public static Path selectedFilePaths = Path.of("default"); //= new Path;

    // should this be its own class?
    public static Table<String> getTable() {
        return table;
    }
    public static List<String> getTablerow(int row) {
        return table.getTableModel().getRow(row);
    }
    public static List<String> getTableColumn(int column) {
        List<String> columnData = new ArrayList<>();
        TableModel<String> model = table.getTableModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            columnData.add(model.getCell(column, row));
        }
        return columnData;
    }
    public static String getTableCell(int column, int row) {
        return table.getTableModel().getCell(column, row);
    }

    // "hooks" for gui extensions
    public static MultiWindowTextGUI globalGui;
    public static Panel globalFramePanel;
    public static Panel globalTopSubPanel;
    public static MenuBar globalMenuBar;
        public static Menu globalFilelMenu;
        public static Menu globalHelpMenu;
        public static Menu globalScriptMenu;
    public static Panel globalLeftSubPanel;
    public static ActionListBox globalDataframeActionListBox;
    public static Panel globalMidSubPanel;


    public static void main(String[] args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");



        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();

            // Create a Panel to hold components (Buttons, Labels, TextBoxes, etc.)
            Panel panel = new Panel();
            globalFramePanel = panel;
            panel.setLayoutManager(new BorderLayout());

            Theme theme = LanternaThemes.getRegisteredTheme("bigsnake");
            // Create a GUI manager and set the screen for the window
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.setTheme(theme);
            globalGui = gui;

            Panel topSubPanel = new Panel();
            globalTopSubPanel = topSubPanel;
            panel.addComponent(topSubPanel.withBorder(Borders.doubleLineBevel()), BorderLayout.Location.TOP);
            topSubPanel.setLayoutManager(new GridLayout(6));
            topSubPanel.setTheme(theme);

            MenuBar menubar = new MenuBar();
            globalMenuBar = menubar;
            topSubPanel.addComponent(menubar);

            Label memoryLabel;
            topSubPanel.addComponent(memoryLabel = new Label("memory usage"), GridLayout.createHorizontallyEndAlignedLayoutData(3));
            MemoryTracker.startMemoryTracker(memoryLabel);

            //shows active/ loading file -- interacted with by openFileDialog() method
            Label selectedFileLabel0;
            topSubPanel.addComponent(selectedFileLabel0 = new Label("Select a file"), GridLayout.createHorizontallyEndAlignedLayoutData(4));

            Menu fileMenu = new Menu("File");
            globalFilelMenu = fileMenu;
            Label finalSelectedFileLabel = selectedFileLabel0;
            fileMenu.add(new MenuItem("Import New File", () -> {
                try {
                    openFileDialog( finalSelectedFileLabel, gui);
                } catch (SQLException | CsvValidationException | IOException | InterruptedException e) {
                    //throw new RuntimeException(e);
                    showErrorDialog( "Error Importing Table", e.getMessage());
                }
            }));
            fileMenu.add(new MenuItem("Open Recent", DataOps::opnRecentFileFromInternal));

            //new method - open PATH dialog after having a user input popup
            fileMenu.add(new MenuItem("Save to Internal Memory", () -> {
                try {
                    DataOps.TabletoSQL(selectedFileNames.toString(), table);
                } catch (SQLException e) {
                    showErrorDialog( "ERROR", "there was an error saving the current table \n check to make sure you have no SQL strings in the table");
                    //throw new RuntimeException(e);
                }
            }));
            fileMenu.add(new MenuItem("Save As CSV", TDM::openSaveDialog));
            //todo add user prefs and its menu
            //Menu settingsMenu = new Menu("Settings");
            //settingsMenu.add(new MenuItem("User Preferences", () -> System.out.println("User Preferences selected")));
            //fileMenu.add(settingsMenu);

            Screen finalScreen = screen; //why is this needed
            fileMenu.add(new MenuItem("Exit", () -> {
                // First, close the screen.
                try {
                    //finalScreen.close();
                    shutDown(finalScreen);
                } catch (IOException e) {
                    showErrorDialog("ERROR", ("there was an error shutting down TDM" + e.getMessage()));
                    //e.printStackTrace();
                }
                // Then exit the application.
            }));
            Menu helpMenu = new Menu("Help");
            globalHelpMenu = helpMenu;
            Menu scriptMenu = new Menu("Scripts");
            globalScriptMenu = scriptMenu;
            scriptMenu.add(new MenuItem("Open Script Editor", () -> showFullPageScriptDialog( "Script Editor")));

            // add above objects to menu object
            menubar.add(fileMenu).add(scriptMenu).add(helpMenu); //.add(openMenu);

            // init new panel
            Panel leftSubPanel = new Panel();
            globalLeftSubPanel = leftSubPanel;
            leftSubPanel.setTheme(theme);
            leftSubPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            panel.addComponent(leftSubPanel.withBorder(Borders.singleLine()), BorderLayout.Location.LEFT); // Place button at the bottom of the panel
            leftSubPanel.addComponent(new Label("Quick Operations").setForegroundColor(TextColor.ANSI.BLUE)); // maybe move this to the panel title

            TerminalSize size = new TerminalSize(17, 20);
            ActionListBox dataframeActionListBox = new ActionListBox(size);
            globalDataframeActionListBox = dataframeActionListBox;

//            dataframeActionListBox.addItem("Quick CAL", new Runnable() {
//                @Override
//                public void run() {
//                    // Code to run when action activated
//                }
//            });

            dataframeActionListBox.addItem("Custom SQL", new Runnable() {
                @Override
                public void run() {
                    quickOps.customSQLWindow();
                }
            });
            dataframeActionListBox.addItem("Add", new Runnable() {
                @Override
                public void run() {

                }
            });
            dataframeActionListBox.addItem("Subtract", new Runnable() {
                @Override
                public void run() {

                }
            });
            dataframeActionListBox.addItem("Multiply", new Runnable() {
                @Override
                public void run() {

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
                    mergeColumn();
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
                    addColumn();
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
            globalMidSubPanel = MidSubPanel;
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
                    }
                }
            });
            // mid/center panel ends here

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
            MemoryTracker.stopMemoryTracker();
            if(screen != null) {
                    /*
                    The close() call here will restore the terminal by exiting from private mode which was done in
                    the call to startScreen(), and also restore things like echo mode
                     */
                    screen.close();
            }
        }
    }

    private static void shutDown(Screen screen) throws IOException {
        //System.out.println("Shutting down TDM");
        screen.close();
        System.exit(0);
    }

    public static void openFileDialog( Label selectedFileLabels, MultiWindowTextGUI gui) throws SQLException, CsvValidationException, IOException, InterruptedException {
        //todo use the index for recent imports list
        String input = String.valueOf(new FileDialogBuilder()
                .setTitle("Import File")
                .setDescription("Choose a file")
                .setActionLabel("Import")
                .build()
                .showDialog(gui));

        if (!Objects.equals(input, "null")) {
            Path path = Paths.get(input);
            selectedFilePaths = path.toAbsolutePath();
            selectedFileNames = path.getFileName();
            selectedFileLabels.setText(String.valueOf(selectedFileNames));

                try {
                    DataOps.importCSV(String.valueOf(selectedFileNames), String.valueOf(selectedFilePaths));
                } catch (IOException e) {
                    showErrorDialog( "IO error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    showErrorDialog( "Interrupted exception", String.valueOf((e)));
                    //throw new RuntimeException(e);
                } catch (SQLException e) {
                    showErrorDialog( "SQL Error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                }
                try {
                    table.setTableModel(TableFromSQL(String.valueOf(selectedFileNames)));
                } catch (SQLException e) {
                    showErrorDialog( "SQL Error", String.valueOf((e)));
                    //throw new RuntimeException(e);
                }
        }
    }
    public static void openSaveDialog() {
        String result = new TextInputDialogBuilder()
                .setTitle("Save File as CSV")
                .setDescription("please input a filename")
                .build()
                .showDialog(globalGui);

        String filename = "";
        if (result == null) {return;}
        if (!Objects.equals(result, "")) {
            if (!result.contains(".csv")) {
            filename = result + ".csv";
            }
        } else {
            filename = "TDM_dump.csv";
        }

        String directory = String.valueOf(new DirectoryDialogBuilder()
                .setTitle("Select directory to save in")
                .setDescription("Choose a directory")
                .setActionLabel("Select")
                .build()
                .showDialog(globalGui));

        if (directory != null) {
            Table<String> copiedTable = table;
            if (Objects.equals(copiedTable.getTableModel().getColumnLabel(1), "ID"));
            {
                copiedTable.getTableModel().removeColumn(1);
            }

            String path = directory + File.separator + filename;
            //selectedFilePaths[index] = path.toAbsolutePath();
            //System.out.println("Selected save path: " + selectedFilePaths[index]);
            try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
                int columnCount = copiedTable.getTableModel().getColumnCount();
                int rowCount = copiedTable.getTableModel().getRowCount();

                String[] headers = new String[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    headers[col] = copiedTable.getTableModel().getColumnLabel(col);
                }
                writer.writeNext(headers);
                // Write table data
                for (int row = 0; row < rowCount; row++) {
                    List<String> rowData = new ArrayList<>();
                    for (int col = 0; col < columnCount; col++) {
                        rowData.add(copiedTable.getTableModel().getCell(col, row));
                    }
                    writer.writeNext(rowData.toArray(new String[0]));
                }
                //System.out.println("CSV file saved at: " + path);
            } catch (IOException e) {
                showErrorDialog( "File Save Error", String.valueOf((e)));
                //e.printStackTrace();
            }
        }
    }

    public static void showFullPageScriptDialog(String title) {
        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(4));

        TextBox textBox = new TextBox("  addClassPath(\"./lib/*\") \n import *;", TextBox.Style.MULTI_LINE)
                .setLayoutData(GridLayout.createLayoutData(
                        GridLayout.Alignment.FILL, // Fill horizontally
                        GridLayout.Alignment.FILL, // Fill vertically
                        true,  // Grab extra horizontal space
                        true   // Grab extra vertical space
                ))
                .setVerticalFocusSwitching(true);

        panel.addComponent(textBox);
        //change mode with dropdown? for py and beanshell as well as actuall shell terminal

        Button submitButton = new Button("Submit", () -> {
            Interpreter i = new Interpreter();
            //textBox.getText();
            try {
                BshClassLoader.getSystemResource("TDM.jar");

                Object BSHresult = i.eval(textBox.getText());
                showErrorDialog( "Beanshell Script result", String.valueOf((BSHresult)));

            } catch (EvalError e) {
                showErrorDialog( "Beanshell Script error", (e.getErrorText()));
                //throw new RuntimeException(e);
            }
//TODO - add program and dependency classpath to beanshell http://beanshell.org/manual/bshmanual.html#Adding_BeanShell_Commands
        });
        Button exitButton = new Button("Exit", dialogWindow::close);
        Button saveButton = new Button("Save Script", () -> {
            try {
                // TODO add script save ability to sqlite
            } catch (Exception e) {
                showErrorDialog("save script error", String.valueOf((e)));
                //throw new RuntimeException(e);
            }
    });
        panel.addComponent(saveButton);
        panel.addComponent(submitButton);
        panel.addComponent(exitButton);
        dialogWindow.setComponent(panel);
        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN)); // Make it full-page
        globalGui.addWindowAndWait(dialogWindow);
    }

    public static void showErrorDialog(String title, String message) {
        MessageDialog.showMessageDialog(
                globalGui,
                title,
                message,
                MessageDialogButton.OK
        );
    }
   public static class MemoryTracker {
        private static boolean memTracking = false;
        public static void startMemoryTracker(Label memoryLabel) {
            memTracking = true;
            Thread memoryThread = new Thread(() -> {
                while (memTracking) {
                    try {
                        displayCurrentMemoryUsage(memoryLabel);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            memoryThread.setDaemon(true);
            memoryThread.start();
        }
        public static void stopMemoryTracker() {
            memTracking = false;
        }
        private static void displayCurrentMemoryUsage(Label memoryLabel) throws InterruptedException {
                memoryLabel.setText("Memory: " + getUsedMemory() +  " MiB");
                Thread.sleep(5000);
        }
        public static int getUsedMemory() {
            int mb = 1024 * 1024;
            Runtime instance = Runtime.getRuntime();
            long totalMemory = instance.totalMemory();
            long freeMemory = instance.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            return (int) (usedMemory / mb);
        }
        public static float getUsedMemoryPercent() {
            Runtime instance = Runtime.getRuntime();
            long totalMemory = instance.totalMemory();
            long freeMemory = instance.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            return ((float) usedMemory / totalMemory) * 100;
        }
    }
}





