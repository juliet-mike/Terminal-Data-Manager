
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

package dev.noodle.modules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SQLsanitize {
    private static final Set<String> SQL_RESERVED_KEYWORDS = new HashSet<>();

    static {
        // Add common SQL reserved keywords to the set
        String[] keywords = {
                "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "BACKUP", "BETWEEN", "CASE",
                "CHECK", "COLUMN", "CONSTRAINT", "CREATE", "DATABASE", "DEFAULT", "DELETE", "DESC",
                "DISTINCT", "DROP", "EXEC", "EXISTS", "FOREIGN", "FROM", "FULL", "GROUP", "HAVING",
                "IN", "INDEX", "INNER", "INSERT", "INTERSECT", "INTO", "IS", "JOIN", "LEFT", "LIKE",
                "LIMIT", "NOT", "NULL", "ON", "OR", "ORDER", "OUTER", "PRIMARY", "PROCEDURE", "RIGHT",
                "ROWNUM", "SELECT", "SET", "TABLE", "TOP", "TRUNCATE", "UNION", "UNIQUE", "UPDATE",
                "VALUES", "VIEW", "WHERE", "ID"
                // Add more keywords as needed
        };
        SQL_RESERVED_KEYWORDS.addAll(Arrays.asList(keywords));
    }

    public static boolean isSQLKeyword(String word) {
        return SQL_RESERVED_KEYWORDS.contains(word.toUpperCase());
    }
}