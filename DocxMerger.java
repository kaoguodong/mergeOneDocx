import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DocxMerger {

    public static void main(String[] args) {
        String fileName = "12_Netty核心功能精讲以及核心源码剖析";
        String folderPath = "/Users/xiaotingting/Downloads/学习资料/" + fileName;  // Replace with your folder path
        String outputFilePath = "/Users/xiaotingting/Downloads/学习资料/" + fileName + ".docx";  // Replace with your output file path
        try {
            List<File> files = getAllFiles(new File(folderPath));
            mergeFiles(files, outputFilePath);
            System.out.println("Files merged successfully into " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 递归获取所有子文件夹中的.docx, .doc, .png 和 .txt 文件
    private static List<File> getAllFiles(File folder) {
        List<File> files = new ArrayList<>();
        File[] fileList = folder.listFiles();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    files.addAll(getAllFiles(file));
                } else if (file.isFile() &&
                        (file.getName().toLowerCase().endsWith(".docx") ||
                                file.getName().toLowerCase().endsWith(".doc") ||
                                file.getName().toLowerCase().endsWith(".png") ||
                                file.getName().toLowerCase().endsWith(".txt"))) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    // 合并所有文件
    public static void mergeFiles(List<File> files, String outputFilePath) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IOException("No files found in the specified directories.");
        }

        // 按文件名排序
        files.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

        // 创建合并后的文档
        try (XWPFDocument mergedDocument = new XWPFDocument()) {
            for (File file : files) {
                // 在文件内容开头插入绝对路径
                addFilePath(mergedDocument, file.getAbsolutePath());

                if (file.getName().toLowerCase().endsWith(".docx")) {
                    mergeDocx(file, mergedDocument);
                } else if (file.getName().toLowerCase().endsWith(".doc")) {
                    mergeDoc(file, mergedDocument);
                } else if (file.getName().toLowerCase().endsWith(".png")) {
                    mergeImage(file, mergedDocument);
                } else if (file.getName().toLowerCase().endsWith(".txt")) {
                    mergeTxt(file, mergedDocument);
                }
            }

            // 保存合并后的文档
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                mergedDocument.write(fos);
            }
        }
    }
    // 在文档中添加文件路径并设置为标题样式
    private static void addFilePath(XWPFDocument document, String filePath) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle("Heading1"); // 使用标题样式
        XWPFRun run = paragraph.createRun();
        run.setText("File Path: " + filePath);
        run.setBold(true); // 文件路径加粗
    }


    // 合并 .docx 文件
    private static void mergeDocx(File file, XWPFDocument mergedDocument) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    mergedDocument.createParagraph().getCTP().set(((XWPFParagraph) element).getCTP());
                } else if (element instanceof XWPFTable) {
                    mergedDocument.createTable().getCTTbl().set(((XWPFTable) element).getCTTbl());
                }
            }
            mergedDocument.createParagraph().setPageBreak(true); // 添加分页符
        }catch (Exception e){
            System.out.println("invalid docx file: " + file.getAbsolutePath());
            System.out.println(e.getMessage());
        }
    }

    // 合并 .doc 文件
    private static void mergeDoc(File file, XWPFDocument mergedDocument) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            HWPFDocument doc = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(doc);
            for (String paragraph : extractor.getParagraphText()) {
                XWPFParagraph newParagraph = mergedDocument.createParagraph();
                newParagraph.createRun().setText(paragraph.trim());
            }
            mergedDocument.createParagraph().setPageBreak(true); // 添加分页符
        }catch (Exception e){
            System.out.println("invalid picture file: " + file.getAbsolutePath());
            System.out.println(e.getCause());
        }
    }

    // 合并 .png 图片
    private static void mergeImage(File file, XWPFDocument mergedDocument) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            XWPFParagraph paragraph = mergedDocument.createParagraph();
            XWPFRun run = paragraph.createRun();

            // 插入图片，指定类型和大小
            run.addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, file.getName(),
                    Units.toEMU(500), Units.toEMU(500)); // 500x500像素
        } catch (Exception e) {
            System.err.println("Failed to insert image: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    // 合并 .txt 文件
    private static void mergeTxt(File file, XWPFDocument mergedDocument) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                XWPFParagraph paragraph = mergedDocument.createParagraph();
                paragraph.createRun().setText(line);
            }
            mergedDocument.createParagraph().setPageBreak(true); // 添加分页符
        }catch (Exception e){
            System.out.println("invalid picture file: " + file.getAbsolutePath());
            System.out.println(e.getCause());
        }
    }
}