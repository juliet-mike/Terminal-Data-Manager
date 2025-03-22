
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

package dev.noodle.ui;

import static dev.noodle.modules.quickOps.*;
import static dev.noodle.modules.quickOps.removeColumn;
import static dev.noodle.TDM.globalDataframeActionListBox;

public class dataframeActionList {

    // moved to take all these repetitive lines out of TDM.java
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
