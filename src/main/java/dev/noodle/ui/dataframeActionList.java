package dev.noodle.ui;

import com.googlecode.lanterna.gui2.ActionListBox;
import dev.noodle.modules.quickOps;

import static dev.noodle.modules.quickOps.*;
import static dev.noodle.modules.quickOps.removeColumn;
import static dev.noodle.TDM.globalDataframeActionListBox;

public class dataframeActionList {
    public static void initActionListBox() {

//            dataframeActionListBox.addItem("Quick CAL", new Runnable() {
//                @Override
//                public void run() {
//                    // Code to run when action activated
//                }
//            });

        globalDataframeActionListBox.addItem("Custom SQL", new Runnable() {
            @Override
            public void run() {
                customSQLWindow();
            }
        });
        globalDataframeActionListBox.addItem("Add", new Runnable() {
            @Override
            public void run() {
                stringMath.addColumns();
            }
        });
        globalDataframeActionListBox.addItem("Subtract", new Runnable() {
            @Override
            public void run() {
                stringMath.subtractColumns();
            }
        });
        globalDataframeActionListBox.addItem("Multiply", new Runnable() {
            @Override
            public void run() {
                stringMath.multiplyColumns();
            }
        });
        globalDataframeActionListBox.addItem("Divide", new Runnable() {
            @Override
            public void run() {
                stringMath.divideColumns();
            }
        });
        globalDataframeActionListBox.addItem("Exponent", new Runnable() {
            @Override
            public void run() {
                stringMath.exponentColumns();
            }
        });
        globalDataframeActionListBox.addItem("ABS", new Runnable() {
            @Override
            public void run() {
                stringMath.absColumns();
            }
        });
        globalDataframeActionListBox.addItem("Round", new Runnable() {
            @Override
            public void run() {
                stringMath.roundColumns();
            }
        });

//            dataframeActionListBox.addItem("Regression", new Runnable() {
//                @Override
//                public void run() {
//                    // Code to run when action activated
//                }
//            });
//            dataframeActionListBox.addItem("Sort", new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
        globalDataframeActionListBox.addItem("Rename Column", new Runnable() {
            @Override
            public void run() {
                renameColumn();
            }
        });
        globalDataframeActionListBox.addItem("Merge Column", new Runnable() {
            @Override
            public void run() {
                mergeColumn();
            }
        });
//            dataframeActionListBox.addItem("Merge Table", new Runnable() {
//                @Override
//                public void run() {
//                   // may be best left to the custom sql query
//                }
//            });
        globalDataframeActionListBox.addItem("Split Column", new Runnable() {
            @Override
            public void run() {
                splitColumn();
            }
        });
        globalDataframeActionListBox.addItem("Contains", new Runnable() {
            @Override
            public void run() {
                colContains();
            }
        });

        globalDataframeActionListBox.addItem("Add Column", new Runnable() {
            @Override
            public void run() {
                addColumn();
            }
        });
        globalDataframeActionListBox.addItem("Remove Column", new Runnable() {
            @Override
            public void run() {
                removeColumn();
            }
        });
    }
}
