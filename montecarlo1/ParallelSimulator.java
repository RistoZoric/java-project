import java.lang.InterruptedException;
import java.lang.reflect.Array;

import java.util.Iterator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelSimulator implements Iterable<Point>, Simulator {
        private static final int threadNumber =
                Runtime.getRuntime().availableProcessors();

        private final Circle circles[];        

        private final float size;

        private final int numCycles;        

        private Experiment exp[];

        public ParallelSimulator(Circle[] circles, int numCycles, float size)
        {
                this.size = size;
                this.circles = circles;
                this.numCycles = numCycles;
                this.exp = new Experiment[threadNumber];                

                float width = size / threadNumber;
                int num = numCycles / threadNumber;
                float minX = 0;                
                for (int i = 0; i < threadNumber-1; i++) {
                        exp[i] = new Experiment(
                                circles, num, minX, width, size);
                        minX += width;                        
                }
                exp[threadNumber - 1] = new Experiment(
                        circles,
                        numCycles - num*(threadNumber - 1),
                        minX,
                        size - minX,
                        size);                
        }

        public ParallelSimulator(int numCircles, int numCycles, float size)
        {
                this(Experiment.randCircles(numCircles, size, size),
                     numCycles,
                     size);                
        }

        public float size() { return size; }

        public void run()
        {
                ExecutorService exec =
                        Executors.newFixedThreadPool(threadNumber);

                for (Experiment e: exp)
                        exec.submit(() -> e.run());

                exec.shutdown();
                try {
                        exec.awaitTermination(
                                Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                        throw new RuntimeException(e);                        
                }
        }

        public double currentEstim()
        {
                int num = 0;
                int total = 0;

                for (Experiment e: exp) {
                        num += e.numInside();
                        total += e.currentCycle();                        
                }

                return (double)num/total * size * size;                
        }

        @Override
        public Iterator<Point> iterator()
        {
                // This array is only used internally and is
                // immediately initialized afterwards to
                // Iterator<Point> elements.  So it's completely safe.
                @SuppressWarnings("unchecked")
                Iterator<Point> it[] = (Iterator<Point>[])
                        Array.newInstance(Iterator.class, threadNumber);
                for (int i = 0; i < it.length; i++)
                        it[i] = exp[i].iterator();

                return new Iterator<Point>() {
                        private int i = 0;

                        @Override
                        public boolean hasNext()
                        {
                                for (int j = 0; j < it.length; j++) {
                                        if (it[i].hasNext())
                                                return true;
                                        i = (i + 1) % it.length;
                                }
                                return false;                                
                        }

                        @Override
                        public Point next() { return it[i].next(); }
                };                
        }

        public int numCycles() { return numCycles; }

        public int currentCycle()
        {
                int num = 0;

                for (Experiment e: exp)
                        num += e.currentCycle();
                return num;                
        }

        public Circle[] circles() { return circles; }

        public static void main(String[] args)
        {
                Circle[] circles = { new Circle(1, 1, 1) };

                ParallelSimulator s = new ParallelSimulator(
                        circles, 2000000, 2);
                s.run();
                System.out.println("Pi area: " + s.currentEstim());
        }
}
