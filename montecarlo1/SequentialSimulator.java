import java.lang.Math;
import java.util.Iterator;

public class SequentialSimulator implements Iterable<Point>, Simulator {
        private Experiment exp;

        public SequentialSimulator(Circle[] circles, int numCycles, float size)
        {
                this.exp = new Experiment(circles, numCycles, 0, size, size);
        }

        public SequentialSimulator(int numCircles, int numCycles, float size)
        {
                this.exp = new Experiment(numCircles, numCycles, 0, size, size);
        }

        public void run() { exp.run(); }

        public double currentEstim()
        {
                return (double)exp.numInside()/exp.currentCycle() *
                        exp.width() * exp.height();
        }

        @Override
        public Iterator<Point> iterator() { return exp.iterator(); }

        public float size() { return exp.width(); }
        public int numCycles() { return exp.numCycles(); }
        public int currentCycle() { return exp.currentCycle(); }
        public Circle[] circles() { return exp.circles(); }

        public static void main(String[] args)
        {
                Circle[] circles = { new Circle(1, 1, 1) };

                SequentialSimulator s = new SequentialSimulator(
                        circles, 2000000, 2);
                s.run();
                System.out.println("Pi area: " + s.currentEstim());
        }
}
