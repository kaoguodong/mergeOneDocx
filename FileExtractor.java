import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;

public class FileExtractor {

    public static void main(String[] args) {
        String fileName = "11_HBase从入门到精通";
        String folderPath = "/Users/xiaotingting/Downloads/学习资料/" + fileName;  // Replace with your folder path
        String outputFilePath = "/Users/xiaotingting/Downloads/学习资料/" + fileName +"unzip";  // Replace with your output file path

        try {
            extractAllCompressedFilesRecursively(new File(folderPath), outputFilePath);
            System.out.println("All files extracted successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 解压文件夹中的所有压缩文件
    public static void extractAllCompressedFiles(String folderPath, String outputFolderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid folder path: " + folderPath);
        }

        File[] files = folder.listFiles();
        if (files == null) {
            throw new IOException("No files found in folder: " + folderPath);
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                extractZipFile(file, outputFolderPath);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".rar")) {
                extractRarFile(file, outputFolderPath);
            }
        }
    }

    // 递归解压文件夹中的所有压缩文件
    public static void extractAllCompressedFilesRecursively(File folder, String outputFolderPath) throws IOException {
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid folder path: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();
        if (files == null) {
            throw new IOException("No files found in folder: " + folder.getAbsolutePath());
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是文件夹，递归调用
                extractAllCompressedFilesRecursively(file, outputFolderPath);
            } else if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith(".zip")) {
                    extractZipFile(file, outputFolderPath);
                } else if (file.getName().toLowerCase().endsWith(".rar")) {
                    extractRarFile(file, outputFolderPath);
                }
            }
        }
    }

    // 解压 ZIP 文件
    public static void extractZipFile(File zipFile, String outputFolderPath) throws IOException {
        // 设置适合的编码，例如 UTF-8 或 GBK
        try (ZipFile zf = new ZipFile(zipFile, "GBK")) {
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                File outputFile = new File(outputFolderPath, entry.getName());
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (InputStream is = zf.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("invalid picture file: " + zipFile.getAbsolutePath());
            System.out.println(e.getMessage());
        }
        System.out.println("Extracted ZIP: " + zipFile.getAbsolutePath());
    }

    // 解压 RAR 文件
    public static void extractRarFile(File rarFile, String outputFolderPath) throws IOException {
        try (Archive archive = new Archive(new FileInputStream(rarFile))) {
            if (archive == null) {
                throw new IOException("Failed to open RAR file: " + rarFile.getAbsolutePath());
            }

            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                File outputFile = new File(outputFolderPath, fileHeader.getFileNameString().trim());
                if (fileHeader.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        archive.extractFile(fileHeader, fos);
                    }
                }
            }
        }catch(Exception e){
            System.out.println("invalid picture file: " + rarFile.getAbsolutePath());
            System.out.println(e.getMessage());
        }
        System.out.println("Extracted RAR: " + rarFile.getAbsolutePath());
    }
}