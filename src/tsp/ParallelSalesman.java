/*
 * Brad LaVigne Shoddy parallelization
 */
package tsp;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParallelSalesman {
  double[][] map = null;// distances from i to j
  ArrayDeque<Integer> bestR = null; // best complete route
  double bestD = Double.MAX_VALUE;// total distance of best route

  ParallelSalesman(double[][] map) {
    this.map = map;
  }

  void run() throws InterruptedException {
    long t = System.currentTimeMillis();
    getRoute();
    t = System.currentTimeMillis() - t;

    bestR.push(bestR.pollLast()); // move start node to head
    System.out.printf("%d & %,.2f & %s & %,dms \\\\ \\hline\n", map.length,
        bestD, bestR, t);
  }

  void getRoute() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(3);
    for (int i = 1; i < map.length; i++) {
      Runnable worker = new Para(i);
      executor.execute(worker);
    }
    executor.shutdown();
    while (!executor.isTerminated()) {
    }
  }

  class Para implements Runnable {
    ArrayDeque<Integer> r;

    Para(int id) {
      r = new ArrayDeque<Integer>();
      r.push(0);
      r.push(id);
    }

    public void run() {
      getRoute_rec(map[0][r.peek()]);
    }

    void getRoute_rec(double d) {
      if (r.size() == map.length) {
        double newD = d + map[r.peek()][0];
        synchronized (this) {
          if (newD < bestD) {
            ArrayDeque<Integer> tmp = r.clone();
            bestD = newD;
            bestR = tmp;
          }
        }
        return;
      }
      for (int i = 1; i < map.length; i++) {
        double newD = d + map[r.peek()][i];
        if (newD < bestD && !r.contains(i)) {
          r.push(i);
          getRoute_rec(newD);
          r.pop();
        }
      }
    }
  }
}
