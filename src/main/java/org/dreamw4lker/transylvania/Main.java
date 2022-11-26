package org.dreamw4lker.transylvania;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    private static Map<String, String> jsonKVMap = new TreeMap<>();

    public static void main(String[] args) throws IOException {
        json2xls();
        map2xls();
    }

    private static void json2xls() throws FileNotFoundException {
        String path = "/home/as/projects-idea/lis_ariadna_psql/ui-main/src/lang/ru-RU.json";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject jsonObject = new JSONObject(tokener);
        json2KVMap(jsonObject, "");
    }

    public static void json2KVMap(JSONObject jsonObj, String prefix) {
        jsonObj.keySet().forEach(keyStr -> {
            Object keyvalue = jsonObj.get(keyStr);
            String currentKey = prefix.isEmpty() ? keyStr : prefix + "." + keyStr;
            if (keyvalue instanceof JSONObject) {
                json2KVMap((JSONObject) keyvalue, currentKey);
            } else {
                jsonKVMap.put(currentKey, String.valueOf(keyvalue));
            }
        });
    }

    public static void map2xls() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Translation");

        Row header = sheet.createRow(0);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Key");

        headerCell = header.createCell(1);
        headerCell.setCellValue("RU translation");
        int i = 1;
        for (Map.Entry<String, String> entry : jsonKVMap.entrySet()) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
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
