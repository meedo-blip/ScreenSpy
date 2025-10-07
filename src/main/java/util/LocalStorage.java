package util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LocalStorage {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static HashMap<String, String> map = new HashMap<>();
    private static final File jsonFile = new File("assets/ls.json");

    public static String get(String key) {
        readFile();

        return map.get(key);
    }

    public static void set(String key, String value) {

        map.put(key, value);

        try {
            objectMapper.writeValue(new File("assets/ls.json"), map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readFile() {
        try {
           map = objectMapper.readValue(new File("assets/ls.json"), HashMap.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
