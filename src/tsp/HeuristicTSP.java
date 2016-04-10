package tsp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Brad LaVigne
 */
class HeuristicTSP implements Callable<HeuristicTSP>, Comparable<HeuristicTSP> {
  static long startTime;
  static float[][] map;

  int id;
  double cost;
  int[] route;
  int mode;

  HeuristicTSP(float[][] map) {
    HeuristicTSP.map = map;
    this.id = -1;
    this.mode = -1;
    this.cost = 0.0f;
    this.route = new int[map.length];
    // sequential tour
    for (int i = 1; i < map.length; ++i) {
      route[i] = i;
      cost += map[i - 1][i];
    }
    // close the tour
    cost += map[route[0]][route[route.length - 1]];
  }

  HeuristicTSP(HeuristicTSP r) {
    this.id = r.id;
    this.mode = -1;
    this.cost = r.cost;
    this.route = Arrays.copyOf(r.route, r.route.length);
  }

  HeuristicTSP(int id, HeuristicTSP r) {
    this(r);
    this.id = id;
    mode = 1;
  }

  HeuristicTSP(HeuristicTSP r, int mode) {
    this.id = r.id;
    this.mode = mode;
    this.cost = r.cost;
    this.route = r.route;
  }

  @Override
  public HeuristicTSP call() {
    switch (mode) {
      case 1:
        nearestNeighbor((int) (Math.random() * map.length));
        break;
      default:
        run();
        break;
    }
    return this;
  }

  public void run() {
    double delta = 1.0;
    while (delta > 0.0) {
      delta = cost;
      switch (mode) {
        case 20:
          twoOptGreedy();
          break;
        case 21:
          twoOptBest();
          break;
        case 22:
          twoOptAll();
          break;
        case 30:
          threeOptGreedy();
          break;
        case 31:
          threeOptBest();
          break;

      }
      delta -= cost;

      if (mode >= 30) {
        if (delta >= 0.0) {
          if (Math.random() > 0.90) {
            mutate();
            mode = 21;
            continue;
          }
          mode = 99;
          return;
        }
        break;
      }
      // System.out.printf("\t%d) %d\t%,.2f\n", id, mode, cost);
    }
  }

  void mutate() {
    int n = (int) (13 * Math.random());
    int a = (int) (route.length * Math.random());
    int b = (int) (route.length * Math.random());
    int c = (int) (route.length * Math.random());

    for (int i = 0; i < n; ++i) {
      int tmp = route[a];
      route[a] = route[b];
      route[b] = route[c];
      route[c] = tmp;
    }
    cost = eval(); // inefficient

    // System.out.println(cost + " _-_ " + eval());
  }

  void twoOptBest() { // make best 2-opt move
    int bestI = -1;
    int bestJ = -1;
    double newC = Double.MAX_VALUE;
    double bestC = Double.MAX_VALUE;

    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 2; j < route.length; ++j) {
        newC = twoCost((i - 1 + route.length) % route.length, i, j - 1, j);
        if (newC < bestC) {
          bestI = i;
          bestJ = j;
          bestC = newC;
        }
      }
    }
    if (bestC < cost) {
      twoOpt((bestI - 1 + route.length) % route.length, bestI, bestJ - 1,
          bestJ);
    }
  }

  void twoOptGreedy() { // greedy 2-opt
    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 2; j < route.length; ++j) {
        if (twoCost((i - 1 + route.length) % route.length, i, j - 1, j) < 0.0) {
          twoOpt(i - 1, i, j - 1, j);
          return;
        }
      }
    }
  }

  void twoOptAll() { // makes all improving 2-opt moves
    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 2; j < route.length; ++j) {
        if (twoCost((i - 1 + route.length) % route.length, i, j - 1,
            j) < cost) {
          twoOpt((i - 1 + route.length) % route.length, i, j - 1, j);
        }
      }
    }
  }

  double twoCost(int a, int b, int c, int d) {
    double newC = cost;
    newC -= map[route[a]][route[b]];
    newC -= map[route[c]][route[d]];
    newC += map[route[a]][route[c]];
    newC += map[route[b]][route[d]];

    return newC;
  }

  void twoOpt(int a, int b, int c, int d) {
    // ..a,b..c,d.. => ..a,c..b,d..
    cost -= map[route[a]][route[b]];
    cost -= map[route[c]][route[d]];
    cost += map[route[a]][route[c]];
    cost += map[route[b]][route[d]];
    // swap [b..c] inclusive
    for (int i = 0; i < (d - b) / 2; ++i) {
      route[b + i] ^= route[c - i];
      route[c - i] ^= route[b + i];
      route[b + i] ^= route[c - i];
    }
  }

  void threeOptGreedy() {
    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 1; j < route.length; ++j) {
        for (int k = j + 2; k < route.length; ++k) {
          threeOpt((i - 1 + route.length) % route.length, i, j - 1, j, k - 1,
              k);
        }
      }
    }
  }

  void threeOptBest() {
    int bestI = -1;
    int bestJ = -1;
    int bestK = -1;
    double deltaC = Double.MAX_VALUE;
    double bestDC = 0.0;

    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 1; j < route.length; ++j) {
        for (int k = j + 2; k < route.length; ++k) {
          if ((deltaC = threeCost((i - 1 + route.length) % route.length, i,
              j - 1, j, k - 1, k)) < bestDC) {
            bestI = i;
            bestJ = j;
            bestK = k;
            bestDC = deltaC;
          }
        }
      }
    }
    if (bestI != -1) {
      threeOpt((bestI - 1 + route.length) % route.length, bestI, bestJ - 1,
          bestJ, bestK - 1, bestK);
    }
  }

  double threeCost(int a, int b, int c, int d, int e, int f) {
    // ..a,b..c,d..e,f.. => ..a,d..e,b..c,f..
    double costA = cost;
    costA -= map[route[a]][route[b]];
    costA -= map[route[c]][route[d]];
    costA -= map[route[e]][route[f]];
    costA += map[route[a]][route[d]];
    costA += map[route[e]][route[b]];
    costA += map[route[c]][route[f]];
    // ..a,b..c,d..e,f.. => ..a,d..e,c..b,f..
    double costB = cost;
    costB -= map[route[a]][route[b]];
    costB -= map[route[c]][route[d]];
    costB -= map[route[e]][route[f]];
    costB += map[route[a]][route[d]];
    costB += map[route[e]][route[c]];
    costB += map[route[b]][route[f]];

    return costA < costB ? costA : costB;
  }

  void threeOpt(int a, int b, int c, int d, int e, int f) {
    // ..a,b..c,d..e,f.. => ..a,d..e,b..c,f..
    double costA = cost;
    costA -= map[route[a]][route[b]];
    costA -= map[route[c]][route[d]];
    costA -= map[route[e]][route[f]];
    costA += map[route[a]][route[d]];
    costA += map[route[e]][route[b]];
    costA += map[route[c]][route[f]];
    // ..a,b..c,d..e,f.. => ..a,d..e,c..b,f..
    double costB = cost;
    costB -= map[route[a]][route[b]];
    costB -= map[route[c]][route[d]];
    costB -= map[route[e]][route[f]];
    costB += map[route[a]][route[d]];
    costB += map[route[e]][route[c]];
    costB += map[route[b]][route[f]];

    if (costA > cost && costB > cost) {
      return;
    }
    if (costA < costB) {
      cost = costA;
    } else {
      // reverse [b..c]
      twoOpt(a, b, c, d);
      cost = costB;
    }
    int[] tmp = Arrays.copyOfRange(route, b, d);// [b..c]d
    System.arraycopy(route, d, route, b, f - d);
    System.arraycopy(tmp, 0, route, b + f - d, tmp.length);
  }

  void nearestNeighbor(int seed) {
    Set<Integer> cities = new HashSet<Integer>(route.length * 2);
    for (int i = 0; i < route.length; ++i) {
      cities.add(route[i]);
      route[i] = -1;
    }
    cost = 0.0;
    route[0] = seed;
    for (int i = 0; i < route.length - 1; ++i) {
      int prev = route[i];
      cities.remove(prev);
      int next = -1;
      float dist = Float.MAX_VALUE;

      for (int c : cities) {
        next = map[prev][c] < dist ? c : next;
        dist = map[prev][next];
      }
      route[i + 1] = next;
      cost += dist;
    }
    // close tour
    cost += map[route[0]][route[route.length - 1]];
  }

  void dupe() {
    for (int i = 0; i < route.length; ++i) {
      for (int j = i + 1; j < route.length; ++j) {
        if (route[i] == route[j]) {
          System.out.println("DUPE: " + "[" + i + "],[" + j + "]==" + route[i]);
        }
      }
    }
  }

  double eval() {
    double cost = 0.0;
    int prev = route[route.length - 1];
    for (int curr : route) {
      cost += map[prev][curr];
      prev = curr;
    }
    return cost;
  }

  boolean equals(HeuristicTSP r) {
    if (cost != r.cost) {
      return false;
    }
    for (int i = 0; i < route.length; ++i) {
      if (route[i] != r.route[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int compareTo(HeuristicTSP r) {
    return cost < r.cost ? -1 : cost > r.cost ? 1 : 0;
  }

  @Override
  public String toString() {
    return cost + " " + Arrays.toString(route);
  }
}
