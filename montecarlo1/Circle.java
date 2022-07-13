import java.util.concurrent.ThreadLocalRandom;

/*
 * Represent a circle in a plane.
 */
public class Circle {
        // Center of the circle.
        private Point center;

        // Radius of the circle.
        private float radius;

        public Circle(float x, float y, float r)
        {
                this.center = new Point(x, y);
                this.radius = r;                
        }

        public Circle(Point p, float r)
        {
                this.center = p;
                this.radius = r;                
        }

        public Point center() { return center; }
        public float radius() { return radius; }

        // Return true if the point is inside the area delimited by
        // the circle.
        public boolean contains(Point p)
        {
                float dx = p.x() - center.x();
                float dy = p.y() - center.y();
                
                return dx*dx + dy*dy <= radius*radius;                
        }

        // Return a random circle whose center is distributed randomly
        // inside the rectangle with the given width and height and
        // whose origin is located at (0, 0).  Its radius is a random
        // value between 0 and "maxRadius".
        public static Circle random(float width,
                                    float height,
                                    float maxRadius)
        {
                return new Circle(
                        Point.random(0, width, 0, height),
                        maxRadius * ThreadLocalRandom.current().nextFloat());
        }
}
