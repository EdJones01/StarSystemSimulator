import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Random;

public class Universe {

    public static final double G = 6.674 * Math.pow(10, -11);
    public static double massScaleFactor = 0.00000000000000000001;

    //public static final BodyInformation SOL = new BodyInformation(
    //        "Sol", 1.9891 * Math.pow(10, 30), 0, new Color(255, 250, 169), new Color(225, 241, 93), false, true);
    public static final BodyInformation SOL = new BodyInformation(
            "Sol", 1.9891 * Math.pow(10, 31), 0, new Color(255, 250, 169), new Color(225, 241, 93), false, true);

    public static final BodyInformation MERCURY = new BodyInformation(
            "Mercury", 3.3022 * Math.pow(10, 23), 58, new Color(255, 112, 11), new Color(255, 183, 52), false, false);
    public static final BodyInformation VENUS = new BodyInformation(
            "Venus", 4.8685 * Math.pow(10, 24), 108.2, new Color(255, 188, 58), new Color(226, 129, 35), false, false);
    public static final BodyInformation EARTH = new BodyInformation(
            "Earth", 5.9736 * Math.pow(10, 24), 149.6, new Color(11, 126, 219), new Color(55, 155, 37), false, false);
    public static final BodyInformation MARS = new BodyInformation(
            "Mars", 6.4185 * Math.pow(10, 23), 227.9, new Color(202, 39, 42), new Color(235, 50, 48), false, false);
    public static final BodyInformation JUPITER = new BodyInformation(
            "Jupiter", 1.8986 * Math.pow(10, 27), 778.5, new Color(255, 133, 92), new Color(203, 104, 65), false, false);
    public static final BodyInformation SATURN = new BodyInformation(
            "Saturn", 5.6846 * Math.pow(10, 26), 1434, new Color(255, 188, 58), new Color(242, 149, 43), true, false);
    public static final BodyInformation URANUS = new BodyInformation(
            "Uranus", 8.6810 * Math.pow(10, 25), 2871, new Color(99, 179, 238), new Color(8, 128, 225), false, false);
    public static final BodyInformation NEPTUNE = new BodyInformation(
            "Neptune", 10.243 * Math.pow(10, 25), 4495, new Color(32, 130, 253), new Color(2, 90, 250), false, false);
    public static final BodyInformation PLUTO = new BodyInformation(
            "Pluto", 1.25 * Math.pow(10, 22), 5900, new Color(193, 193, 193), new Color(86, 86, 86), false, false);
    public static final BodyInformation MOON = new BodyInformation(
            "Moon", 7.349 * Math.pow(10, 22), 0, new Color(193, 193, 193), new Color(86, 86, 86), false, false);

    public static Body randomBody() {
        String[] names;
        try {
            names = Tools.readFromFile(FileManager.getResourcesFolderLocation() + "names.txt");
        } catch (FileNotFoundException e) {
            names = new String[] {"Cool Planet"};
        }
        String name = names[new Random().nextInt(names.length)];
        Vector2D velocity = Vector2D.random().mul(0.1);
        int smallestPower = 23;
        int biggestPower = 31;
        double mantissa = Tools.randomDoubleBetween(1, 9);
        int exponent = Tools.randomIntBetween(smallestPower, biggestPower);
        double mass = mantissa * Math.pow(10, exponent);
        boolean isStar = exponent >= 31;
        System.out.println("In Class : " + isStar);
        Color primaryColor = Tools.randomColor();
        Color secondaryColor = Tools.randomColor();
        return new Body(0, 0, velocity.getX(), velocity.getY(), mass, name, primaryColor, secondaryColor, isStar, 0);
    }
}

class BodyInformation {
    private String name;
    private double mass;
    private double distanceToSun;
    private Color primaryColor;
    private Color secondaryColor;
    private boolean hasRing;
    private boolean isStar;

    public BodyInformation(String name, double mass, double distanceToSun, Color primaryColor, Color secondaryColor, boolean hasRing, boolean isStar) {
        setName(name);
        setRealMass(mass);
        setDistanceToSun(distanceToSun);
        setPrimaryColor(primaryColor);
        setSecondaryColor(secondaryColor);
        setHasRing(hasRing);
        setIsStar(isStar);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRealMass() {
        return mass;
    }

    public void setRealMass(double mass) {
        this.mass = mass;
    }

    public double getAdjustedMass() {
        return mass * Universe.massScaleFactor;
    }

    public double getDistanceToSun() {
        return distanceToSun;
    }

    public void setDistanceToSun(double distanceToSun) {
        this.distanceToSun = distanceToSun;
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

    public boolean hasRing() {
        return hasRing;
    }

    public void setHasRing(boolean hasRing) {
        this.hasRing = hasRing;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setIsStar(boolean star) {
        isStar = star;
    }
}
