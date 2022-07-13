import java.util.concurrent.ThreadLocalRandom;

/*
 * Represents a point in a plane.
 */
public class Point {
        // Abscissa of the point.
        private float x;

        // Ordinate of the point.
        private float y;
        
        // Is the point inside the area we want to compute?
        private boolean isInside;

        public Point(float x, float y) 
        {
                this.x = x;
                this.y = y;                
                this.isInside = false;                
        }

        public float x() { return x; }
        public float y() { return y; }

        // Mark the point as inside the area we want to compute.
        public void setInside() { isInside = true; }

        // Is the point inside the area we want to compute?
        public boolean isInside() { return isInside; }

        // Return a random float between min (included) and max (excluded).
        private static float rand(float min, float max)
        {
                return min +
                        (max - min) * ThreadLocalRandom.current().nextFloat();
        }
        // Return a random point whose abscissa is between minx
        // (included) and maxx (excluded) and ordinate is between miny
        // (included) and maxy (excluded).
        public static Point random(float minX,
                                   float maxX,
                                   float minY,
                                   float maxY)
        {
                return new Point(rand(minX, maxX), rand(minY, maxY));
        }
}
