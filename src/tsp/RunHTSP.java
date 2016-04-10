package tsp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Brad LaVigne
 */
public class RunHTSP {
  static final String IN_FILE = "tsp.txt";
  // static final String LIB_FILE = "bcl380.txt"; // 1621
  // static final String LIB_FILE = "dka1376.txt"; // 4666
  // static final String LIB_FILE = "dbj2924.txt"; // 10128
  static final String LIB_FILE = "lsb22777.txt"; // 60977

  static final boolean TSP_LIB = true;
  static final boolean RND = false;
  static final boolean WRITE = false;
  static final String OUT_FILE = "x380.txt";
  static final int N = 5000;
  static final long TIME = 150000;// 895013;

  static PriorityQueue<HeuristicTSP> nnPQ;
  static PriorityQueue<HeuristicTSP> pq2opt;
  static PriorityQueue<HeuristicTSP> pq2all;
  static PriorityQueue<HeuristicTSP> pq3opt;
  static PriorityQueue<HeuristicTSP> pq3all;
  static PriorityQueue<HeuristicTSP> peakPQ;

  static float[][] map;
  static long startTime;
  static long endTime;

  public static void main(String[] args) {
    RunHTSP del = new RunHTSP(IN_FILE);
    del.run();
  }

  RunHTSP(String file) {
    startTime = System.currentTimeMillis();
    endTime = startTime + TIME;

    nnPQ = new PriorityQueue<HeuristicTSP>();
    pq2opt = new PriorityQueue<HeuristicTSP>();
    pq2all = new PriorityQueue<HeuristicTSP>();
    pq3opt = new PriorityQueue<HeuristicTSP>();
    pq3all = new PriorityQueue<HeuristicTSP>();
    peakPQ = new PriorityQueue<HeuristicTSP>();

    if (RND) {
      map = mapGen(N);
    } else if (TSP_LIB) {
      map = mapGen(readTspLib(LIB_FILE));
    } else {
      readFile(file);
    }
    if (WRITE) {
      writeFile(OUT_FILE);
    }
    // System.out.println("Size: " + map.length);
  }

  void run() {
    ExecutorService execNN = Executors.newFixedThreadPool(1);
    ExecutorService exec2opt = Executors.newFixedThreadPool(2);
    ExecutorService exec2all = Executors.newFixedThreadPool(2);
    ExecutorService exec3opt = Executors.newFixedThreadPool(1);
    ExecutorService exec3all = Executors.newFixedThreadPool(1);

    ExecutorCompletionService<HeuristicTSP> nnEcs =
        new ExecutorCompletionService<HeuristicTSP>(execNN);
    ExecutorCompletionService<HeuristicTSP> ecs2opt =
        new ExecutorCompletionService<HeuristicTSP>(exec2opt);
    ExecutorCompletionService<HeuristicTSP> ecs2all =
        new ExecutorCompletionService<HeuristicTSP>(exec2all);
    ExecutorCompletionService<HeuristicTSP> ecs3opt =
        new ExecutorCompletionService<HeuristicTSP>(exec3opt);
    ExecutorCompletionService<HeuristicTSP> ecs3all =
        new ExecutorCompletionService<HeuristicTSP>(exec3all);

    int cntNN = 0;
    int cnt3all = 0;
    int cnt3opt = 0;
    int cnt2all = 0;
    int cnt2opt = 0;
    HeuristicTSP r = new HeuristicTSP(map);

    while (System.currentTimeMillis() < endTime) {
      // generate random NN starts
      while (cntNN < 39) {
        nnEcs.submit(new HeuristicTSP(
            (int) (System.currentTimeMillis() % (1 + cntNN << 4)), r));
        ++cntNN;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }

      cntNN = ecsToPQ(nnEcs, nnPQ, cntNN);
      cnt2all = pqToEcs(nnPQ, ecs2all, cnt2all, 22);
      cntNN = ecsToPQ(nnEcs, nnPQ, cntNN);
      cnt2opt = pqToEcs(nnPQ, ecs2opt, cnt2opt, 21);

      cnt2all = ecsToPQ(ecs2all, pq3all, cnt2all);
      cnt2opt = ecsToPQ(ecs2opt, pq3opt, cnt2opt);

      cnt3all = pqToEcs(pq3all, ecs3all, cnt3all, 30);
      cnt3opt = pqToEcs(pq3opt, ecs3opt, cnt3opt, 30);

      cnt3opt = ecsToPQ(ecs3opt, pq2all, cnt3opt);
      cnt3all = ecsToPQ(ecs3all, pq2opt, cnt3all);

      report(peakPQ.peek());
    }
    
    printRoute(peakPQ.peek());
    report(peakPQ.peek());
    System.out.println(peakPQ.peek());

    execNN.shutdownNow();
    exec2opt.shutdownNow();
    exec2all.shutdownNow();
    exec3opt.shutdownNow();
    System.exit(0);
  }

  int ecsToPQ(ExecutorCompletionService<HeuristicTSP> ecs,
      PriorityQueue<HeuristicTSP> pq, int n) {
    Future<HeuristicTSP> f = null;
    while ((f = ecs.poll()) != null) {
      try {
        HeuristicTSP r1 = f.get();
        if (r1.mode == 99) {
          peakPQ.offer(r1);
        } else if (peakPQ.isEmpty()) {
          peakPQ.offer(r1);
          pq.offer(r1);
        } else if (r1.cost <= peakPQ.peek().cost) {
          peakPQ.offer(r1);
          pq.offer(r1);
        } else {
          pq.offer(r1);
        }
        --n;
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
    return n;
  }

  int pqToEcs(PriorityQueue<HeuristicTSP> pq,
      ExecutorCompletionService<HeuristicTSP> ecs, int n, int mode) {
    while (n < 7 && !pq.isEmpty()) {
      pq.peek().mode = mode;
      ecs.submit(pq.poll());
      ++n;
    }
    return n;
  }

  void printRoute(HeuristicTSP r) {
    int zero = -1;
    for (int i = 0; i < r.route.length; ++i) {
      if (r.route[i] == 0) {
        zero = i;
      }
      if (zero >= 0) {
        System.out.print(r.route[i] + " ");
      }
    }
    for (int i = 0; i < zero; ++i) {
      System.out.print(r.route[i] + " ");
    }
    System.out.println("0");
  }

  void readFile(String file) {
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

  void writeFile(String file) {
    try (PrintWriter out = new PrintWriter(file);) {
      for (int i = 0; i < map.length; i++) {
        out.write(i + " ");
        for (int j = i + 1; j < map.length; j++) {
          out.write(map[i][j] + " ");
        }
        out.write("\n");
      }
    } catch (FileNotFoundException e) {
      System.err.println("Unable to open output file: " + file);
    }
  }

  List<City> readTspLib(String file) {
    List<City> cities = new ArrayList<City>();
    try (BufferedReader in = new BufferedReader(new FileReader(file));) {
      while (in.ready()) {
        String[] tokens = in.readLine().split("\\s+");
        cities.add(new City(Integer.parseInt(tokens[0]),
            Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return cities;
  }

  class City {
    int id = 0;
    float x = 0.0f;
    float y = 0.0f;

    City(int id, float x, float y) {
      this.id = id;
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return id + " " + x + "," + y;
    }
  }

  List<City> cityGen(int n) {
    List<City> cities = new ArrayList<City>(n);
    cities.add(new City(0, 0.0f, 0.0f));

    for (int i = 1; i < n; ++i) {
      float x = (float) (i * 1000 + 1000.0 * Math.random());
      float y = (float) (n * 1000.0 * Math.random());
      cities.add(new City(i, x, y));
    }
    return cities;
  }

  float[][] mapGen(int n) {
    return mapGen(cityGen(n));
  }

  float[][] mapGen(List<City> cities) {
    int n = cities.size();
    float[][] map = new float[n][n];

    for (int i = 0; i < n - 0; i++) {
      for (int j = i + 1; j < n; j++) {
        float deltaX = Math.abs(cities.get(i).x - cities.get(j).x);
        float deltaY = Math.abs(cities.get(i).y - cities.get(j).y);
        map[i][j] =
            map[j][i] = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      }
    }
    return map;
  }

  void rndMapGen(int n, boolean write) {
    map = new float[n][n];

    for (int i = 0; i < n - 0; i++) {
      for (int j = i + 1; j < n; j++) {
        map[i][j] = map[j][i] = (float) (Math.random() * 100000);
      }
    }
  }

  void report(HeuristicTSP r) {
    if (r == null) {
      System.out.println(r);
      return;
    }
    System.out.printf(r.id + "\ncost: %,.2f\t%,.2f\t\n", r.cost, r.eval());
    System.out.printf("Time: %,d ms\n", System.currentTimeMillis() - startTime);
    r.dupe();
  }
}
