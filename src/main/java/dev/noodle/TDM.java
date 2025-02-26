package dev.noodle;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.DirectoryDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.WaitingDialog;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import dev.noodle.models.DBops;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


import java.sql.*;

public class TDM {
    //static double memory = 0;

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
            // TODO on this - it fluctuates memory usage back and forth maybe change to just call after new objects are loaded to memory?
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
            memoryThread.setDaemon(true); // Set the thread as a daemon thread
            memoryThread.start(); // Start the thread


            //shows active/ loading file -- interacted with by openFileDialog() method
            Label selectedFileLabel0;
            topSubPanel.addComponent(selectedFileLabel0 = new Label("Select a file"), GridLayout.createHorizontallyEndAlignedLayoutData(4));


            Menu fileMenu = new Menu("File");
            Label finalSelectedFileLabel = selectedFileLabel0;
            fileMenu.add(new MenuItem("Import New File", () -> openFileDialog(0, finalSelectedFileLabel, selectedFilePaths, selectedFileNames, gui)));

            // TODO - implement save as by using directory menu and have a popup or something that asks filename
            //new method - open PATH dialog after having a user input popup
            fileMenu.add(new MenuItem("Save As", () -> openDirDialog(0, finalSelectedFileLabel, selectedFilePaths, selectedFileNames, gui)));



            Menu helpMenu = new Menu("Help");

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

            // add above objects to menu object
            menubar.add(fileMenu).add(helpMenu); //.add(openMenu);


            // init new panel
            Panel leftSubPanel = new Panel();
            leftSubPanel.setTheme(theme);
            leftSubPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            panel.addComponent(leftSubPanel.withBorder(Borders.singleLine()), BorderLayout.Location.LEFT); // Place button at the bottom of the panel
            leftSubPanel.addComponent(new Label("Dataframe Operations").setForegroundColor(TextColor.ANSI.BLUE)); // maybe move this to the panel title
            //leftSubPanel.addComponent(new EmptySpace(new TerminalSize(2, 1)));
            //final Path[] fileName = {null};
            //final Path[] filePath = {null};

            TerminalSize size = new TerminalSize(17, 20);
            ActionListBox dataframeActionListBox = new ActionListBox(size);

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
            dataframeActionListBox.addItem("Exponent", new Runnable() {
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
            MidSubPanel.setLayoutManager(new GridLayout(2));
            panel.addComponent(MidSubPanel.withBorder(Borders.doubleLine()), BorderLayout.Location.CENTER);
            // Create the table and store it in a variable
            String Index = "index";
            //List<String[]> data = DBops.updateGUI("Comp.csv");
            Table<String> table = new Table<>( "1", "2", "3", "4", "5");


            // Add the table to the panel
            MidSubPanel.addComponent(table.withBorder(Borders.doubleLine()));
            // Add a row to the table

            for (int i = 0; i < 200; i++) {
                table.getTableModel().addRow("1", "2", "3", "4", "5");
            }


            table.setCellSelection(true);
            table.setSelectAction(new Runnable() {
                @Override
                public void run() {
                    int selectedRow = table.getSelectedRow();
                    int selectedCol = table.getSelectedColumn();
                    String cellValue = table.getTableModel().getCell(selectedCol, selectedRow);
                    System.out.println("Selected cell (" + selectedCol + "," + selectedRow + "): " + cellValue);
                }
            });
            // mid/center panel ends here


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




            // Create the window to hold the panel (this is the actual window that appears on the screen)
            BasicWindow mainWindow = new BasicWindow("TDM v 0.01");

            // Set the window to be full-screen and expand its content
            mainWindow.setHints(Arrays.asList(Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW));

            // Add the panel to the window
            mainWindow.setComponent(panel);



            // Add the main window to the GUI and wait for user interaction
            gui.addWindowAndWait(mainWindow);


            // Stop the screen once the GUI has exited
            //screen.stopScreen();



        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(screen != null) {
                try {
                    /*
                    The close() call here will restore the terminal by exiting from private mode which was done in
                    the call to startScreen(), and also restore things like echo mode and intr
                     */
                    screen.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
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

    public static class MemoryTracker {
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
    private static void openFileDialog(int index, Label selectedFileLabels, Path[] selectedFilePaths, Path[] selectedFileNames, WindowBasedTextGUI gui) {
        //TODO add some method of either detecting headers or asking the user if there are headers -- see left panel button
        Theme theme2 = LanternaThemes.getRegisteredTheme("bigsnake");

        // okay the file dialouge is not modifiable so we will see how this goes
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
            System.out.println("Selected file: " + selectedFileNames[index]);
            System.out.println("Selected path: " + selectedFilePaths[index]);
        } else {
            System.out.println("User canceled the dialog.");
        }
    }
    private static void openDirDialog(int index, Label selectedFileLabels, Path[] selectedFilePaths, Path[] selectedFileNames, WindowBasedTextGUI gui) {
        //TODO add to save button and then change required vars and returns
        // okay the file dialouge is not modifiable so we will see how this goes
        String input = String.valueOf(new DirectoryDialogBuilder()
                .setTitle("Select directory to save in")
                .setDescription("Choose a directory")
                .setActionLabel("Select")
                .build()
                .showDialog(gui));

        if (!Objects.equals(input, "null")) {
            Path path = Paths.get(input);
            selectedFilePaths[index] = path.toAbsolutePath();
            selectedFileLabels.setText(String.valueOf(selectedFileNames[index]));
            System.out.println("Selected save path: " + selectedFilePaths[index]);
        } else {
            System.out.println("User canceled the dialog.");
        }
    }

        public static void runTaskWithWaitingDialog(TextGUI textGUI) {
            // Create and display the WaitingDialog
            WaitingDialog waitingDialog = WaitingDialog.createDialog("Please Wait", "Processing...");
            new Thread(() -> {
                try {
                    // Simulate a long-running task
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Close the dialog after the task is complete
                    waitingDialog.close();
                }
            }).start();

            // Show the dialog and block until it's closed
            waitingDialog.showDialog((WindowBasedTextGUI) textGUI);
        }


}

