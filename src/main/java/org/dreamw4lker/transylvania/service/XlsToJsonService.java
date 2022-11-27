package org.dreamw4lker.transylvania.service;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XlsToJsonService {
    private final String workPath;
    private final String xlsPath;
    private final String langFrom;
    private final String langTo;

    private final Map<String, Map<String, String>> filepathMap = new HashMap<>();

    public XlsToJsonService(String workPath, String xlsPath, String langFrom, String langTo) {
        this.workPath = workPath;
        this.xlsPath = xlsPath;
        this.langFrom = langFrom;
        this.langTo = langTo;
    }

    public void createJsonFiles() throws IOException {
        xls2map();
        map2json();
    }

    private void xls2map() throws IOException {
        File xlsFile = new File(xlsPath);
        try (FileInputStream fileInputStream = new FileInputStream(xlsFile)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            //Первая строка с заголовками - она не нужна
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String key = row.getCell(0).getStringCellValue();
                String filepath = row.getCell(1).getStringCellValue();
                //Ячейку с индексом 2 пропускаем: там исходная строка, запрещённая к редактированию
                String translation = row.getCell(3).getStringCellValue();

                filepathMap.computeIfAbsent(filepath, k -> new TreeMap<>()).put(key, translation);
            }
        }
    }

    private void map2json() throws IOException {
        for (Map.Entry<String, Map<String, String>> entry : filepathMap.entrySet()) {
            JSONObject jsonObject = createJsonObject(entry.getValue());
            saveJsonObject(entry.getKey(), jsonObject);
        }
    }

    private JSONObject createJsonObject(Map<String, String> values) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String[] jsonPath = entry.getKey().split("\\.");
            JSONObject pointer = jsonObject;
            for (int i = 0; i < jsonPath.length; i++) {
                if (pointer.has(jsonPath[i])) {
                    pointer = pointer.getJSONObject(jsonPath[i]);
                } else {
                    if (i != jsonPath.length - 1) {
                        pointer.put(jsonPath[i], new JSONObject());
                        pointer = pointer.getJSONObject(jsonPath[i]);
                    } else {
                        pointer.put(jsonPath[i], entry.getValue());
                    }
                }
            }
        }
        return jsonObject;
    }

    private void saveJsonObject(String path, JSONObject jsonObject) throws IOException {
        File file = new File(workPath + File.separator + langTo + File.separator + path);
        FileUtils.write(file, jsonObject.toString(4), StandardCharsets.UTF_8);
    }
}
