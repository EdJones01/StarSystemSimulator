import javax.swing.*;
import java.awt.event.*;

public class CreateBodyFrame extends JFrame {
    private ActionListener parent;
    private CreateBodyPanel createBodyPanel;

    public CreateBodyFrame(ActionListener parent) {
        this.parent = parent;
        setResizable(false);
    }

    public void setup() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        int configPanelWidth = 500;

        createBodyPanel = new CreateBodyPanel(parent);
        add(createBodyPanel);
        pack();

        createBodyPanel.setup(configPanelWidth);
    }

    public Body getBody() {
        return createBodyPanel.getBody();
    }
}