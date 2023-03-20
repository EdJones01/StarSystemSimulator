import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        GraphicsConfiguration windowsScaleFactor = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();

        double scaleX = windowsScaleFactor.getDefaultTransform().getScaleX();
        double scaleY = windowsScaleFactor.getDefaultTransform().getScaleY();

        Dimension desiredStageSize = new Dimension(1700, 900);
        Dimension adjustedStageSize = new Dimension((int) (desiredStageSize.width/scaleX), (int) (desiredStageSize.height/scaleY));
        setTitle("Star System Simulator");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Stage stage = new Stage();
        stage.setPreferredSize(adjustedStageSize);
        MenuBar menuBar = new MenuBar(stage);
        setJMenuBar(menuBar);
        setContentPane(stage);
        pack();
        setLocationRelativeTo(null);
        stage.setup(adjustedStageSize.width, adjustedStageSize.height);
        menuBar.updateBodiesMenu();
    }
}
