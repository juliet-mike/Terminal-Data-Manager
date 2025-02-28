package dev.noodle.models;

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