public class Particle {
    private Vector2D position;
    private Vector2D velocity;

    public Particle(Vector2D position, Vector2D velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    void update() {
        position = position.add(velocity);
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }
}