import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class FileManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .create();

    private static String saveFolderLocation = "src/saves/";
    private static String presetFolderLocation = "src/presets/";
    private static String resourcesFolderLocation = "src/resources/";

    public static void makeSaveDirectory() {
        File theDir = new File(saveFolderLocation);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    public static void makePresetDirectory() {
        File theDir = new File(presetFolderLocation);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    public static boolean saveToJSON(LinkedList<Body> bodies, String filename) {
        String json = gson.toJson(bodies);
        try {
            if (new File(saveFolderLocation + filename).exists()) {
                if (!Tools.showYesNoDialog("Overwrite " + filename + "?")) {
                    return false;
                }
            }
            Tools.saveToFile(json, saveFolderLocation + filename);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean saveImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, "png", new File(saveFolderLocation + filename + "." + "png"));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean savesPresent() {
        return new File(saveFolderLocation).listFiles().length > 0;
    }

    public static BufferedImage loadImage(String filename) {
        BufferedImage img;
        try {
            return ImageIO.read(new File(saveFolderLocation + filename + "." + "png"));
        } catch (IOException e) {
            return null;
        }
    }

    public static LinkedList<Body> loadFromJSON(String filename) {
        try {
            String[] data = Tools.readFromFile(saveFolderLocation + filename);
            String rawData = "";
            for (String s : data) {
                rawData += s;
            }
            Body[] fromJson = gson.fromJson(rawData, Body[].class);

            LinkedList<Body> bodies = new LinkedList<>();
            for (Body body : fromJson)
                bodies.add(body);

            return bodies;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSaveFolderLocation() {
        return saveFolderLocation;
    }

    public static void setSaveFolderLocation(String saveFolderLocation) {
        FileManager.saveFolderLocation = saveFolderLocation;
    }

    public static String getPresetFolderLocation() {
        return presetFolderLocation;
    }

    public static void setPresetFolderLocation(String presetFolderLocation) {
        FileManager.presetFolderLocation = presetFolderLocation;
    }

    public static String getResourcesFolderLocation() {
        return resourcesFolderLocation;
    }

    public static void setResourcesFolderLocation(String resourcesFolderLocation) {
        FileManager.resourcesFolderLocation = resourcesFolderLocation;
    }
}

class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, Color value) throws IOException {
        out.beginObject();
        out.name("red");
        out.value(value.getRed());
        out.name("green");
        out.value(value.getGreen());
        out.name("blue");
        out.value(value.getBlue());
        out.endObject();
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        in.beginObject();
        int red = 0, green = 0, blue = 0;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "red":
                    red = in.nextInt();
                    break;
                case "green":
                    green = in.nextInt();
                    break;
                case "blue":
                    blue = in.nextInt();
                    break;
                default:
                    in.skipValue();
            }
        }
        in.endObject();
        return new Color(red, green, blue);
    }
}

