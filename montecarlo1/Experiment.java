import java.util.Iterator;
import java.util.stream.Stream;
import java.lang.Math;

public class Experiment implements Iterable<Point> {
        // The circles that delimit the area we want to compute.
        private Circle circles[];

        // Width of the field inside which the random points are
        // generated.
        private float width;

        // Height of the field inside which the random points are
        // generated.
        private float height;        

        // The abscissa of the points inside the field we run the
        // experiment on lies between minX and minX + width.
        private float minX;

        // The number of cycles we want to run.
        private final int numCycles;

        // The current cycle we're in.
        private int currentCycle;

        // Number of random points that falls inside the area we want
        // to compute.
        private int numInside;

        // The random points generated during the simulation.
        private Point points[];

        public Experiment(Circle[] circles,
                          int numCycles,
                          float minX,
                          float width,
                          float height)
        {
                this.numCycles = numCycles;
                this.minX = minX;
                this.width = width;
                this.height = height;
                this.points = new Point[numCycles];
                this.circles = Stream.of(circles)
                        .filter(c -> {
                                        float x = c.center().x();
                                        float r = c.radius();
                                        float maxX = minX + width;
                                        return (x >= minX && x <= maxX) ||
                                                (x < minX && x + r > minX) ||
                                                (x > maxX && x - r < maxX);
                                })
                        .toArray(Circle[]::new);                
        }

        // Return "num" random circles.
        //
        // The center of each circle lies in the rectangle of the
        // given width and height.
        public static Circle[] randCircles(int num, float width, float height)
        {
                Circle[] c = new Circle[num];
                
                // Here's how we compute the maximum radius "max" of
                // the circles.  Even if none of them overlap, we want
                // the total area to not exceed half the rectangle on
                // average.  Since the average area of a random circle
                // is pi * (max/2)^2.  Hence we want the inequality:
                //      num * pi * (max/2)^2 <= width * height / 2
                float max = (float)Math.sqrt(2*width*height/(Math.PI*num));
                for (int i = 0; i < num; i++)
                        c[i] = Circle.random(width, height, max);

                return c;                
        }

        public Experiment(int numCircles,
                          int numCycles,
                          float minX,
                          float width,
                          float height)
        {
                this(randCircles(numCircles, width, height),
                     numCycles,
                     minX,
                     width,
                     height);                
        }

        // Run the experiment.
        public void run()
        {
                numInside = 0;
                for (currentCycle = 0; currentCycle < numCycles; currentCycle++) {
                        Point p = Point.random(minX, minX + width, 0, height);
                        points[currentCycle] = p;                        
                        for (Circle c : circles)
                                if (c.contains(p)) {
                                        numInside++;
                                        p.setInside();
                                        break;
                                }
                }
        }

        @Override
        public Iterator<Point> iterator()
        {
                return new Iterator<Point>() {
                        private int i = 0;

                        @Override
                        public boolean hasNext() { return i < currentCycle; }

                        @Override
                        public Point next() { return points[i++]; }
                };                
        }

        public float width() { return width; }
        public float height() { return height; }
        public int numCycles() { return numCycles; }
        public int numInside() { return numInside; }
        public int currentCycle() { return currentCycle; }
        public Circle[] circles() { return circles; }
}
