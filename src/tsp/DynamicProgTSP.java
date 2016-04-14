package tsp;

import java.io.*;
import java.util.*;

/**
 * Brad LaVigne Algorithms, assignment 3.2
 * 
 * Traveling Salesman Problem (TSP) with dynamic programming
 */
public class DynamicProgTSP {
  static final boolean VERBOSE = false;
  static final int[] MASKS = {0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024,
      2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576,
      2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
      268435456, 536870912, 1073741824};
  float[][] map = null; // adjacency matrix
  float[][] known = null; // best known from 0 to i, using visited
  int[] bestRt = null; // best known route
  float bestLen = Float.MAX_VALUE; // length of best known route
  long cnt = 0; // visited nodes

  /**
   * Instantiate TSP from file. The salesman will find the optimal route by
   * which to visit each city, beginning and ending at city 0.
   * 
   * @param file file specifying distances between cities
   */
  DynamicProgTSP(String file) {
    read_file(file);
    known = new float[map.length - 1][0];
    known[0] = new float[MASKS[map.length - 1]];
    Arrays.fill(known[0], Float.MAX_VALUE);
    for (int i = 1; i < known.length; i++) {
      known[i] = Arrays.copyOf(known[0], known[0].length);
    }
  }

  /**
   * Instantiate TSP from provided adjacency matrix. The salesman will find the
   * optimal route by which to visit each city, beginning and ending at city 0.
   * 
   * @param map adjacency matrix of distances between cities
   */
  DynamicProgTSP(float[][] map) {
    this.map = map;
    known = new float[map.length - 1][0];
    known[0] = new float[MASKS[map.length - 1]];
    Arrays.fill(known[0], Float.MAX_VALUE);
    for (int i = 1; i < known.length; i++) {
      known[i] = Arrays.copyOf(known[0], known[0].length);
    }
  }

  /**
   * Invoking the run method causes Del Griffith to find the most efficient
   * route for selling shower curtain rings.
   */
  public void run() {
    long t = System.currentTimeMillis();
    find_rec(0, new int[map.length + 1], 0, 0.0f);
    t = System.currentTimeMillis() - t;
    print_stuff(t);
  }

  /**
   * Execute recursive DFS with dynamic-programming-based pruning. As each
   * successor is generated: If the cost of visiting the cities on the current
   * route is less than the known cost, known is updated. Otherwise, the current
   * route is pruned.
   * 
   * @param open cities not in current route
   * @param visited cities in current route
   * @param route current route as a stack
   * @param ptr stack pointer for current route
   * @param d distance of current route
   */
  void find_rec(int visited, int[] route, int ptr, float d) {
    cnt++;
    /* If route is complete, test against best route */
    if (ptr == map.length - 1) {
      if ((d += map[route[ptr]][0]) < bestLen) {
        bestLen = d;
        bestRt = route.clone();
      }
      return;
    }
    /* generate successors */
    for (int succ = 1; succ < map.length; succ++) {
      /* ensure succ not already visited */
      if ((visited & MASKS[succ]) != 0) {
        continue;
      }
      float newD = d + map[route[ptr]][succ];
      /* DFS pruning--just barely worth doing */
      if (newD > bestLen) {
        continue;
      }
      /* Check known for better routes using current combination */
      // use succ's bit to store city 1's visited status then shift right
      int v = (visited ^ ((visited & 1) != 0 ? MASKS[succ] : 0)) >>> 1;
      float knownD = known[succ - 1][v];
      if (newD < knownD) {
        known[succ - 1][v] = newD;
      } else {
        continue;
      }
      route[++ptr] = succ;
      find_rec(visited ^ MASKS[succ], route, ptr, newD);
      route[ptr--] = 0;
    }
  }

  /**
   * Instruct Del to report his findings.
   * 
   * @param t time to find best route
   */
  void print_stuff(long t) {
    System.out.printf("size:\t%,d cities\n", map.length);
    System.out.printf("route:\t%s\n", Arrays.toString(bestRt));
    System.out.printf("cost:\t%,.3f\n", bestLen);
    System.out.printf("time:\t%,d ms\n\n", t);
    if (VERBOSE) {
      Runtime rt = Runtime.getRuntime();
      long xmem = rt.maxMemory() >> 20;
      long tmem = rt.totalMemory() >> 20;
      long umem = (rt.freeMemory() - tmem) >> 20;
      float mem =
          (0.125f * Float.SIZE * (map.length - 1) * MASKS[map.length - 1])
              / (1 << 20);
      System.out.printf("\tvisited nodes:\t%,d\n", cnt);
      System.out.printf("\ttable alloc:\t%,.3f MB\n", mem);
      System.out.printf("\truntime:\tused / avail / max\n");
      System.out.printf("\tmem (MB):\t%,d / %,d / %,d\n", umem, tmem, xmem);
      verify();
      System.out.printf("---------------------------------------\n");
    }
  }

  /**
   * Utility method to check against known best cost, up to 22.
   */
  void verify() {
    int n = map.length;
    int[] cees = {0, 0, 0, 0, 83562, 83870, 86074, 94981, 97651, 107691, 123499,
        125179, 133126, 146619, 168980, 192698, 229431, 229535, 230051, 231018,
        233288, 241460, 244747};
    if (n < cees.length && (int) (bestLen * 10) != cees[n]) {
      System.out.println("*************");
      System.out.println("Fail with n=" + n);
      System.out.println((int) (bestLen * 10) + " != " + cees[n]);
      System.out.println("*************");
    }
  }

  /**
   * Utility method to read input into a 2d array.
   * 
   * @param file input
   * @return 2d array of input as float
   */
  void read_file(String file) {
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(file));
      String[] tokens = in.readLine().split("\\s+");

      int n = tokens.length;
      map = new float[n][n];
      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          map[i][j] = map[j][i] = Float.parseFloat(tokens[j - i]);
        }
        tokens = in.readLine().split("\\s+");
      }
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String args[]) {
    if (args.length != 2) {
      new DynamicProgTSP("input2.txt").run();
    } else {
      int n = Integer.parseInt(args[1]);
      for (int i = Integer.parseInt(args[0]); i <= n; i++) {
        new DynamicProgTSP("input" + i + ".txt").run();
      }
    }
  }
}
