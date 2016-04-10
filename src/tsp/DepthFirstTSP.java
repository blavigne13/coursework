/* Brad LaVigne
 * Accidentally optimized a little*/
package tsp;
import java.util.*;

public class DepthFirstTSP {
   double[][] map = null;// distances from i to j
   ArrayDeque<Integer> bestR = null; // best complete route
   double bestD = Double.MAX_VALUE;// total distance of best route

   DepthFirstTSP(double[][] map) {
      this.map = map;
      ArrayDeque<Integer> r = new ArrayDeque<Integer>();

      long t = System.currentTimeMillis();
      r.push(0); // initial state
      getRoute_rec(r, 0.0);
      t = System.currentTimeMillis() - t;

      bestR.push(bestR.pollLast()); // move start node to head
      System.out.printf("%d & %,.2f & %s & %,dms \\\\ \\hline\n", map.length,
            bestD, bestR, t);
   }

   void getRoute_rec(ArrayDeque<Integer> r, double d) {
      if (r.size() == map.length) {
         if ((d += map[r.peek()][0]) < bestD) {
            bestD = d;
            bestR = r.clone();
         }
         return;
      }
      for (int i = 1; i < map.length; i++) {
         double newD = d + map[r.peek()][i];
         if (newD < bestD && !r.contains(i)) {
            r.push(i);
            getRoute_rec(r, newD);
            r.pop();
         }
      }
   }
}
