package dev.noodle.modules;



import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import dev.noodle.TDM;

import java.sql.*;
import java.util.List;


import static dev.noodle.TDM.*;
import static dev.noodle.modules.DataOps.*;


public class quickOps {
    public static int[] arrayOfOperatingColumns =  {-1, -1};

    static class Option {
        private final int number;
        private final String name;

        public Option(int number, String name) {
            this.number = number;
            this.name = name;
        }

        public int getNumber() {
            return number;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return number + ": " + name;
        }
    }

    public static boolean isValidColumns() {
        if (!(arrayOfOperatingColumns[1] == -1) || !(arrayOfOperatingColumns[0] == -1)) {
            return true;
        } else {
            //showErrorDialog("invalid column selection", "the current value of columns 0 and 1 stored in arrayOfOperatingColumns is invalid (-1) \n this operation cannot proceed please try again with different values");
            return false;
        }

    }

    public static void customSQLWindow() {
        BasicWindow dialogWindow = new BasicWindow("Custom SQL operation");
        Panel panel = new Panel(new GridLayout(4));

        TextBox textBox = new TextBox("", TextBox.Style.MULTI_LINE)
                .setLayoutData(GridLayout.createLayoutData(
                        GridLayout.Alignment.FILL, // Fill horizontally
                        GridLayout.Alignment.FILL, // Fill vertically
                        true,  // Grab extra horizontal space
                        true   // Grab extra vertical space
                ))
                .setVerticalFocusSwitching(true);
        panel.addComponent(textBox);

        Button submitButton = new Button("Submit", () -> {
            try {
                TDM.table.setTableModel(CustomUpdateTableFromSQL(textBox.getText()));
            } catch (SQLException e) {
                showErrorDialog("SQL Error", e.getMessage());
            }
        });
        Button exitButton = new Button("Exit", dialogWindow::close);
        panel.addComponent(submitButton);
        panel.addComponent(exitButton);
        dialogWindow.setComponent(panel);
        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN)); // Make it full-page
        globalGui.addWindowAndWait(dialogWindow);
    }


    public static void twoWaySelection(String title, String operatorText) {
        //use radio button to select the two collumns
        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(3));
        TerminalSize size = new TerminalSize(14, 10);
        RadioBoxList<Option> selectionBox1 = new RadioBoxList<Option>(size);
        for (int i = 0; i < (table.getTableModel().getColumnCount()); i++) {
            selectionBox1.addItem((new Option(i, table.getTableModel().getColumnLabel(i))));
        }
        panel.addComponent(selectionBox1);

        Label label = new Label(operatorText);
        panel.addComponent(label);

        RadioBoxList<Option> selectionBox2 = new RadioBoxList<Option>(size);
        for (int i = 0; i < (table.getTableModel().getColumnCount()); i++) {
            selectionBox2.addItem((new Option(i, table.getTableModel().getColumnLabel(i))));
        }
        panel.addComponent(selectionBox2);

        dialogWindow.setComponent(panel);

        panel.addComponent(new Button("Submit", () -> {
            Option selectedOption1 = selectionBox1.getCheckedItem();
            Option selectedOption2 = selectionBox2.getCheckedItem();
            if (selectedOption1 != null && selectedOption2 != null) {
                int selectedNumber1 = selectedOption1.getNumber();
                int selectedNumber2 = selectedOption2.getNumber();
                arrayOfOperatingColumns[0] = selectedNumber1;
                arrayOfOperatingColumns[1] = selectedNumber2;
                dialogWindow.close();
            }
        }));

        Button exitButton = new Button("Exit", dialogWindow::close);
        panel.addComponent(exitButton);
        globalGui.addWindowAndWait(dialogWindow);
    }


    public static void addCollumn() {

    }

    public static void mergeColumn() {
        twoWaySelection("Select columns to merge", "merge with");
        if (isValidColumns()) {
        String label = table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1]);
        String[] values = new String[table.getTableModel().getRowCount()];
        List<String> first = getTableColumn(arrayOfOperatingColumns[0]);
        List<String> second = getTableColumn(arrayOfOperatingColumns[1]);
        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
            values[i] = first.get(i) + second.get(i);
        }
        arrayOfOperatingColumns[0] = -1; arrayOfOperatingColumns[1] = -1;
        table.getTableModel().addColumn((label), values);
        } else {
            return;
        }
    }
}


















