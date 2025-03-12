package dev.noodle.modules;



import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import dev.noodle.TDM;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.*;
import java.util.List;
import java.util.Objects;


import static dev.noodle.TDM.*;
import static dev.noodle.modules.DataOps.*;

// this class is cursed but it makes things work and display how i want so we are gonna live with it
public class quickOps {
    public static int[] arrayOfOperatingColumns =  {-1, -1};

    public static class Option {
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
                table.setTableModel(CustomUpdateTableFromSQL(textBox.getText()));
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

    public static void oneWaySelection(String title, String operatorText) {
        //use radio button to select one column
        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(3));
        TerminalSize size = new TerminalSize(20, 10);
        RadioBoxList<Option> selectionBox1 = new RadioBoxList<Option>(size);
        for (int i = 0; i < (table.getTableModel().getColumnCount()); i++) {
            selectionBox1.addItem((new Option(i, table.getTableModel().getColumnLabel(i))));
        }
        panel.addComponent(selectionBox1);
        Label label = new Label(operatorText);
        panel.addComponent(label);

        dialogWindow.setComponent(panel);

        panel.addComponent(new Button("Submit", () -> {
            Option selectedOption1 = selectionBox1.getCheckedItem();
            if (selectedOption1 != null) {
                int selectedNumber1 = selectedOption1.getNumber();
                arrayOfOperatingColumns[0] = selectedNumber1;
                dialogWindow.close();
            }
        }));
        Button exitButton = new Button("Exit", dialogWindow::close);
        panel.addComponent(exitButton);
        globalGui.addWindowAndWait(dialogWindow);
    }



    public static void twoWaySelection(String title, String operatorText) {
        //use radio button to select the two collumns
        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(3));
        TerminalSize size = new TerminalSize(20, 10);
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

    public static void addColumn() {
        String result = new TextInputDialogBuilder()
                .setTitle("New Column")
                .setDescription("please input a column name")
                .build()
                .showDialog(globalGui);

        if (result == null) {return;}
        if (Objects.equals(result, "")) {
            return;
        }
        String[] values = new String[table.getTableModel().getRowCount()];
        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
            values[i] = "";
        }
        table.getTableModel().addColumn((result), values);
    }

    public static void addColumn(String header) {
        String[] values = new String[table.getTableModel().getRowCount()];
        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
            values[i] = "";
        }
        table.getTableModel().addColumn((header), values);
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
            showErrorDialog("invalid columns", "invalid columns selected array of operating columns may not have been reset on the last operation \n ignore this message if you canceled the merge intentionally");
            return;
        }
    }
}


















