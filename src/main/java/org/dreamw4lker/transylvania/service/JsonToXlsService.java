package org.dreamw4lker.transylvania.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class JsonToXlsService {
    private final String workPath;
    private final String langFrom;
    private final String langTo;

    private final Map<String, List<String>> jsonKVMap = new TreeMap<>();

    public JsonToXlsService(String workPath, String langFrom, String langTo) {
        this.workPath = workPath;
        this.langFrom = langFrom;
        this.langTo = langTo;
    }

    public void createXls() throws IOException {
        json2xls();
        map2xls();
    }

    private void json2xls() throws IOException {
        Path dir = Paths.get(workPath + File.separator + langFrom);
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            BufferedReader reader = Files.newBufferedReader(path);
                            JSONTokener tokener = new JSONTokener(reader);
                            JSONObject jsonObject = new JSONObject(tokener);
                            String relativePath = path.toString().replace(workPath + File.separator + langFrom, "");
                            json2KVMap(jsonObject, "", relativePath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void json2KVMap(JSONObject jsonObj, String prefix, String path) {
        jsonObj.keySet().forEach(keyStr -> {
            Object value = jsonObj.get(keyStr);
            String currentKey = prefix.isEmpty() ? keyStr : prefix + "." + keyStr;
            if (value instanceof JSONObject) {
                json2KVMap((JSONObject) value, currentKey, path);
            } else {
                //TODO: можно вставить проверку на наличие такого ключа (для warning'ов о дубликатах строк)
                jsonKVMap.put(currentKey, List.of(path, String.valueOf(value)));
            }
        });
    }

    private void map2xls() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Перевод " + langFrom + " --> " + langTo);

        Row header = sheet.createRow(0);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("ID строки");

        headerCell = header.createCell(1);
        headerCell.setCellValue("Путь до файла");

        headerCell = header.createCell(2);
        headerCell.setCellValue(langFrom + " перевод");

        headerCell = header.createCell(3);
        headerCell.setCellValue(langTo + " перевод");
        int i = 1;
        for (Map.Entry<String, List<String>> entry : jsonKVMap.entrySet()) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().get(0));
            row.createCell(2).setCellValue(entry.getValue().get(1));
            i++;
        }

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xls";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }
}
