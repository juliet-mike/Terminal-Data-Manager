package dev.noodle.modules;



import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;


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

    public static void resetArrayOfOperatingColumns(){
        arrayOfOperatingColumns[0] = -1;
        arrayOfOperatingColumns[1] = -1;
    }

    public static class stringMath {
        public static void addColumns (){
            twoWayColSelection("Select columns to add", "plus");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try {
                        double result = Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i)) + Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[1], i));
                        values[i] = Double.toString(result);
                    } catch (NumberFormatException e) {
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn((table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + "+"+ table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1])), values);
                resetArrayOfOperatingColumns();
            }

        }
        public static void subtractColumns (){
            twoWayColSelection("Select columns to subtract", "minus");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try {
                        double result = Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i)) - Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[1], i));
                        values[i] = Double.toString(result);
                    } catch (NumberFormatException e) {
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn((table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + "-"+ table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1])), values);
                resetArrayOfOperatingColumns();
            }

        }
        public static void multiplyColumns (){
            twoWayColSelection("Select columns to multiply", "x");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try {
                        double result = Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i)) * Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[1], i));
                        values[i] = Double.toString(result);
                    } catch (NumberFormatException e) {
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn((table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + "x"+ table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1])), values);
                resetArrayOfOperatingColumns();
            }

        }
        public static void divideColumns (){
            twoWayColSelection("Select columns to divide", "/");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try {
                        double result = Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i)) / Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[1], i));
                        values[i] = Double.toString(result);
                    } catch (ArithmeticException e) {
                        values[i] = "DIV-BY-ZERO";
                    } catch (NumberFormatException e) {
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn((table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + "/"+ table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1])), values);
                resetArrayOfOperatingColumns();
            }

        }
        public static void exponentColumns (){
            oneWayColSelection("Select column to get ABS", "|ABS|");
            if (isValidColumns()) {
                String userInput = new TextInputDialogBuilder()
                        .setTitle("EXPONENT")
                        .setDescription("please input an exponent to be applied across all rows in a column")
                        .setInitialContent("")
                        .build()
                        .showDialog(globalGui);

                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try{
                        values[i] = String.valueOf(Math.pow(Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i)), Double.parseDouble(userInput)));
                    }
                    catch (NumberFormatException e){
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn( "EXP_" + userInput +"_"+ (table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0])), values);
                resetArrayOfOperatingColumns();
            }

        }
        public static void absColumns (){
            oneWayColSelection("Select column to get ABS", "|ABS|");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try{
                        values[i] = String.valueOf(Math.abs(Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i))));
                    }
                    catch (NumberFormatException e){
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn( "ABS_" + (table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0])), values);
                resetArrayOfOperatingColumns();

            }
        }
        public static void roundColumns (){
            oneWayColSelection("Select column to round", "");
            if (isValidColumns()) {
                String[] values = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    try{
                        values[i] = String.valueOf(Math.round(Double.parseDouble(table.getTableModel().getCell(arrayOfOperatingColumns[0], i))));
                    }
                    catch (NumberFormatException e){
                        for (int j = 0; j < table.getTableModel().getRowCount(); j++) {
                            values[j] = "ERROR";
                        }
                        break;
                    }
                }
                table.getTableModel().addColumn( "ROUND_" + (table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0])), values);
                resetArrayOfOperatingColumns();
            }
        }


    }


    public static void customSQLWindow() {
        BasicWindow dialogWindow = new BasicWindow("Custom SQL internal");
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

    public static void oneWayColSelection(String title, String operatorText) {
        //use radio button to select one column
        BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new GridLayout(2));
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
        Button exitButton = new Button("Exit", () -> {
            dialogWindow.close();
        });
        panel.addComponent(exitButton);
        globalGui.addWindowAndWait(dialogWindow);
    }

    public static void twoWayColSelection(String title, String operatorText) {
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
        resetArrayOfOperatingColumns();
    }
//    public static void addColumn(String header) {
//        String[] values = new String[table.getTableModel().getRowCount()];
//        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
//            values[i] = "";
//        }
//        table.getTableModel().addColumn((header), values);
//    }
    public static void renameColumn() {
        //System.out.println("Rename Column");
        oneWayColSelection("Select a Column to Rename", " ");
        if (isValidColumns()){
            String old = table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]);
            String result = new TextInputDialogBuilder()
                    .setTitle("Rename Column")
                    .setDescription("Please input a new column name")
                    .setInitialContent("")
                    .build()
                    .showDialog(globalGui);

            if ((result != null) || (!result.isEmpty())) {
                table.getTableModel().setColumnLabel(arrayOfOperatingColumns[0], result);
            } else {
                table.getTableModel().setColumnLabel(arrayOfOperatingColumns[0], old);
            }
        }
        resetArrayOfOperatingColumns();
    }

    public static void mergeColumn() {
        twoWayColSelection("Select columns to merge", "merge with");
        if (isValidColumns()) {
        String label = table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]) + table.getTableModel().getColumnLabel(arrayOfOperatingColumns[1]);
        String[] values = new String[table.getTableModel().getRowCount()];
        List<String> first = getTableColumn(arrayOfOperatingColumns[0]);
        List<String> second = getTableColumn(arrayOfOperatingColumns[1]);
        for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
            values[i] = first.get(i) + second.get(i);
        }
        resetArrayOfOperatingColumns();
        table.getTableModel().addColumn((label), values);
        } else {
            showErrorDialog("invalid columns", "invalid columns selected \n array of operating columns may not have been reset on the last operation \n ignore this message if you canceled the merge intentionally");
        }
    }
    public static void splitColumn() {
        oneWayColSelection("Select a Column to Split", "you will be asked for a delimiter to split the col next");
        if (isValidColumns()) {
            String origLabel = table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]);
            List<String> data = getTableColumn(arrayOfOperatingColumns[0]);
            String userInput = new TextInputDialogBuilder()
                    .setTitle("Delimiter")
                    .setDescription("Please input a Delimiter to split the column \n the first instance of the delimiter will start the split operation")
                    .setInitialContent("")
                    .build()
                    .showDialog(globalGui);

            if ((userInput != null) || (!userInput.isEmpty())) {
                String[] col1 = new String[table.getTableModel().getRowCount()];
                String[] col2 = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < data.size(); i++) {
                    try {
                        String[] result = (data.get(i).split(userInput, 2));
                        col1[i] = (result[0]);
                        col2[i] = (result[1]);
                    }
                    catch (Exception e) {
                        showErrorDialog("Split Error", e.getMessage() + "there was an error splitting this column most likely \n due to inconsistent data or delimiter not found");
                        break;
                    }

                }
                int colcount = table.getTableModel().getColumnCount();
                table.getTableModel().addColumn((origLabel + "split_COL" + (colcount + 1)), col1);
                table.getTableModel().addColumn((origLabel + "split_COL" + (colcount + 2)), col2);
            }

        }
        resetArrayOfOperatingColumns();
    }
    public static void colContains(){
        oneWayColSelection("Select a Column to check for", "");
        if (isValidColumns()) {
            String origLabel = table.getTableModel().getColumnLabel(arrayOfOperatingColumns[0]);
            String userInput = new TextInputDialogBuilder()
                    .setTitle("Contains")
                    .setDescription("input a value to check if the column contains it")
                    .setInitialContent("")
                    .build()
                    .showDialog(globalGui);

            if ((userInput != null) || (!userInput.isEmpty())) {
                String[] colData = new String[table.getTableModel().getRowCount()];
                for (int i = 0; i < table.getTableModel().getRowCount(); i++) {
                    if (getTableCell((arrayOfOperatingColumns[0]), i).contains(userInput)) {
                        colData[i] = "TRUE";
                    } else {
                        colData[i] = "FALSE";
                    }
                }
                table.getTableModel().addColumn((origLabel + "_CONTAINS_" + userInput), colData);
            }
        }
        resetArrayOfOperatingColumns();
    }

    public static void removeColumn() {
        oneWayColSelection("Select a Column to remove", "");
        if (isValidColumns()) {
            table.getTableModel().removeColumn(arrayOfOperatingColumns[0]);
        }
        resetArrayOfOperatingColumns();
    }

}




