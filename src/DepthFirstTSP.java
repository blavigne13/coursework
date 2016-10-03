import java.util.*;

/**
 * Find optimal solution to traveling salesman problem (TSP) using depth-first
 * search.
 * 
 * @author Brad LaVigne
 */
public class DepthFirstTSP {
  static final boolean LATEX = false; // format output for LaTeX?

  double[][] map = null; // adjacency matrix
  ArrayDeque<Integer> bestRt = null; // best known route
  double bestLen = Double.MAX_VALUE; // length of best route

  DepthFirstTSP(double[][] map) {
    this.map = map;
  }

  void run() {
    long t = System.currentTimeMillis();
    ArrayDeque<Integer> route = new ArrayDeque<Integer>();

    route.push(0); // initialize route with start node
    getRoute_rec(route, 0.0);
    bestRt.push(bestRt.pollLast()); // move start node to head

    t = System.currentTimeMillis() - t;
    if (LATEX) {
      System.out.printf("%d & %,.2f & %s & %,dms \\\\ \\hline\n", map.length,
          bestLen, bestRt, t);
    } else {
      System.out.println("Cities: " + map.length + "\tLength: " + bestLen
          + "\ttime: " + t + "ms\nRoute: " + bestRt);
    }
  }

  void getRoute_rec(ArrayDeque<Integer> r, double d) {
    if (r.size() == map.length) {
      if ((d += map[r.peek()][0]) < bestLen) {
        bestLen = d;
        bestRt = r.clone();
      }
      return;
    }
    for (int i = 1; i < map.length; i++) {
      double newD = d + map[r.peek()][i];
      if (newD < bestLen && !r.contains(i)) {
        r.push(i);
        getRoute_rec(r, newD);
        r.pop();
      }
    }
  }
}
