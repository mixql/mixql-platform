import scala.collection.immutable.Stream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader {
    public static void downloadFile(String uri, File target) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        try(InputStream in = connection.getInputStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(target))
        ){
            byte[] buffer = new byte[128];
            int count;
            while ((count = in.read(buffer, 0, 128)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
        }
    }
}
