package dev.noodle.remoteSQL;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import dev.noodle.modules.DataOps;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static dev.noodle.TDM.*;
import static dev.noodle.modules.DataOps.CustomGetTableModelFromSQL;

public class remoteSQL {

    public static void main (String[] args) {
        //use for testing new UI layout only

        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();

            // Create a Panel to hold components (Buttons, Labels, TextBoxes, etc.)
            Panel panel = new Panel();
            panel.setLayoutManager(new BorderLayout());

            Theme theme = LanternaThemes.getRegisteredTheme("bigsnake");
            // Create a GUI manager and set the screen for the window
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.setTheme(theme);
            globalGui = gui;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        importRemote();
    }


    public static void importRemote(){
        BasicWindow dialogWindow = new BasicWindow("Import Remote SQL Data");
        Panel framePanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel Lpanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel Rpanel = new Panel(new LinearLayout(Direction.VERTICAL));
        dialogWindow.setComponent(framePanel);
        framePanel.addComponent(Lpanel);
        framePanel.addComponent(Rpanel);
        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.CENTERED));
        dialogWindow.setComponent(framePanel);

        framePanel.addComponent(Lpanel.withBorder(Borders.singleLine("Query panel"))); // Optional border for visualization
        framePanel.addComponent(Rpanel.withBorder(Borders.singleLine("Control Panel"))); // Optional border

        Label warnlabel = new Label("Warning: \n Importing too much data \n may exceed your RAM");
        Rpanel.addComponent(warnlabel);

        TextBox queryBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)) // Expand horizontally
                .setPreferredSize(new TerminalSize(40, 20)) // Set explicit width and height
                .setVerticalFocusSwitching(true);

        Lpanel.addComponent(queryBox);

        Label urlLabel = new Label("URL:");
        Rpanel.addComponent(urlLabel);

        TextBox urlBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);
        String url = urlBox.getText();

        Rpanel.addComponent(urlBox);

        Label usernameLabel = new Label("Username:");
        Rpanel.addComponent(usernameLabel);

        TextBox usernameBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);
        String username = usernameBox.getText();

        Rpanel.addComponent(usernameBox);

        Label passwordLabel = new Label("Password:");
        Rpanel.addComponent(passwordLabel);

        TextBox passwordBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);
        String password = passwordBox.getText();

        Rpanel.addComponent(passwordBox);

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button submitButton = new Button("Submit", () -> {
            String query = queryBox.getText();
            Connection conn = null;
            try {
                 conn = DriverManager.getConnection(url, username, password);
                 setSelectedFilePaths(url);
                 setSelectedFileNames(url);
                 if (conn != null) {
                     setTDMTableModel(CustomGetTableModelFromSQL(query, conn));
                     DataOps.TableToInternalSQL(getSelectedFileNames().toString(), getTDMTable());
                 }
                 else {
                     // not sure if this will actually prevent a crash or not better safe than sorry
                     showErrorDialog("Conn Error", "Could not connect to the database");
                 }

            }
            catch (SQLException e) {
                showErrorDialog("SQL Error", "SQL Error: " + e.getMessage());
            }
            finally {
                if (conn != null) {
                    dialogWindow.close();
                }
            }

        });
        buttonPanel.addComponent(submitButton);

        Button exitButton = new Button("Exit", dialogWindow::close);
        buttonPanel.addComponent(exitButton);


        Rpanel.addComponent(buttonPanel);

        globalGui.addWindowAndWait(dialogWindow);
    }

    public static void exportToRemote() {
        // TODO add this but refactor import remote first
    }


}
