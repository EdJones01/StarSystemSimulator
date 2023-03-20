import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Tools {
    /**
     * Provides a standard path to save file to "src/file".
     */
    public static final String DEFAULT_FILEPATH = "src/file";

    /**
     * Used for generation of random numbers.
     */
    private static final Random random = new Random();

    /**
     * Adds a function to a key press.
     *
     * @param comp           The parent component.
     * @param keyCode        Keycode (Use KeyEvent.VK_[key]).
     * @param id             Unique ID for the keybinding.
     * @param actionListener The action lister containing the function to be run on keypress.
     */
    public static void addKeyBinding(JComponent comp, int keyCode, String id, ActionListener actionListener) {
        InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = comp.getActionMap();
        im.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
        am.put(id, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                actionListener.actionPerformed(e);
            }
        });
    }

    /**
     * Shows a simple yes no dialog.
     *
     * @param prompt The prompt to display in the dialog.
     * @return Returns true if the user presses yes, else returns false.
     */
    public static boolean showYesNoDialog(String prompt) {
        return JOptionPane.showConfirmDialog(null, prompt, "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Scales a BufferedImage.
     *
     * @param before The BufferedImage you want to scale.
     * @param scaleX Scale on the X-axis.
     * @param scaleY Scale on the Y-axis.
     * @return The scaled BufferedImage.
     */
    public static BufferedImage scaleBufferedImage(BufferedImage before, double scaleX, double scaleY) {
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(before, after);
    }

    /**
     * Shows simple input dialog.
     *
     * @param prompt The prompt to display in the dialog.
     * @return The String entered by the user.
     */
    public static String showInputDialog(String prompt) {
        return JOptionPane.showInputDialog(null, prompt);
    }

    /**
     * Retrieves and derives a font from "src/resources/font.ttf".
     *
     * @param style The desired font style.
     * @param size  The desired font size.
     * @return The font.
     */
    public static Font getCustomFont(int style, int size) {
        Font font = null;
        InputStream is = Tools.class.getResourceAsStream("resources/font.ttf");
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
        }
        return font.deriveFont(style, size);
    }

    /**
     * Retrieves and derives a font from "src/resources/font.ttf".
     *
     * @param size The desired font size.
     * @return The font in plain style.
     */
    public static Font getCustomFont(int size) {
        return getCustomFont(Font.PLAIN, size);
    }

    /**
     * Adds a function to a set of key presses.
     *
     * @param comp           The parent component.
     * @param keyCodes       Array of Keycodes (Use KeyEvent.VK_[key]).
     * @param id             Unique ID for the keybinding.
     * @param actionListener The action lister containing the function to be run on keypress.
     */
    public static void addKeyBinding(JComponent comp, int[] keyCodes, String id, ActionListener actionListener) {
        for (int keyCode : keyCodes)
            addKeyBinding(comp, keyCode, id, actionListener);
    }

    /**
     * Shows basic popup.
     *
     * @param string The String to display in the popup.
     */
    public static void showPopup(String string) {
        JOptionPane.showMessageDialog(null, string);
    }

    /**
     * Returns a random Integer between 2 values.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random value between the two provided Integers.
     */
    public static int randomIntBetween(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Returns a random Double between 2 values.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random value between the two provided Doubles.
     */
    public static double randomDoubleBetween(double min, double max) {
        return min + (max - min) * new Random().nextDouble();
    }

    /**
     * Returns a color with a random hue.
     *
     * @return A random color.
     */
    public static Color randomColor() {
        return Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f);
    }

    /**
     * Makes a variable that goes between a min and max to a number from another min and max.
     *
     * @param num    The variable to map
     * @param numMin The variables minimum value.
     * @param numMax The variables maximum value.
     * @param min    Minimum for new variable.
     * @param max    Maximum for new variable.
     * @return A number between the new min and max based on the percentage of the provided variables min and max.
     */
    public static double map(double num, double numMin, double numMax, double min, double max) {
        return min + (max - min) * ((num - numMin) / (numMax - numMin));
    }

    /**
     * Converts a Double to scientific notation (e.g. 1.23 * 10^3)
     *
     * @param value         The Double to convert.
     * @param decimalPlaces The number of decimal places for the mantissa.
     * @return A String representing the scientific notation of the provided double.
     */
    public static String doubleToScientificNotation(double value, int decimalPlaces) {
        StringBuilder result = new StringBuilder();
        String pattern = "0.";
        for (int i = 0; i < decimalPlaces; i++) {
            pattern += "0";
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        int exponent = (int) Math.floor(Math.log10(Math.abs(value)));
        double mantissa = value / Math.pow(10, exponent);

        result.append(decimalFormat.format(mantissa)).append(" * 10^").append(exponent);

        return result.toString();
    }

    /**
     * Rounds a double to a certain number of decimal places.
     *
     * @param value         The Double to round.
     * @param decimalPlaces The number of decimal places ot round to.
     * @return The rounded double.
     */
    public static String round(double value, int decimalPlaces) {
        String pattern = "0.";
        for (int i = 0; i < decimalPlaces; i++) {
            pattern += "0";
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value);
    }

    /**
     * Reads a line from the console.
     *
     * @return The line written by the user to the console.
     */
    public static String readLine() {
        String input = "";
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            input = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO Error reading from command line.");
        }
        return input;

    }

    /**
     * Draws a String in the center of a Rectangle.
     *
     * @param g2     Graphics2D used for drawing.
     * @param bounds The Rectangle to draw in the center r=of.
     * @param string The String to draw.
     */
    public static void centerString(Graphics2D g2, Rectangle bounds, String string) {
        centerString(g2, bounds, 0, 0, string);
    }


    /**
     * Draws a String in the center of a Rectangle with an X and Y offset.
     *
     * @param g2      Graphics2D used for drawing.
     * @param xOffset Desired X offset for text.
     * @param yOffset Desired Y offset for text.
     * @param bounds  The Rectangle to draw in the center r=of.
     * @param string  The String to draw.
     */
    public static void centerString(Graphics2D g2, Rectangle bounds, int xOffset, int yOffset, String string) {
        Rectangle newBounds = new Rectangle(bounds.x + xOffset, bounds.y + yOffset, bounds.width, bounds.height);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Rectangle2D r2D = g2.getFont().getStringBounds(string, frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (newBounds.width / 2) - (rWidth / 2) - rX;
        int b = (newBounds.height / 2) - (rHeight / 2) - rY;
        g2.drawString(string, newBounds.x + a, newBounds.y + b);
    }

    /**
     * Places a sound located in 'src/resources' using a given filename.
     *
     * @param filename The filename of the sound to play.
     * @param muted    If the sound is muted.
     */
    public static void playSound(String filename, boolean muted) {
        if (!muted)
            try {
                URL url = Tools.class.getResource("/resources/" + filename + ".wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-20.0f);
                clip.start();
            } catch (Exception e) {
            }
    }

    /**
     * Reads text from a file.
     *
     * @param filepath The path to the desired file.
     * @return A String array of the lines within the file.
     * @throws FileNotFoundException Throws if there is no file at the given filepath.
     */
    public static String[] readFromFile(String filepath) throws FileNotFoundException {
        File file = new File(filepath);
        Scanner myReader = new Scanner(file);
        LinkedList<String> data = new LinkedList<>();
        while (myReader.hasNextLine()) {
            data.add(myReader.nextLine());
        }
        myReader.close();
        return data.toArray(new String[data.size()]);
    }

    /**
     * Saves text to a file.
     *
     * @param data     The data to save.
     * @param filepath The path to the desired file.
     * @throws IOException Throws if it cannot write to the given filepath.
     */
    public static void saveToFile(String data, String filepath) throws IOException {
        Files.write(Paths.get(filepath), data.getBytes());
    }

    /**
     * Converts a String to an Integer array with a given delimiter.
     *
     * @param s         The string to convert.
     * @param delimiter A delimiter from the splitting.
     * @return An int array from the given string.
     */
    public static int[] intArrayFromString(String s, String delimiter) {
        String[] strings = s.split(delimiter);
        int[] arr = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            arr[i] = Integer.parseInt(strings[i]);
        }
        return arr;
    }

    /**
     * Constrains an Integer between a minimum and maximum value.
     *
     * @param value The value to constrain.
     * @param min   The minimum of the value.
     * @param max   The maximum of the value.
     * @return The constrained value.
     */
    public static int constrainInt(int value, int min, int max) {
        return (int) constrainDouble(value, min, max);
    }

    /**
     * Constrains a Double between a minimum and maximum value.
     *
     * @param value The value to constrain.
     * @param min   The minimum of the value.
     * @param max   The maximum of the value.
     * @return The constrained value.
     */
    public static double constrainDouble(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /**
     * Checks if a MouseEvent is within a Rectangle.
     *
     * @param event  The MouseEvent to check.
     * @param bounds The Rectangle to check.
     * @return Returns true if the MouseEvent is within the bounds fo the Rectangle.
     */
    public static boolean mouseEventWithin(MouseEvent event, Rectangle bounds) {
        return (event.getX() >= bounds.getMinX() && event.getX() <= bounds.getMaxX())
                && (event.getY() >= bounds.getMinY() && event.getY() <= bounds.getMaxY());
    }
}