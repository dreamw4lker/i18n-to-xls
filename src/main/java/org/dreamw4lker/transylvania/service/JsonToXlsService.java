package org.dreamw4lker.transylvania.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
                            JsonElement jsonElement = JsonParser.parseReader(reader);
                            String relativePath = path.toString().replace(workPath + File.separator + langFrom, "");
                            json2KVMap(jsonElement.getAsJsonObject(), "", relativePath, lang);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void json2KVMap(JsonObject jsonObj, String prefix, String path, String lang) {
        jsonObj.keySet().forEach(keyStr -> {
            JsonElement value = jsonObj.get(keyStr);
            String currentKey = prefix.isEmpty() ? keyStr : prefix + "." + keyStr;
            if (value instanceof JsonObject) {
                json2KVMap((JsonObject) value, currentKey, path, lang);
            } else {
                if (Objects.equals(lang, langFrom)) {
                    //TODO: можно вставить проверку на наличие такого ключа (для warning'ов о дубликатах строк)
                    jsonKVMap.put(currentKey, new ArrayList<>(List.of(path, value.getAsString())));
                } else {
                    //TODO: there should be better way to update
                    List<String> values = jsonKVMap.getOrDefault(currentKey, new ArrayList<>());
                    if (values.isEmpty()) {
                        values = Arrays.asList(path, "", value.getAsString());
                    } else {
                        values.add(value.getAsString());
                    }
                    jsonKVMap.put(currentKey, values);
                }
            }
        });
    }

    private void map2xls() throws IOException {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "translation_template.xls";

        try (HSSFWorkbook workbook = new XlsMakeService(langFrom, langTo, jsonKVMap).createWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            workbook.write(outputStream);
        }
    }
}
