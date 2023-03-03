import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TarGzArchiver {
    public static void  extractTarGz(File tarGzFile, File target)
            throws java.io.IOException {
        try(FileInputStream fin = new FileInputStream(tarGzFile)){
            _extractTarGz(fin, target.getAbsolutePath());
        }
    }

    static void _extractTarGz(InputStream in, String distTargetDir)
            throws java.io.IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                /** if entry is derictory, create directory **/
                if (entry.isDirectory()) {
                    File f = new File(distTargetDir + "/" + entry.getName());
                    try {
                        Files.createDirectories(Paths.get(f.getAbsolutePath()));
                    } catch (Exception ex) {
                        System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
                                f.getAbsolutePath() + "Error: " + ex.getMessage());
                        throw ex;
                    }
                } else {
                    int count;
                    int BUFFER_SIZE = 1024;
                    byte data[] = new byte[BUFFER_SIZE];
                    FileOutputStream fos = new FileOutputStream(distTargetDir + "/" + entry.getName(), false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                }
            }
            System.out.println("Untar completed successfully");
        }
    }

    static public void createTarGz(File tarGzPath, String base,  File... dirPath)
            throws IOException {
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        GzipCompressorOutputStream gzOut = null;
        TarArchiveOutputStream tOut = null;
        try {
            System.out.println(new File(".").getAbsoluteFile());
            fOut = new FileOutputStream(tarGzPath);
            bOut = new BufferedOutputStream(fOut);
            gzOut = new GzipCompressorOutputStream(bOut);
            tOut = new TarArchiveOutputStream(gzOut);
            tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            for (int i = 0; i < dirPath.length; i++) {
                addFileToTarGz(tOut, dirPath[i], base);
            }
        } finally {
            tOut.finish();
            tOut.close();
            gzOut.close();
            bOut.close();
            fOut.close();
        }
    }

    static private void addFileToTarGz(TarArchiveOutputStream tOut, File path, String base)
            throws IOException {
        System.out.println("Does path exist: " + path.exists());
        String entryName = base + path.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(path, entryName);
        tOut.putArchiveEntry(tarEntry);

        if (path.isFile()) {
            IOUtils.copy(new FileInputStream(path), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = path.listFiles();
            if (children != null) {
                for (File child : children) {
                    System.out.println(child.getName());
                    addFileToTarGz(tOut, child, entryName + "/");
                }
            }
        }
    }
}
