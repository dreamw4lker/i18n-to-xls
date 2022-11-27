package org.dreamw4lker.transylvania.service;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
        json2xls(langFrom);
        json2xls(langTo);
        map2xls();
    }

    private void json2xls(String lang) throws IOException {
        Path dir = Paths.get(workPath + File.separator + lang);
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            BufferedReader reader = Files.newBufferedReader(path);
                            JSONTokener tokener = new JSONTokener(reader);
                            JSONObject jsonObject = new JSONObject(tokener);
                            String relativePath = path.toString().replace(workPath + File.separator + langFrom, "");
                            json2KVMap(jsonObject, "", relativePath, lang);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void json2KVMap(JSONObject jsonObj, String prefix, String path, String lang) {
        jsonObj.keySet().forEach(keyStr -> {
            Object value = jsonObj.get(keyStr);
            String currentKey = prefix.isEmpty() ? keyStr : prefix + "." + keyStr;
            if (value instanceof JSONObject) {
                json2KVMap((JSONObject) value, currentKey, path, lang);
            } else {
                if (Objects.equals(lang, langFrom)) {
                    //TODO: можно вставить проверку на наличие такого ключа (для warning'ов о дубликатах строк)
                    jsonKVMap.put(currentKey, new ArrayList<>(List.of(path, String.valueOf(value))));
                } else {
                    //TODO: there should be better way to update
                    List<String> values = jsonKVMap.getOrDefault(currentKey, new ArrayList<>());
                    if (values.isEmpty()) {
                        values = Arrays.asList(path, "", String.valueOf(value));
                    } else {
                        values.add(String.valueOf(value));
                    }
                    jsonKVMap.put(currentKey, values);
                }
            }
        });
    }

    private void map2xls() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Перевод " + langFrom + " -> " + langTo);
        sheet.protectSheet("1234"); //TODO: use strict pass. Use properties?

        Row header = sheet.createRow(0);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("ID строки");

        headerCell = header.createCell(1);
        headerCell.setCellValue("Путь до файла");

        headerCell = header.createCell(2);
        headerCell.setCellValue(langFrom + " перевод");

        headerCell = header.createCell(3);
        headerCell.setCellValue(langTo + " перевод");

        CellStyle unlockedCellStyle = workbook.createCellStyle();
        unlockedCellStyle.setLocked(false);

        int i = 1;
        for (Map.Entry<String, List<String>> entry : jsonKVMap.entrySet()) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().get(0));
            row.createCell(2).setCellValue(entry.getValue().get(1));

            Cell toLangCell = row.createCell(3);
            toLangCell.setCellValue(entry.getValue().size() > 2 ? entry.getValue().get(2) : "");
            toLangCell.setCellStyle(unlockedCellStyle);
            i++;
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.setColumnWidth(3, 100 * 256); //The unit is 1/256 of character -> the value is 100 chars

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xls";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }
}
