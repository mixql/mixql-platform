package org.mixql.platform.demo.utils;

import java.io.*;

public class FilesOperations {
    public static String readFileContent(File path) throws IOException{
        try (FileInputStream fin = new FileInputStream(path)) {
            byte[] buffer = new byte[fin.available()];
            // считываем буфер
            fin.read(buffer, 0, buffer.length);
            return new String(buffer);
        }
    }

    public static void writeContent(String content, File path) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(path)) {
            byte[] buffer = content.getBytes();
            // считываем буфер
            fos.write(buffer, 0, buffer.length);
        }
    }
}
