package org.mixql.engine.sqlite;

import java.sql.Array;

public class JavaSqlArrayConverter {
    public static String[] toStringArray(Array value) throws java.sql.SQLException {
        return (String[]) value.getArray();
    }

    public static Boolean[] toBooleanArray(Array value) throws java.sql.SQLException{
        return (Boolean[]) value.getArray();
    }

    public static int[] toIntArray(Array value) throws java.sql.SQLException{
        return (int[]) value.getArray();
    }

    public static Double[] toDoubleArray(Array value) throws java.sql.SQLException{
        return (Double[]) value.getArray();
    }
}
