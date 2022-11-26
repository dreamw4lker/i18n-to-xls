package org.dreamw4lker.transylvania;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    private static Map<String, String> jsonKVMap = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        json2xls();
        System.out.println("OK done");
    }

    private static void json2xls() throws FileNotFoundException {
        String path = "//ru-RU.json";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject jsonObject = new JSONObject(tokener);
        json2KVMap(jsonObject, "");

        for (Map.Entry<String, String> entry : jsonKVMap.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
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
}
