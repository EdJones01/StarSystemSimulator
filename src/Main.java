import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        FileManager.makeSaveDirectory();
        FileManager.makePresetDirectory();

        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}
