import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import javax.swing.*;

public class LoadFrame extends JFrame {
    private LoadDrawPanel drawPanel;
    private LoadButtonPanel buttonPanel;

    private JScrollPane scrollPane;

    private ActionListener actionListener;

    private String folderPath;

    private Dimension frameSize = new Dimension(1000, 600);

    private File[] files;

    public LoadFrame(ActionListener parent, String folderPath) {
        this.actionListener = parent;
        this.folderPath = folderPath;
    }

    public void setup() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(frameSize);

        int buttonPanelWidth = 220;

        drawPanel = new LoadDrawPanel(actionListener, frameSize, buttonPanelWidth);
        add(drawPanel, "Center");

        File saveFolder = new File(folderPath);
        files = parseFiles(saveFolder.listFiles());


        buttonPanel = new LoadButtonPanel(files, drawPanel);

        scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setPreferredSize(new Dimension(buttonPanelWidth, getHeight()));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, "West");
    }

    private File[] parseFiles(File[] files) {
        List<File> result = new LinkedList<>();
        for (File file : files) {
            if (!file.getName().endsWith(".png")) {
                result.add(file);
            }
        }

        File[] arr = result.toArray(new File[result.size()]);
        Arrays.sort(arr, Comparator.comparingLong(File::lastModified).reversed());
        return arr;
    }

    public String getSelectedFilename() {
        return files[buttonPanel.getSelectedIndex()].getName();
    }
}

class LoadButtonPanel extends JPanel implements ActionListener {
    private LoadDrawPanel drawPanel;

    private int selectedIndex = 0;
    private File[] files;

    public LoadButtonPanel(File[] files, LoadDrawPanel drawPanel) {
        this.files = files;
        if (files.length > 8) {
            setLayout(new GridLayout(files.length, 1));
        } else {
            setLayout(new GridLayout(8, 1));
        }
        setBackground(new Color(47, 47, 47));


        this.drawPanel = drawPanel;

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            JButton button = new JButton();
            String name = file.getName();
            if(name.length() > 16) {
                name = name.substring(0, 16);
                name += "...";
            }

            button.setText("<html>" + name + "<br />" + sdf.format(file.lastModified()) + "</html>");
            button.setPreferredSize(new Dimension(button.getWidth(), 50));
            button.addActionListener(this);
            button.setActionCommand("" + i);
            button.setFont(Tools.getCustomFont(20));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(button.getFont());
            add(button);

            if (i == 0)
                button.doClick();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setSelectedIndex(Integer.parseInt(e.getActionCommand()));
        drawPanel.setThumbnail(FileManager.loadImage(files[selectedIndex].getName()));
        FileManager.loadImage(files[selectedIndex].getName());
    }
}

class LoadDrawPanel extends JPanel {
    BufferedImage thumbnail;

    public LoadDrawPanel(ActionListener actionListener, Dimension frameSize, int x) {
        setBackground(new Color(10, 10, 10));
        setLayout(null);

        JButton loadButton = new JButton("Load");
        loadButton.setBounds(new Rectangle(frameSize.width - x - 120, frameSize.height - 90, 90, 40));
        loadButton.setFont(Tools.getCustomFont(20));
        loadButton.addActionListener(actionListener);
        loadButton.setActionCommand("load");

        add(loadButton);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (thumbnail != null) {
            g2.drawImage(Tools.scaleBufferedImage(thumbnail, 0.6, 0.6), 0, 0, this);
        }
    }

    public void setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
        repaint();
    }
}
