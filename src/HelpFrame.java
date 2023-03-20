import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

public class HelpFrame extends JFrame {
    public HelpFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(400, 500));

        String[] data = new String[1];
        data[0] = "Error loading help menu text.";
        try {
            data = Tools.readFromFile(FileManager.getResourcesFolderLocation() + "help.txt");
        } catch (FileNotFoundException e) {}

        String helpText = "";
        for (String s : data)
            helpText += s;

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Help");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(Tools.getCustomFont(30));
        panel.add(title, BorderLayout.NORTH);

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(helpText);
        panel.add(textPane, BorderLayout.CENTER);

        add(panel);
        pack();
    }
}
