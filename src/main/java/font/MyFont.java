package font;


import util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

public class MyFont {
    public final String noExtensionPath;

    public final int texId;
    public Map<Character, float[]> charTexCoords;
    public final JsonHandler jsonHandler;

    private static final String absFontsPath = new File("assets\\fonts").getAbsolutePath();

    public MyFont(String ttfPath, int fontsize) {
        String absPath = new File(ttfPath).getAbsolutePath();

        String ttfName = ttfPath.substring(ttfPath.lastIndexOf('\\') + 1);
        ttfName = ttfName.substring(0, ttfName.lastIndexOf('.'));

        System.out.println("ttfName: " + ttfName);

        jsonHandler = new JsonHandler();
        noExtensionPath = "assets\\fonts\\" + ttfName;

        Path path = Path.of(noExtensionPath + ".ttf");
        if (!absPath.substring(0, absPath.lastIndexOf('\\')).equals(absFontsPath)
                && !Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.copy(Path.of(absPath), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (new File(noExtensionPath + ".json").exists()) {
            charTexCoords = jsonHandler.readCharInfo(noExtensionPath);
            texId = Utils.createTexture(noExtensionPath + ".png", false);
        } else {
            assert false : "Font file '" + noExtensionPath + ".json' not found";
            texId = 0;
        }
    }
    public float[] getCharTexCoords(int codepoint) {
        return charTexCoords.get((char)codepoint);
    }
}
