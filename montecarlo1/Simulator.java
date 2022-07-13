import java.util.Iterator;

// Represents a simulator that computes the area delimited by a set of
// circles.  In order to do this, we generate random points inside a
// square field of a given size.  We then count the number of those
// that falls within the circle.
public interface Simulator {
        // Return the size of the square field where we generate 
        float size();

        // Run the simulation.
        void run();

        // Return an iterator to the points that was generated randomly.
        Iterator<Point> iterator();

        double currentEstim();

        int numCycles();

        int currentCycle();

        Circle[] circles();        
}
