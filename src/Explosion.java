import java.awt.*;
import java.util.Random;

public class Explosion {
    static final double HUGE = 5.0;
    static final double BIG = 3.0;
    static final double SMALL = 1.0;

    private Random random = new Random();
    private int maxParticles = 16;
    private int minParticles = 8;
    private Particle[] particles;
    private int alpha = 255;

    public Explosion(Vector2D position, double modifier) {
        particles = new Particle[random.nextInt((maxParticles - minParticles) + minParticles) * (int) modifier];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(position, Vector2D.random());
        }
    }

    public void update() {
        for (Particle p : particles)
            p.update();
        alpha -= 3;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, getAlpha()));
        for (Particle p : getParticles())
            g2.drawLine((int) p.getPosition().getX(), (int) p.getPosition().getY(),
                    (int) p.getPosition().getX(), (int) p.getPosition().getY());
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    public int getMinParticles() {
        return minParticles;
    }

    public void setMinParticles(int minParticles) {
        this.minParticles = minParticles;
    }

    public Particle[] getParticles() {
        return particles;
    }

    public void setParticles(Particle[] particles) {
        this.particles = particles;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}

