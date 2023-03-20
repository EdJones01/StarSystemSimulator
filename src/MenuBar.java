import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

public class MenuBar extends JMenuBar implements MenuListener {
    private Stage stage;
    private ActionListener actionListener;
    private JMenu bodiesMenu = new JMenu("Jump To Body");

    public MenuBar(Stage stage) {
        this.stage = stage;
        this.actionListener = stage;

        JMenu fileMenu = new JMenu("File");

        fileMenu.add(createMenuItem("New"));
        fileMenu.add(createMenuItem("Open"));
        fileMenu.add(createMenuItem("Save"));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Add new Body", "createNewBody"));

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(createMenuItem("Show all paths", "showPaths"));
        toolsMenu.add(createMenuItem("Hide all paths", "hidePaths"));
        toolsMenu.add(createMenuItem("Show Gridlines", "showGridlines"));
        toolsMenu.add(createMenuItem("Hide Gridlines", "hideGridlines"));

        bodiesMenu.addMenuListener(this);
        bodiesMenu.setActionCommand("updateBodies");
        toolsMenu.add(bodiesMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("Show help menu", "help"));

        JMenu presetMenu = new JMenu("Presets");
        File[] presets = new File(FileManager.getPresetFolderLocation()).listFiles();
        for (int i = 0; i < presets.length; i++) {
            presetMenu.add(createMenuItem(presets[i].getName(),
                    "preset_" + presets[i].getName()));
        }


        add(fileMenu);
        add(editMenu);
        add(presetMenu);
        add(toolsMenu);
        add(helpMenu);
    }

    public void updateBodiesMenu() {
        bodiesMenu.removeAll();
        for (int i = 0; i < stage.getBodies().size(); i++)
            bodiesMenu.add(createMenuItem(stage.getBodies().get(i).getName(), "jumpTo_" + i));
    }

    private JMenuItem createMenuItem(String name) {
        return createMenuItem(name, name.toLowerCase());
    }

    private JMenuItem createMenuItem(String name, String command) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand(command);
        return menuItem;
    }

    @Override
    public void menuSelected(MenuEvent e) {
        if (e.getSource() == bodiesMenu) {
            System.out.println("yes");
            updateBodiesMenu();
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {

    }

    @Override
    public void menuCanceled(MenuEvent e) {

    }
}
