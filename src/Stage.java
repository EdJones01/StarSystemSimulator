import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Random;

public class Stage extends JPanel implements ActionListener, MouseWheelListener, MouseListener,
        MouseMotionListener, ComponentListener {
    private final int physicsFPS = 60;
    private final int drawFPS = 144;
    private final int minTimerFactor = 1;
    private final int maxTimeFactor = 20;
    private final int minPathSize = 100;
    private final int maxPathSize = 500;
    private final int minPathResolution = 50;
    private final int maxPathResolution = 500;
    private final int UIBodyRadius = 85;
    private final int gridlinesGap = 80;

    private final boolean gameMuted = false;

    private final Random random = new Random();

    private LoadFrame loadFrame;
    private CreateBodyFrame createBodyFrame;

    private int timerFactor = minTimerFactor;
    private int pathSize = minPathSize;
    private int pathResolution = 50;
    private int gameSaveLoadOpacity = 0;
    private int numGeneratePathPerSecond = 2;
    private int generatePathDelay = physicsFPS / numGeneratePathPerSecond;
    private int generatePathCounter = 0;

    private int width;
    private int height;

    private String gameSaveLoadStatus = "";

    private Timer physicsTimer;
    private Timer drawTimer;

    private AffineTransform transform = new AffineTransform();

    private LinkedList<Body> bodies = new LinkedList<>();
    private LinkedList<Explosion> explosions = new LinkedList<>();
    private LinkedList<Vector2D[]> paths = new LinkedList<>();

    private Vector2D center;
    private Vector2D transformedMousePosition = new Vector2D();
    private Vector2D realMousePosition = new Vector2D();
    private Vector2D UIBodyPosition;
    private Vector2D previousRightDragPosition = null;


    private Body selectedBody = null;
    private Body bodyToCreate = null;

    private Rectangle configPanelBounds;
    private Rectangle simulationPanelBounds;
    private Rectangle pauseButtonBounds;
    private Rectangle pathButtonBounds;
    private Rectangle timerSliderBounds;
    private Rectangle pathSizeSliderBounds;
    private Rectangle pathResolutionSliderBounds;

    private boolean movingBody = false;
    private boolean configPanelShown = true;
    private boolean showGridlines = false;
    private boolean runningBeforeMove = false;
    private boolean firstLaunch = true;

    public Stage() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);

        setupTimers();
        setupKeybindings();
    }

    public void setup(int width, int height) {
        this.width = width;
        this.height = height;


        System.out.println(width);
        System.out.println(height);

        setBackground(new Color(10, 10, 10));

        setupBounds();

        UIBodyPosition = new Vector2D(configPanelBounds.x + configPanelBounds.width / 2, (height / 3.2));

        drawTimer.start();

        if (firstLaunch) {
            File[] presets = new File(FileManager.getPresetFolderLocation()).listFiles();
            if (presets.length > 0)
                load(presets[0].getName());
            firstLaunch = false;
        }
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.transform(transform);

        if (showGridlines)
            drawGridlines(g2);

        for (Body body : bodies)
            if (body.isShowPath()) {
                drawPath(g2, body);
            }

        for (Body body : bodies)
            body.draw(g2);

        for (Explosion explosion : explosions)
            explosion.draw(g2);

        AffineTransform identity = new AffineTransform();
        identity.setToIdentity();
        g2.setTransform(identity);

        drawBodyInidicatorArrows(g2);
        if (configPanelShown)
            drawConfigPanel(g2);
        if (gameSaveLoadOpacity > 0)
            drawSaveLoadText(g2);

    }

    private void setupTimers() {
        physicsTimer = new Timer(1000 / physicsFPS, this);
        physicsTimer.setActionCommand("physics");
        drawTimer = new Timer(1000 / drawFPS, this);
        drawTimer.setActionCommand("draw");
    }

    private void setupBounds() {
        if (configPanelShown) {
            configPanelBounds = new Rectangle(width - (width / 5), 0,
                    width - (width - (width / 5)), height);
            simulationPanelBounds = new Rectangle(0, 0, width - configPanelBounds.width, height);
            pauseButtonBounds = new Rectangle(configPanelBounds.x, 0, configPanelBounds.width / 2, 80);
            pathButtonBounds = new Rectangle(configPanelBounds.x + configPanelBounds.width / 2, 0, configPanelBounds.width / 2, 80);
            timerSliderBounds = new Rectangle(configPanelBounds.x + 20, height - 40, width - configPanelBounds.x - 40, 20);
            pathSizeSliderBounds = new Rectangle(configPanelBounds.x + 20, height - 120, width - configPanelBounds.x - 40, 20);
            pathResolutionSliderBounds = new Rectangle(configPanelBounds.x + 20, height - 200, width - configPanelBounds.x - 40, 20);
            center = new Vector2D((width - configPanelBounds.width) / 2, height / 2);
        } else {
            simulationPanelBounds = new Rectangle(0, 0, width, height);
            center = new Vector2D((width) / 2, height / 2);
        }
    }

    private void setupKeybindings() {
        Tools.addKeyBinding(this, KeyEvent.VK_SPACE, "pause", (evt) -> {
            togglePause();
        });
        Tools.addKeyBinding(this, KeyEvent.VK_P, "showPath", (evt) -> {
            toggleShowPath();
        });
        Tools.addKeyBinding(this, KeyEvent.VK_UP, "addPathSize", (evt) -> {
            adjustSelectedBodyVelocity(0, -0.01);
        });
        Tools.addKeyBinding(this, KeyEvent.VK_DOWN, "subPathSize", (evt) -> {
            adjustSelectedBodyVelocity(0, 0.01);
        });
        Tools.addKeyBinding(this, KeyEvent.VK_RIGHT, "subPathResolution", (evt) -> {
            adjustSelectedBodyVelocity(0.01, 0);
        });
        Tools.addKeyBinding(this, KeyEvent.VK_LEFT, "addPathResolution", (evt) -> {
            adjustSelectedBodyVelocity(-0.01, 0);
        });
        Tools.addKeyBinding(this, KeyEvent.VK_DELETE, "delete", (evt) -> {
            if (selectedBody != null) {
                explode(bodies.get(bodies.indexOf(selectedBody)), false);
                selectedBody = null;
            }
        });
        Tools.addKeyBinding(this, KeyEvent.VK_ESCAPE, "config", (evt) -> {
            toggleConfigPanel();
        });
        Tools.addKeyBinding(this, KeyEvent.VK_F, "freeze", (evt) -> {
            toggleFrozen();
        });
    }

    private void adjustSelectedBodyVelocity(double x, double y) {
        if (selectedBody != null) {
            selectedBody.setVelocity(selectedBody.getVelocity().add(new Vector2D(x, y)));
        }
    }

    private void toggleFrozen() {
        if (selectedBody != null)
            selectedBody.setFrozen(!selectedBody.isFrozen());
    }

    public BufferedImage generateThumbnail() {
        BufferedImage image = new BufferedImage(configPanelBounds.width, configPanelBounds.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        paint(g2);
        return image.getSubimage(0, 0, simulationPanelBounds.width, simulationPanelBounds.height);
    }

    private void openLoadFrame() {
        loadFrame = new LoadFrame(this, FileManager.getSaveFolderLocation());
        loadFrame.setup();
        loadFrame.setLocationRelativeTo(this);
        loadFrame.setVisible(true);
    }

    private void openCreateBodyFrame() {
        createBodyFrame = new CreateBodyFrame(this);
        createBodyFrame.setup();
        createBodyFrame.setLocationRelativeTo(this);
        createBodyFrame.setVisible(true);
    }

    private void toggleConfigPanel() {
        configPanelShown = !configPanelShown;

        if (configPanelShown) {
            setupBounds();
        }
    }

    private void save() {
        physicsTimer.stop();
        String filename = Tools.showInputDialog("Enter filename");
        if (filename != null && !filename.equals("")) {
            boolean success = FileManager.saveToJSON(bodies, filename) &&
                    FileManager.saveImage(generateThumbnail(), filename);
            if (success) {
                gameSaveLoadOpacity = 255;
                gameSaveLoadStatus = "Game Saved";
            }
        }
    }

    private void load(String filename) {
        LinkedList<Body> success = FileManager.loadFromJSON(filename);

        if (success != null) {
            selectedBody = null;
            bodies = success;
            physicsTimer.stop();
            gameSaveLoadOpacity = 255;
            gameSaveLoadStatus = "Game Loaded";
            regeneratePaths();
        }
    }

    private void togglePause() {
        if (physicsTimer.isRunning())
            physicsTimer.stop();
        else
            physicsTimer.start();
    }

    private void toggleShowPath() {
        if (selectedBody != null)
            selectedBody.setShowPath(!selectedBody.isShowPath());
        else {
            boolean allShown = true;
            for (Body body : bodies)
                if (!body.isShowPath())
                    allShown = false;

            for (Body body : bodies)
                if (allShown)
                    body.setShowPath(false);
                else
                    body.setShowPath(true);
        }
    }

    private int generateRandomBodyID() {
        int id;
        do {
            id = random.nextInt(Integer.MAX_VALUE);
        } while (checkIfIDPresent(id, bodies));
        return id;
    }

    private boolean checkIfIDPresent(int id, LinkedList<Body> bodies) {
        for (Body body : bodies)
            if (body.getId() == id)
                return true;
        return false;
    }

    private void regeneratePaths() {
        paths = new LinkedList<>();
        for (int i = 0; i < bodies.size(); i++)
            paths.add(new Vector2D[]{});

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            if (body.isShowPath())
                paths.set(i, generatePath(body, bodies));
        }
    }

    public Vector2D[] generatePath(Body body, LinkedList<Body> bodies) {
        LinkedList<Body> clonedBodies = new LinkedList<>();
        Body currentClone = null;
        for (Body b : bodies) {
            clonedBodies.add(b.clone());
            if (b.getId() == body.getId())
                currentClone = clonedBodies.getLast();
        }

        Vector2D[] path = new Vector2D[pathSize];
        path[0] = currentClone.getPosition().copy();
        for (int i = 1; i < pathSize; i++) {
            for (int j = 0; j < pathResolution; j++) {
                for (Body clone : clonedBodies) {
                    clone.update(clonedBodies);
                }
            }

            path[i] = currentClone.getPosition().copy();
        }
        return path;
    }

    private LinkedList<Body> checkCollisions(LinkedList<Body> bodies, boolean virtual) {
        LinkedList<Body> destroyedBodies = new LinkedList<>();
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = 0; j < bodies.size(); j++) {
                Body currentBody = bodies.get(i);
                Body otherBody = bodies.get(j);
                if (currentBody != otherBody) {
                    if (currentBody.distance(otherBody) < currentBody.getRadius() + otherBody.getRadius()) {
                        destroyedBodies.addAll(resolveObjectCollision(bodies.get(i), bodies.get(j), virtual));
                    }
                }
            }
        }
        return destroyedBodies;
    }

    private void update() {
        for (Body body : bodies)
            body.update(bodies);
        checkCollisions(bodies, false);

        for (int i = 0; i < explosions.size(); i++) {
            Explosion e = explosions.get(i);
            e.update();
            if (e.getAlpha() <= 0)
                explosions.remove(e);
        }

        generatePathCounter++;
        if (generatePathCounter == generatePathDelay) {
            regeneratePaths();
            generatePathCounter = 0;
        }
    }

    private LinkedList<Body> resolveObjectCollision(Body body1, Body body2, boolean virtual) {
        LinkedList<Body> destroyedBodies = new LinkedList<>();
        if (!body1.isStar() && body2.isStar()) {
            explode(body1, virtual);
            destroyedBodies.add(body1);
        } else if (body1.isStar() && !body2.isStar()) {
            explode(body2, virtual);
            destroyedBodies.add(body2);
        } else {
            explode(body1, virtual);
            explode(body2, virtual);
            destroyedBodies.add(body1);
            destroyedBodies.add(body2);
        }
        return destroyedBodies;
    }

    private void explode(Body body, boolean virtual) {
        bodies.remove(body);
        double explosionSize = Explosion.BIG;
        if (body.isStar())
            explosionSize = Explosion.HUGE;
        if (!virtual) {
            explosions.add(new Explosion(body.getPosition().copy(), explosionSize));
            if (explosionSize == Explosion.BIG)
                Tools.playSound("bangMedium", gameMuted);
            else
                Tools.playSound("bangLarge", gameMuted);
        }
        if (selectedBody != null)
            if (body.getId() == selectedBody.getId())
                selectedBody = null;
    }

    private Body getPressedBody() {
        for (Body body : bodies) {
            if (body.getPosition().distance(transformedMousePosition) < body.getRadius())
                return body;
        }
        return null;
    }

    private void openHelpFrame() {
        HelpFrame helpFrame = new HelpFrame();
        helpFrame.setLocationRelativeTo(this);
        helpFrame.setVisible(true);
    }

    private String incrementName(String name, boolean first) {
        if (name == null || name.length() == 0) {
            return "";
        }

        int i = name.length() - 1;
        while (i >= 0 && Character.isDigit(name.charAt(i))) {
            i--;
        }

        if (i == name.length() - 1) {
            if (first)
                return name;
            return name + " 1";
        } else {
            try {
                int num = Integer.parseInt(name.substring(i + 1)) + 1;
                return name.substring(0, i + 1) + num;
            } catch (NumberFormatException e) {
                return name + " 1";
            }
        }
    }

    private Body cloneBody(Body body, boolean newCreation) {
        Body clone = body.clone();
        clone.setId(generateRandomBodyID());
        clone.setName(incrementName(clone.getName(), newCreation));
        return clone;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("physics")) {
            for (int i = 0; i < timerFactor; i++) {
                update();
            }
        }
        if (cmd.equals("draw")) {
            repaint();
        }
        if (cmd.equals("new")) {
            bodies = new LinkedList<>();
        }
        if (cmd.equals("open")) {
            if (FileManager.savesPresent())
                openLoadFrame();
        }
        if (cmd.equals("save")) {
            save();
        }
        if (cmd.equals("load")) {
            load(loadFrame.getSelectedFilename());
            loadFrame.dispose();
        }
        if (cmd.equals("help")) {
            openHelpFrame();
        }
        if (cmd.equals("showPaths")) {
            for (Body body : bodies)
                body.setShowPath(true);
        }
        if (cmd.equals("hidePaths")) {
            for (Body body : bodies)
                body.setShowPath(false);
        }
        if (cmd.contains("preset")) {
            load(cmd.split("_")[1]);
        }
        if (cmd.equals("showGridlines")) {
            showGridlines = true;
        }
        if (cmd.equals("hideGridlines")) {
            showGridlines = false;
        }
        if (cmd.equals("create")) {
            Body clone = createBodyFrame.getBody();
            clone.setMass(clone.getRealMass());
            bodyToCreate = clone;
            selectedBody = null;
            createBodyFrame.dispose();
        }
        if (cmd.equals("createNewBody")) {
            physicsTimer.stop();
            openCreateBodyFrame();
        }
        if (cmd.contains("jumpTo_")) {
            translateToBody(bodies.get(Integer.parseInt(new StringBuilder(cmd).reverse().
                    toString().split("_")[0])));
        }
    }

    private void translateToBody(Body body) {
        transform.setToTranslation(-body.getPosition().getX() + center.getX(),
                -body.getPosition().getY() + center.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = -e.getWheelRotation();
        timerFactor += notches;
        timerFactor = Tools.constrainInt(timerFactor, minTimerFactor, maxTimeFactor);
        physicsTimer.start();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private Vector2D getTransformedMousePosition(MouseEvent e) {
        return new Vector2D(e.getX() - transform.getTranslateX(), e.getY() - transform.getTranslateY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        transformedMousePosition = getTransformedMousePosition(e);
        realMousePosition = new Vector2D(e.getX(), e.getY());
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (Tools.mouseEventWithin(e, simulationPanelBounds)) {
                selectedBody = getPressedBody();
                if (selectedBody != null) {
                    movingBody = true;
                    runningBeforeMove = physicsTimer.isRunning();
                    physicsTimer.stop();
                }
            }
            if (Tools.mouseEventWithin(e, configPanelBounds)) {
                if (Tools.mouseEventWithin(e, pauseButtonBounds)) {
                    togglePause();
                }
                if (Tools.mouseEventWithin(e, pathButtonBounds)) {
                    toggleShowPath();
                }
                if (UIBodyPosition.distance(realMousePosition) < UIBodyRadius) {
                    if (selectedBody != null) {
                        Body clone = cloneBody(selectedBody, false);
                        bodies.addLast(clone);
                        selectedBody = clone;
                        physicsTimer.stop();
                        movingBody = true;
                    } else if (bodyToCreate != null) {
                        Body clone = cloneBody(bodyToCreate, true);
                        bodies.addLast(clone);
                        selectedBody = clone;
                        physicsTimer.stop();
                        movingBody = true;
                        bodyToCreate = null;
                    }
                }
            }
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
            if (Tools.mouseEventWithin(e, simulationPanelBounds)) {
                physicsTimer.stop();
                openCreateBodyFrame();
            }
            if (Tools.mouseEventWithin(e, configPanelBounds)) {
                if (UIBodyPosition.distance(realMousePosition) < UIBodyRadius) {
                    if (selectedBody != null) {
                        translateToBody(selectedBody);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (movingBody) {
                if (runningBeforeMove)
                    physicsTimer.start();
                movingBody = false;
                regeneratePaths();
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            previousRightDragPosition = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        transformedMousePosition = getTransformedMousePosition(e);
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (movingBody) {
                selectedBody.setPosition(transformedMousePosition.copy());
                //regeneratePaths();
            }

            int relativeX = e.getX() - timerSliderBounds.x;
            if (Tools.mouseEventWithin(e, timerSliderBounds)) {
                timerFactor = (int) Tools.map(relativeX, 0, timerSliderBounds.width,
                        minTimerFactor, maxTimeFactor);
                timerFactor = Tools.constrainInt(timerFactor, minTimerFactor, maxTimeFactor);
            }
            if (Tools.mouseEventWithin(e, pathSizeSliderBounds)) {
                pathSize = (int) Tools.map(relativeX, 0, pathSizeSliderBounds.width,
                        minPathSize, maxPathSize);
                pathSize = Tools.constrainInt(pathSize, minPathSize, maxPathSize);
            }
            if (Tools.mouseEventWithin(e, pathResolutionSliderBounds)) {
                pathResolution = maxPathResolution + minPathResolution - (int) Tools.map(relativeX, 0, pathResolutionSliderBounds.width,
                        minPathResolution, maxPathResolution);
                pathResolution = Tools.constrainInt(pathResolution, minPathResolution, maxPathResolution);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            Vector2D realMousePosition = new Vector2D(e.getX(), e.getY());
            if (previousRightDragPosition == null)
                previousRightDragPosition = realMousePosition;

            Vector2D movement = realMousePosition.sub(previousRightDragPosition);
            transform.translate(movement.getX(), movement.getY());
            previousRightDragPosition = realMousePosition.copy();
        }
    }

    private void drawGridlines(Graphics2D g2) {
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(255, 255, 255, 5));
        for (int i = 0; i < (simulationPanelBounds.width / gridlinesGap) + 1; i++)
            g2.drawLine((int) -transform.getTranslateX() + i * gridlinesGap + (int) (transform.getTranslateX() % gridlinesGap),
                    (int) -transform.getTranslateY(),
                    (int) -transform.getTranslateX() + i * gridlinesGap + (int) (transform.getTranslateX() % gridlinesGap),
                    simulationPanelBounds.height - (int) transform.getTranslateY());

        for (int i = 0; i < (simulationPanelBounds.width / gridlinesGap) + 1; i++)
            g2.drawLine(
                    (int) -transform.getTranslateX(),
                    (int) -transform.getTranslateY() + i * gridlinesGap + (int) (transform.getTranslateY() % gridlinesGap),
                    simulationPanelBounds.width - (int) transform.getTranslateX(),
                    (int) -transform.getTranslateY() + i * gridlinesGap + (int) (transform.getTranslateY() % gridlinesGap));
    }

    private void drawPath(Graphics2D g2, Body body) {
        g2.setColor(body.getPrimaryColor());
        g2.setStroke(new BasicStroke(2));
        try {
            Vector2D[] path = paths.get(bodies.indexOf(body));

            for (int i = 0; i < path.length - 1; i++) {
                if (path[i + 1] != null) {
                    Vector2D p1 = path[i];
                    Vector2D p2 = path[i + 1];
                    g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                }
            }
        } catch (Exception e) {
        }
    }

    private void drawRunning(Graphics2D g2, int x, int y) {
        g2.setColor(Color.white);
        g2.setFont(Tools.getCustomFont(30));
        String text = "Running";
        if (!physicsTimer.isRunning())
            text = "Paused";
        g2.drawString(text, x, y);
        g2.setFont(Tools.getCustomFont(20));
        g2.drawString("[ SPACE ]", x, y + 25);
    }

    private void drawShowPathText(Graphics2D g2, int x, int y) {
        g2.setColor(Color.white);
        g2.setFont(Tools.getCustomFont(30));

        String text = "Hide Path";

        if (selectedBody != null) {
            if (!selectedBody.isShowPath())
                text = "Show Path";
        } else {
            for (Body body : bodies) {
                if (!body.isShowPath())
                    text = "Show Path";
            }
        }

        g2.drawString(text, x, y);
        g2.setFont(Tools.getCustomFont(20));
        g2.drawString("[ P ]", width - 66, y + 25);
    }

    private void drawTimeFactor(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.drawRect(timerSliderBounds.x, timerSliderBounds.y, timerSliderBounds.width, timerSliderBounds.height);
        g2.fillRect(timerSliderBounds.x, timerSliderBounds.y,
                (int) Tools.map(timerFactor, minTimerFactor, maxTimeFactor, 0, timerSliderBounds.width), timerSliderBounds.height);
        g2.setFont(Tools.getCustomFont(28));
        g2.drawString("Speed = " + timerFactor + "x", timerSliderBounds.x, timerSliderBounds.y - 10);
    }

    private void drawPathSize(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.drawRect(pathSizeSliderBounds.x, pathSizeSliderBounds.y, pathSizeSliderBounds.width, pathSizeSliderBounds.height);
        g2.fillRect(pathSizeSliderBounds.x, pathSizeSliderBounds.y,
                (int) Tools.map(pathSize, minPathSize, maxPathSize, 0, pathSizeSliderBounds.width), pathSizeSliderBounds.height);
        g2.setFont(Tools.getCustomFont(28));
        g2.drawString("Path size = " + pathSize, pathSizeSliderBounds.x, pathSizeSliderBounds.y - 10);
    }

    private void drawPathResolution(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.drawRect(pathResolutionSliderBounds.x, pathResolutionSliderBounds.y, pathResolutionSliderBounds.width, pathResolutionSliderBounds.height);
        g2.fillRect(pathResolutionSliderBounds.x, pathResolutionSliderBounds.y,
                pathResolutionSliderBounds.width - (int) Tools.map(pathResolution, 50, maxPathResolution, 0,
                        pathResolutionSliderBounds.width), pathResolutionSliderBounds.height);
        g2.setFont(Tools.getCustomFont(28));
        g2.drawString("Path Resolution = " + pathResolution, pathResolutionSliderBounds.x, pathResolutionSliderBounds.y - 10);
    }

    private void drawConfigPanel(Graphics2D g2) {
        g2.setColor(new Color(47, 47, 47));
        g2.fillRect(configPanelBounds.x, 0, width - configPanelBounds.x, height);

        drawRunning(g2, configPanelBounds.x + 20, 40);

        drawTimeFactor(g2);
        drawPathSize(g2);
        drawPathResolution(g2);

        drawShowPathText(g2, width - 150, 40);

        if (selectedBody != null) {
            drawBodyUI(g2, selectedBody);
        } else if (bodyToCreate != null) {
            drawBodyUI(g2, bodyToCreate);
        }
    }

    private void drawBodyInidicatorArrows(Graphics2D g2) {
        g2.setColor(Color.white);
        int padding = 30;

        int offLeftBodies = 0;
        int offRightBodies = 0;
        int offTopBodies = 0;
        int offBottomBodies = 0;

        for (Body body : bodies) {
            int x = (int) (body.getPosition().getX() + transform.getTranslateX());
            int y = (int) (body.getPosition().getY() + transform.getTranslateY());

            if (x + body.getRadius() < 0)
                offLeftBodies++;
            if (y + body.getRadius() < 0)
                offTopBodies++;
            if (x - body.getRadius() > simulationPanelBounds.width)
                offRightBodies++;
            if (y - body.getRadius() > simulationPanelBounds.height)
                offBottomBodies++;
        }
        int centerX = width / 2;
        if (configPanelShown)
            centerX = simulationPanelBounds.width / 2;


        if (offLeftBodies > 0) {
            drawIndicatiorArrow(g2, centerX, simulationPanelBounds.height / 2,
                    simulationPanelBounds.x + padding, simulationPanelBounds.height / 2, offLeftBodies, 5, 0);
        }
        if (offTopBodies > 0) {
            drawIndicatiorArrow(g2, centerX, simulationPanelBounds.height / 2,
                    centerX, padding, offTopBodies, 0, 5);
        }
        if (offRightBodies > 0) {
            if (configPanelShown)
                drawIndicatiorArrow(g2, centerX, simulationPanelBounds.height / 2,
                        simulationPanelBounds.width - padding, simulationPanelBounds.height / 2, offRightBodies, -5, 0);
            else
                drawIndicatiorArrow(g2, centerX, simulationPanelBounds.height / 2,
                        width - padding, simulationPanelBounds.height / 2, offRightBodies, -5, 0);
        }
        if (offBottomBodies > 0) {
            drawIndicatiorArrow(g2, centerX, simulationPanelBounds.height / 2,
                    centerX, simulationPanelBounds.height - padding, offBottomBodies, 0, -5);
        }

    }

    private void drawIndicatiorArrow(Graphics2D g2, int x1, int y1, int x2, int y2, int num,
                                     int xOffset, int yOffset) {
        AffineTransform tx = new AffineTransform();
        tx.setToIdentity();
        double angle = Math.atan2(y2 - y1, x2 - x1);
        tx.translate(x2, y2);
        tx.rotate((angle - Math.PI / 2d));

        Polygon arrowHead = new Polygon();
        int scale = 20;
        arrowHead.addPoint(0, scale);
        arrowHead.addPoint(-scale, -scale);
        arrowHead.addPoint(scale, -scale);

        Graphics2D g = (Graphics2D) g2.create();
        g.setTransform(tx);
        g.setColor(Color.white);
        g.fill(arrowHead);
        g.setColor(Color.black);
        g.setFont(Tools.getCustomFont(20));
        tx.rotate(-((angle - Math.PI / 2d)));
        g.setTransform(tx);

        String text = num + "";
        if (num > 9)
            text = "9+";
        Tools.centerString(g, new Rectangle(-scale, -scale, 2 * scale, 2 * scale), xOffset, yOffset, text);
        g.dispose();
    }

    private void drawBodyUI(Graphics2D g2, Body body) {
        Body UIBody = body.clone();

        Stroke previousStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(170));
        g2.setColor(new Color(35, 35, 35));
        g2.drawLine((int) UIBodyPosition.getX() + 66, (int) UIBodyPosition.getY() + 28, 3000, height);
        g2.setStroke(previousStroke);

        UIBody.setRadius(85);
        UIBody.setPosition(new Vector2D(UIBodyPosition.getX(), UIBodyPosition.getY()));
        UIBody.draw(g2);

        g2.setFont(Tools.getCustomFont(30));
        Rectangle bodyNameBounds = (Rectangle) configPanelBounds.clone();
        bodyNameBounds.height -= 600;
        String star = "";
        if (UIBody.isStar())
            star += "*";
        Tools.centerString(g2, bodyNameBounds, UIBody.getName() + star);

        g2.setFont(Tools.getCustomFont(20));
        g2.setColor(Color.white);

        String[] properties = UIBody.getPropertiesStringArray();
        int newline = g2.getFont().getSize() + 5;
        int y = height / 2;
        for (int i = 0; i < properties.length; i++) {
            g2.drawString(properties[i], configPanelBounds.x + 20, y += newline);
        }
    }

    private void drawSaveLoadText(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, gameSaveLoadOpacity));
        g2.setFont(Tools.getCustomFont(40));
        g2.drawString(gameSaveLoadStatus, 16, 40);
        gameSaveLoadOpacity -= 4;
    }

    public LinkedList<Body> getBodies() {
        return bodies;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setup(width, height);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
