package font;

import com.fasterxml.jackson.databind.ObjectMapper;
import util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class JsonHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    // cell size 163
    public void generateFontFile(
            HashMap<String, float[]> charInfo,
            String noExtensionPath)
    {

        try {
            objectMapper.writeValue(new File(noExtensionPath + ".json"), charInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public HashMap<Character, float[]> readCharInfo(String noExtensionPath) {
        try {
            HashMap<String, List<Double>> value = objectMapper.readValue(new File(noExtensionPath + ".json"), HashMap.class);
            HashMap<Character, float[]> result = new HashMap<>(value.size());

            value.forEach((s, l) -> {
                result.put(s.charAt(0), Utils.toFloatArray(l));
            });
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
