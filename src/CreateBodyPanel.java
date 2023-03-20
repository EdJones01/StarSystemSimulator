import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;

public class CreateBodyPanel extends JPanel implements ActionListener, MouseListener, ChangeListener {
    private final int padding = 20;
    private final double minVelocity = -0.1;
    private final double maxVelocity = 0.1;
    private final int sliderResolution = 100;

    private ActionListener parent;

    private Body body;

    private JTextFieldLimit nameTextField;

    private JSlider velocityYSlider;
    private JSlider velocityXSlider;
    private JSlider mantissaSlider;
    private JSlider exponentSlider;

    private JLabel velocityLabel;
    private JLabel massLabel;

    //private JCheckBox starCheckBox;

    private Rectangle configPanelBounds;
    private Rectangle drawPanelBounds;
    private Rectangle randomiseButtonBounds;
    private Rectangle createButtonBounds;

    public CreateBodyPanel(ActionListener parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        addMouseListener(this);
    }

    public void setup(int configPanelWidth) {
        setupBounds(configPanelWidth);

        setLayout(null);

        nameTextField = new JTextFieldLimit(20);
        nameTextField.setBounds(50, 25, 400, 67);
        nameTextField.setFont(Tools.getCustomFont(30));
        nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
        nameTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                actionPerformed(new ActionEvent(nameTextField, 0, ""));
            }
        });
        add(nameTextField);

        velocityYSlider = new JSlider();
        velocityYSlider.setOrientation(SwingConstants.VERTICAL);
        velocityYSlider.setBounds(22, 129, 70, 210);
        velocityYSlider.setMinimum(sliderResolution);
        velocityYSlider.setMaximum(2 * sliderResolution);
        velocityYSlider.setValue(sliderResolution);
        velocityYSlider.addChangeListener(this);
        add(velocityYSlider);

        velocityXSlider = new JSlider();
        velocityXSlider.setBounds(113, 286, 280, 70);
        velocityXSlider.setMinimum(sliderResolution);
        velocityXSlider.setMaximum(2 * sliderResolution);
        velocityXSlider.setValue(sliderResolution);
        velocityXSlider.addChangeListener(this);
        add(velocityXSlider);

        velocityLabel = new JLabel("Velocity = (-1, 0)");
        velocityLabel.setBounds(126, 189, 400, 52);
        velocityLabel.setFont(Tools.getCustomFont(20));
        add(velocityLabel);

        mantissaSlider = new JSlider();
        mantissaSlider.setBounds(113, 468, 280, 70);
        mantissaSlider.setMinimum(sliderResolution);
        mantissaSlider.setMaximum(2 * sliderResolution);
        mantissaSlider.setValue(sliderResolution);
        mantissaSlider.addChangeListener(this);
        add(mantissaSlider);

        exponentSlider = new JSlider();
        exponentSlider.setBounds(113, 510, 280, 70);
        exponentSlider.setMinimum(23);
        exponentSlider.setMaximum(26);
        exponentSlider.setValue(26);
        exponentSlider.addChangeListener(this);
        add(exponentSlider);

//        starCheckBox = new JCheckBox("Is this a star?");
//        starCheckBox.setBounds(41, 363, 200, 23);
//        starCheckBox.setFont(Tools.getCustomFont(20));
//        starCheckBox.addActionListener(this);
//        add(starCheckBox);

        massLabel = new JLabel("Mass = 1.23 * 10^25");
        massLabel.setBounds(113, 405, 247, 52);
        massLabel.setFont(Tools.getCustomFont(20));
        add(massLabel);

        randomise();
    }

    private void setupInitialComponentValues() {
        nameTextField.setText(body.getName());

        Vector2D velocity = body.getVelocity().copy();
        String velocityText = "Velocity = (" + Tools.round(velocity.getX(), 3) + ", " +
                Tools.round(velocity.getY(), 3) + ")";

        System.out.println(velocityText);

        velocityLabel.setText(velocityText);
        massLabel.setText("Mass = " + Tools.doubleToScientificNotation(body.getRealMass(), 3));

        String stringMass = Tools.doubleToScientificNotation(body.getMass(), 3);
        double mantissa = Double.parseDouble(stringMass.substring(0, stringMass.indexOf("*") - 1));
        mantissaSlider.setValue((int) Tools.map(mantissa, 0, 10, sliderResolution + 1, 2 * sliderResolution - 1));
        int exponent = Integer.parseInt(stringMass.substring(stringMass.indexOf("^") + 1));
        System.out.println(exponent);
        exponentSlider.setValue(exponent);

        //starCheckBox.setSelected(body.isStar());

        int xSliderValue = (int) (Tools.map(body.getVelocity().getY(), minVelocity, maxVelocity,
                sliderResolution, 2 * sliderResolution));
        int ySliderValue = (int) (Tools.map(body.getVelocity().getX(), minVelocity, maxVelocity,
                sliderResolution, 2 * sliderResolution));

        velocityYSlider.setValue(ySliderValue);
        velocityXSlider.setValue(xSliderValue);


    }

    private void update() {
        double velocityX = Tools.map(velocityXSlider.getValue(), sliderResolution, 2 * sliderResolution, -0.1, 0.1);
        double velocityY = Tools.map(velocityYSlider.getValue(), sliderResolution, 2 * sliderResolution, -0.1, 0.1);
        velocityLabel.setText("Velocity = (" + Tools.round(velocityX, 3) + ", " +
                Tools.round(velocityY, 3) + ")");

        double mass = Tools.map(mantissaSlider.getValue(), sliderResolution, 2 * sliderResolution, 0.01, 9.99)
                * Math.pow(10, exponentSlider.getValue());

        massLabel.setText("Mass = " + Tools.doubleToScientificNotation(mass, 3));

        if (nameTextField.getText().equals(""))
            nameTextField.setText("Cool Planet");

        body.setName(nameTextField.getText());
        body.setVelocity(new Vector2D(velocityX, velocityY));
        body.setMass(mass * Universe.massScaleFactor);
        //body.setIsStar(starCheckBox.isSelected());
        body.setIsStar(false);
    }

    private void setupBounds(int configPanelWidth) {
        configPanelBounds = new Rectangle(0, 0, configPanelWidth, getHeight());
        drawPanelBounds = new Rectangle(configPanelWidth, 0, getWidth() - configPanelWidth, getHeight());
        randomiseButtonBounds = new Rectangle(drawPanelBounds.x + padding, padding,
                drawPanelBounds.width - 2 * padding, 50);
        createButtonBounds = new Rectangle(drawPanelBounds.x + padding, (int) (drawPanelBounds.getHeight() - padding - 50),
                drawPanelBounds.width - 2 * padding, 50);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(47, 47, 47));
        g2.fillRect(configPanelBounds.x, configPanelBounds.y, configPanelBounds.width, configPanelBounds.height);

        if (body != null) {
            Stroke previousStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(170));
            g2.setColor(new Color(35, 35, 35));
            g2.drawLine((int) body.getPosition().getX() + 66, (int) body.getPosition().getY() + 28, 3000, getHeight() * 2);
            g2.setStroke(previousStroke);
            body.draw(g2);
            drawName(g2);
            drawCreateButton(g2);
        }

        drawRandomiseButton(g2);
    }

    private void drawName(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.setFont(Tools.getCustomFont(30));
        String name = body.getName();
        if (body.isStar())
            name += "*";
        Tools.centerString(g2, new Rectangle(drawPanelBounds.x, drawPanelBounds.y,
                drawPanelBounds.width, drawPanelBounds.height - 250), name);
    }

    private void drawRandomiseButton(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.fillRect(randomiseButtonBounds.x, randomiseButtonBounds.y, randomiseButtonBounds.width,
                randomiseButtonBounds.height);
        g2.setColor(new Color(47, 47, 47));
        g2.setFont(Tools.getCustomFont(30));
        Tools.centerString(g2, randomiseButtonBounds, "Randomise");
    }

    private void drawCreateButton(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.fillRect(createButtonBounds.x, createButtonBounds.y, createButtonBounds.width,
                createButtonBounds.height);
        g2.setColor(new Color(47, 47, 47));
        g2.setFont(Tools.getCustomFont(30));
        Tools.centerString(g2, createButtonBounds, "Create");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println("yes");
        update();
        repaint();
    }

    private void create() {
        parent.actionPerformed(new ActionEvent(this, 0, "create"));
    }

    private void randomise() {
        body = Universe.randomBody();
        System.out.println("At inception : " + body.isStar());

        body.setPosition(new Vector2D(drawPanelBounds.x + drawPanelBounds.width / 2, getHeight() / 2));
        body.setRadius(85);
        setupInitialComponentValues();
    }

    public Body getBody() {
        BodyInformation information = new BodyInformation(body.getName(), body.getMass(), 0, body.getPrimaryColor(),
                body.getSecondaryColor(), false, body.isStar());
        return new Body(information, 0, 0, body.getVelocity().getX(), body.getVelocity().getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (Tools.mouseEventWithin(e, createButtonBounds)) {
                if (body != null)
                    create();
            }
            if (Tools.mouseEventWithin(e, randomiseButtonBounds)) {
                randomise();
            }
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }
}

class JTextFieldLimit extends JTextField {
    private int limit;

    public JTextFieldLimit(int limit) {
        super();
        this.limit = limit;
    }

    @Override
    protected Document createDefaultModel() {
        return new LimitDocument();
    }

    private class LimitDocument extends PlainDocument {

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }

    }
}