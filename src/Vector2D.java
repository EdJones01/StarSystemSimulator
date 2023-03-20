import java.awt.*;
import java.util.Random;

public class Vector2D {
    private static final Random random = new Random();
    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this(0, 0);
    }

    public static Vector2D random(double xMin, double xMax, double yMin, double yMax) {
        return new Vector2D(xMin + random.nextDouble() * (xMax - xMin), yMin + random.nextDouble() * (yMax - yMin));
    }

    public static Vector2D random() {
        return Vector2D.random(-random.nextDouble(), random.nextDouble(), -random.nextDouble(), random.nextDouble());
    }

    public static Vector2D random(Vector2D min, Vector2D max) {
        return Vector2D.random(min.getX(), max.getX(), min.getY(), max.getY());
    }

    public static Vector2D random(Rectangle bounds) {
        return Vector2D.random(bounds.x, bounds.x + bounds.width, bounds.y, bounds.y + bounds.height);
    }

    public Vector2D copy() {
        return new Vector2D(x, y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D sub(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D mul(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D div(double scalar) {
        return new Vector2D(this.x / scalar, this.y / scalar);
    }

    public double mag() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D setMag(double mag) {
        if (mag > 0) {
            return normalize().mul(mag);
        }
        return new Vector2D();
    }

    public Vector2D normalize() {
        double mag = (double) mag();
        if (mag > 0) {
            return div(mag);
        }
        return new Vector2D(x, y);
    }

    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    public double distance(Vector2D other) {
        return other.sub(this).mag();
    }

    public Vector2D limit(double mag) {
        if (mag() > mag) {
            return setMag(mag);
        }
        return new Vector2D(x, y);
    }

    public double getTheta() {
        return Math.atan2(y, x);
    }

    public String toString() {
        return "Vector(x: " + x + " y: " + y + ")";
    }

    public String asRoundedString(int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return "Vector(x: " + Math.round(x * factor) / factor +
                " y: " + Math.round(y * factor) / factor + ")";
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}