package dev.noodle.modules;



import com.googlecode.lanterna.gui2.*;
import dev.noodle.TDM;

import java.sql.*;
import java.util.Arrays;
import java.util.List;


import static dev.noodle.TDM.*;
import static dev.noodle.modules.DataOps.*;

public class quickOps {

    public static void customSQL( MultiWindowTextGUI gui) {
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
                showErrorDialog(gui, "SQL Error", e.getMessage());
                //throw new RuntimeException(e);
            }
        });
        Button exitButton = new Button("Exit", dialogWindow::close);
        panel.addComponent(submitButton);
        panel.addComponent(exitButton);
        dialogWindow.setComponent(panel);
        dialogWindow.setHints(List.of(Window.Hint.FULL_SCREEN)); // Make it full-page
        gui.addWindowAndWait(dialogWindow);

    }
    private static void collumnMath (String A , String B) {
        //gonna need to check type and all
        //something like this but not quite
//        int sum = Integer.parseInt(A) + Integer.parseInt(B);
//        System.out.println(sum);
    }


    public static void addCollumn () {

    }





    public static void mergeCollumn (int A , int B) {
        String s = table.getTableModel().getColumnLabel(A)+ table.getTableModel().getColumnLabel(B);
        String[] values = new String[table.getTableModel().getRowCount()];
        List<String> first = getTableColumn(A);
        List<String> second = getTableColumn(B);

        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
            values[i] = first.get(i)+second.get(i);
        }
        table.getTableModel().addColumn((s), values);
    }




}







