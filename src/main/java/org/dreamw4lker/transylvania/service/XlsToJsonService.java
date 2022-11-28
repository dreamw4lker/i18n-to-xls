package org.dreamw4lker.transylvania.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class XlsToJsonService {
    private final String workPath;
    private final String xlsPath;
    private final String langTo;

    private final Map<String, Map<String, String>> filepathMap = new HashMap<>();

    public XlsToJsonService(String workPath, String xlsPath, String langTo) {
        this.workPath = workPath;
        this.xlsPath = xlsPath;
        this.langTo = langTo;
    }

    public void createJsonFiles() throws IOException {
        xls2map();
        map2json();
    }

    private void xls2map() throws IOException {
        File xlsFile = new File(xlsPath);
        try (FileInputStream fileInputStream = new FileInputStream(xlsFile)) {
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);
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
            JsonObject jsonObject = createJsonObject(entry.getValue());
            saveJsonObject(entry.getKey(), jsonObject);
        }
    }

    private JsonObject createJsonObject(Map<String, String> values) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String[] jsonPath = entry.getKey().split("\\.");
            JsonObject pointer = jsonObject;
            for (int i = 0; i < jsonPath.length; i++) {
                if (pointer.has(jsonPath[i])) {
                    pointer = pointer.getAsJsonObject(jsonPath[i]);
                } else {
                    if (i != jsonPath.length - 1) {
                        pointer.add(jsonPath[i], new JsonObject());
                        pointer = pointer.getAsJsonObject(jsonPath[i]);
                    } else {
                        pointer.addProperty(jsonPath[i], entry.getValue());
                    }
                }
            }
        }
        return jsonObject;
    }

    private void saveJsonObject(String path, JsonObject jsonObject) throws IOException {
        File file = new File(workPath + File.separator + langTo + File.separator + path);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtils.write(file, gson.toJson(jsonObject), StandardCharsets.UTF_8);
    }
}
