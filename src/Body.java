import java.awt.*;
import java.util.LinkedList;

public class Body extends Particle {
    private Color primaryColor;
    private Color secondaryColor;
    private String name;
    private double mass;
    private double radius;
    private boolean isStar;
    private int id;

    private boolean beingDragged;
    private boolean showPath;
    private boolean frozen;

    public Body(double x, double y, double xVel, double yVel, double mass, String name, Color primaryColor, Color secondaryColor, boolean isStar) {
        super(new Vector2D(x, y), new Vector2D(xVel, yVel));
        setPosition(new Vector2D(x, y));
        setVelocity(new Vector2D(xVel, yVel));
        setName(name);
        setPrimaryColor(primaryColor);
        setSecondaryColor(secondaryColor);
        setMass(mass);
        setIsStar(isStar);
        if (isStar)
            setRadius(45);
        else
            setRadius(15 + radiusSizeOffset());
        setBeingDragged(false);
        setShowPath(false);
        setFrozen(false);
    }

    public Body(double x, double y, double xVel, double yVel, double mass, String name, Color primaryColor, Color secondaryColor, boolean isStar, int id) {
        this(x, y, xVel, yVel, mass, name, primaryColor, secondaryColor, isStar);
        setId(id);
    }

    public Body(BodyInformation bodyInformation, double x, double y, double xVel, double yVel) {
        this(x, y, xVel, yVel, bodyInformation.getAdjustedMass(), bodyInformation.getName(),
                bodyInformation.getPrimaryColor(), bodyInformation.getSecondaryColor(), bodyInformation.isStar());
    }

    public Body(double xVel, double yVel, String name, Color primaryColor, Color secondaryColor, boolean isStar) {
        this(0, 0, xVel, yVel, 0, name, primaryColor, secondaryColor, isStar);
    }

    public Body clone() {
        Body clone = new Body(getPosition().getX(), getPosition().getY(), getVelocity().getX(), getVelocity().getY(), mass, name, primaryColor, secondaryColor, isStar, id);
        clone.setBeingDragged(beingDragged);
        clone.setShowPath(showPath);
        clone.setFrozen(frozen);
        return clone;
    }

    public double distance(Body other) {
        return getPosition().distance(other.getPosition());
    }

    private double radiusSizeOffset() {
        return Tools.map(mass, Universe.MERCURY.getAdjustedMass(), Universe.JUPITER.getAdjustedMass(), -5, 5);
    }

    public void update(LinkedList<Body> bodies) {
        if(!frozen)
            for (Body other : bodies) {
                if (other != this) {
                    Vector2D forceDirection = other.getPosition().sub(getPosition()).normalize();
                    Vector2D force = (forceDirection.mul(Universe.G).mul(mass).mul(other.getMass()))
                            .div(Math.pow(getPosition().distance(other.getPosition()), 2));
                    setVelocity(getVelocity().add(force.div(mass)));
                    super.update();
                }
            }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(secondaryColor);
        g2.fillOval((int) (getPosition().getX() - radius), (int) (getPosition().getY() - radius),
                (int) (radius * 2), (int) (radius * 2));
        double smallerRadius = radius * 0.8;
        g2.setColor(primaryColor);
        g2.fillOval((int) (getPosition().getX() - smallerRadius), (int) (getPosition().getY() - smallerRadius),
                (int) (smallerRadius * 2), (int) (smallerRadius * 2));

        if(frozen) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval((int) (getPosition().getX() - radius), (int) (getPosition().getY() - radius),
                    (int) (radius * 2), (int) (radius * 2));
        }
    }

    public String[] getPropertiesStringArray() {
        return new String[]{
                "Position: x: " + (int) getPosition().getX() + " y: " + (int) getPosition().getY(),
                "Velocity: x: " + Tools.round(getVelocity().getX(), 3) + " y: " + Tools.round(getVelocity().getY(), 3),
                "Mass: " + Tools.doubleToScientificNotation(getRealMass(), 3)
        };
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMass() {
        return mass;
    }

    public double getRealMass() {
        return mass/Universe.massScaleFactor;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setIsStar(boolean star) {
        isStar = star;
    }

    public boolean isBeingDragged() {
        return beingDragged;
    }

    public void setBeingDragged(boolean beingDragged) {
        this.beingDragged = beingDragged;
    }

    public boolean isShowPath() {
        return showPath;
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
}
