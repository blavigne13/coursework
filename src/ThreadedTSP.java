import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedTSP {
  float[][] map; // adjacency matrix
  int duration;
  long startTime;
  long endTime;

  static PriorityQueue<HeuristicTSP> nnPQ;
  static PriorityQueue<HeuristicTSP> pq2opt;
  static PriorityQueue<HeuristicTSP> pq2all;
  static PriorityQueue<HeuristicTSP> pq3opt;
  static PriorityQueue<HeuristicTSP> pq3all;
  static PriorityQueue<HeuristicTSP> peakPQ;

  ThreadedTSP(float[][] map, int duration) {
    this.map = map;

    nnPQ = new PriorityQueue<HeuristicTSP>();
    pq2opt = new PriorityQueue<HeuristicTSP>();
    pq2all = new PriorityQueue<HeuristicTSP>();
    pq3opt = new PriorityQueue<HeuristicTSP>();
    pq3all = new PriorityQueue<HeuristicTSP>();
    peakPQ = new PriorityQueue<HeuristicTSP>();

    startTime = System.currentTimeMillis();
    endTime = startTime + duration * 1000;
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
    exec3all.shutdownNow();
    System.exit(0);
  }

  private int ecsToPQ(ExecutorCompletionService<HeuristicTSP> ecs,
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

  private int pqToEcs(PriorityQueue<HeuristicTSP> pq,
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
