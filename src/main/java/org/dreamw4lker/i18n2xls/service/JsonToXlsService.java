package org.dreamw4lker.i18n2xls.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Сервис переноса JSON-файлов переводов в XLS
 *
 * @author Alexander Shkirkov
 */
@Slf4j
public class JsonToXlsService {
    private final String workPath;
    private final String langFrom;
    private final String langTo;
    private final String resultFileName;
    private final String password;

    private final Map<String, String[]> jsonKVMap = new TreeMap<>();

    public JsonToXlsService(Map<String, String> args) {
        this.workPath = args.get("path");
        this.langFrom = args.get("from");
        this.langTo = args.get("to");
        this.resultFileName = args.get("resultFileName");
        this.password = args.get("password");
    }

    /**
     * Основная функция преобразования JSON-файлов в XLS
     */
    public void createXls() {
        fillKVMap(langFrom);
        fillKVMap(langTo);
        mapToXls();
    }

    /**
     * Преобразование JSON-файлов в key-value map
     * @param lang язык перевода (используется в качестве префикса папки переводов)
     */
    private void fillKVMap(String lang) {
        String absolutePath = workPath + File.separator + lang;
        Path dir = Paths.get(absolutePath);
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                    .filter(filepath -> filepath.getFileName().toString().endsWith(".json"))
                    .forEach(filepath -> {
                        try {
                            BufferedReader reader = Files.newBufferedReader(filepath);
                            JsonElement jsonElement = JsonParser.parseReader(reader);
                            String relativePath = filepath.toString().replace(absolutePath, "");
                            jsonToKVMap(jsonElement.getAsJsonObject(), "", relativePath, lang);
                        } catch (IOException e) {
                            log.error("JSON file processing error. Filepath: {}", filepath, e);
                        }
                    });
        } catch (IOException e) {
            log.error("I/O exception at Files.walk command execution. Directory: {}", dir, e);
        }
    }

    /**
     * Перенос данных одного JSON-файла в key-value map.
     * (!) Это рекурсивная функция. Есть потенциальная возможность получить StackOverflowError на больших вложенностях
     *
     * @param jsonObj JSON-объект, прочитанный из JSON-файла
     * @param prefix текущий префикс. Для перевода "aa.bb.cc.dd" на каждом уровне рекурсии он будет равен "aa", "aa.bb", "aa.bb.cc"
     * @param path относительный путь до файла перевода
     * @param lang текущий язык перевода
     */
    private void jsonToKVMap(JsonObject jsonObj, String prefix, String path, String lang) {
        jsonObj.keySet().forEach(keyStr -> {
            JsonElement value = jsonObj.get(keyStr);
            String currentKey = prefix.isEmpty() ? keyStr : prefix + "." + keyStr;
            if (value instanceof JsonObject) {
                jsonToKVMap((JsonObject) value, currentKey, path, lang);
            } else {
                String[] mapValue = new String[] {"", "", ""};
                if (Objects.equals(lang, langFrom)) {
                    //В этом случае мы работаем со строками на исходном языке
                    if (jsonKVMap.containsKey(currentKey)) {
                        log.warn("Found duplicate key: '{}' at path {}", currentKey, path);
                    }
                    mapValue[0] = path;
                    mapValue[1] = value.getAsString();
                } else {
                    //В этом случае мы работаем со строками на языке-результате
                    mapValue = jsonKVMap.get(currentKey);
                    if (mapValue == null) {
                        mapValue = new String[] {path, "", value.getAsString()};
                    } else {
                        //В Map могут присутствовать записи с переводом на исходном языке. Просто дополняем их
                        mapValue[2] = value.getAsString();
                    }
                }
                jsonKVMap.put(currentKey, mapValue);
            }
        });
    }

    /**
     * Преобразование key-value map в XLS-файл
     */
    private void mapToXls() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + resultFileName;

        try (HSSFWorkbook workbook = new XlsMakeService(langFrom, langTo, password, jsonKVMap).createWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileLocation)) {
            workbook.write(outputStream);
            log.info("XLS file successfully created at path: {}", fileLocation);
        } catch (IOException e) {
            log.error("I/O exception at XLS to OutputSteam write", e);
        }
    }
}
