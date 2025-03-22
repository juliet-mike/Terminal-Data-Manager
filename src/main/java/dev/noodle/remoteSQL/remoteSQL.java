
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

package dev.noodle.remoteSQL;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import dev.noodle.modules.dataOps;
import dev.noodle.modules.quickOps;


import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.*;



import static dev.noodle.TDM.*;
import static dev.noodle.modules.dataOps.*;

public class remoteSQL {

    public static void main (String[] args) throws ClassNotFoundException, SQLException {
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
        Class.forName("org.postgresql.Driver"); // for PostgreSQL
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // for SQL Server

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            System.out.println("Available JDBC driver: " + drivers.nextElement().getClass().getName());
        }
        String url2 = "jdbc:sqlserver://192.168.1.151:1433;databaseName=master;encrypt=true;trustServerCertificate=true";
        Connection conn2 = DriverManager.getConnection(url2, "sa", "Wingnut16921$$");
        System.out.println(conn2);

        importRemote();
    }


    public static void importRemote() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        createRecentQueryTable();
        BasicWindow dialogWindow = new BasicWindow("Import Remote SQL Data");
        Panel framePanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel Lpanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel Rpanel = new Panel(new LinearLayout(Direction.VERTICAL));
        dialogWindow.setComponent(framePanel);
        framePanel.addComponent(Lpanel);
        framePanel.addComponent(Lpanel.withBorder(Borders.singleLine("Query panel")));
        Lpanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        framePanel.addComponent(Rpanel);
        framePanel.addComponent(Rpanel.withBorder(Borders.singleLine("Control Panel")));
        Rpanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.CENTERED));
        dialogWindow.setComponent(framePanel);

        Label warnlabel = new Label("Warning: \n Importing too much data may exceed your RAM");
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

        Rpanel.addComponent(urlBox);

        Label dbLabel = new Label("Database Name:");
        Rpanel.addComponent(dbLabel);

        TextBox dbBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);

        Rpanel.addComponent(dbBox);

        Label envLabel = new Label("Other ENV variables: (must follow proper syntax!!): ");
        Rpanel.addComponent(envLabel);

        TextBox envBox = new TextBox(";encrypt=true;trustServerCertificate=true", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);

        Rpanel.addComponent(envBox);

        Label usernameLabel = new Label("Username:");
        Rpanel.addComponent(usernameLabel);

        TextBox usernameBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);

        Rpanel.addComponent(usernameBox);

        Label passwordLabel = new Label("Password:");
        Rpanel.addComponent(passwordLabel);

        TextBox passwordBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                .setPreferredSize(new TerminalSize(30, 2))
                .setVerticalFocusSwitching(true);

        Rpanel.addComponent(passwordBox);

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button submitButton = new Button("Submit", () -> {

            String query = queryBox.getText();
            Connection conn = null;
            try {
                StringBuilder builtURL = new StringBuilder();
                builtURL.append("jdbc:")
                        .append(urlBox.getText())
                        .append(";databaseName=")
                        .append(dbBox.getText());

                insertValuesRecentQueryTable(queryBox.getText(), ("jdbc:"+ urlBox.getText()),
                        dbBox.getText(), envBox.getText(), usernameBox.getText());

                if (!(envBox.getText().isEmpty())) {
                    builtURL.append(envBox.getText());
                }

                String username = usernameBox.getText();
                String password = passwordBox.getText();

                conn = DriverManager.getConnection(builtURL.toString(), username, password);
                 setSelectedFilePaths(envBox.getText());
                 setSelectedFileNames(envBox.getText());
                 if (conn != null) {
                     setTDMTableModel(CustomGetTableModelFromSQL(query, conn));
                     dataOps.TableToInternalSQL(getSelectedFileNames().toString(), getTDMTable());
                 }
                 else {
                     // not sure if this will actually prevent a crash or not better safe than sorry
                     showErrorDialog("Conn Error", "Could not connect to the database");
                 }
            }
            catch (SQLException e) {
                showErrorDialog("SQL MESSAGE", "SQL msg: " + e.getMessage());
            }
        });
        buttonPanel.addComponent(submitButton);

        Button exitButton = new Button("Exit", dialogWindow::close);
        buttonPanel.addComponent(exitButton);

        Button recentButton = new Button("fill recent", () -> {
            BasicWindow recentDialogWindow = new BasicWindow("open recent query");
            RadioBoxList<OptionofRecent> selectionBox1 = recentTableEntriesListFromInternal("*",
                    "RecentQueries",
                    10);
            Panel recentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

            if (!(selectionBox1.isEmpty())) {
                recentPanel.addComponent(selectionBox1);
                recentPanel.addComponent(new Button("Submit", () -> {
                    OptionofRecent selectedOption1 = selectionBox1.getCheckedItem();
                    if (selectedOption1 != null) {
                        String[] values = selectedOption1.getValues();

                        queryBox.setText(values[1]);
                        urlBox.setText(values[2]);
                        dbBox.setText(values[3]);
                        envBox.setText(values[4]);
                        usernameBox.setText(values[5]);

                        recentDialogWindow.close();
                    }
                }));
                Button recentexitButton = new Button("Exit", recentDialogWindow::close);
                recentPanel.addComponent(recentexitButton);
                recentDialogWindow.setComponent(recentPanel);
                globalGui.addWindowAndWait(recentDialogWindow);
            } else {
                dialogWindow.close();
                showErrorDialog("No recent queries found", "No recent query found for this user");
            }
        });
        buttonPanel.addComponent(recentButton);
        Rpanel.addComponent(buttonPanel);

        globalGui.addWindowAndWait(dialogWindow);
    }

//    public static void exportToRemote() {
//        // TODO
//
//    }
//

    public static class OptionofRecent {
        private final int number;
        private final String name;
        private final String[] values;

        public OptionofRecent(int number, String name, String[] values) {
            this.number = number;
            this.name = name;
            this.values = values;
        }

        public int getNumber() {
            return number;
        }
        public String getName() {
            return name;
        }
        public String[] getValues() {
            return values;
        }

        @Override
        public String toString() {
            return number + ": " + name + "  " +Arrays.toString(values);
        }
    }

}
