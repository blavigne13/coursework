public class TwoOpt {
	static float[][] map;

	int[] route;
	double cost;

	public TwoOpt(int[] route) {
		this.route = route;
	}

	public void twoOptBest() { // make best 2-opt move
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
			twoOpt((bestI - 1 + route.length) % route.length, bestI, bestJ - 1, bestJ);
		}
	}

	public void twoOptGreedy() { // greedy 2-opt
		for (int i = 0; i < route.length; ++i) {
			for (int j = i + 2; j < route.length; ++j) {
				if (twoCost((i - 1 + route.length) % route.length, i, j - 1, j) < 0.0) {
					twoOpt(i - 1, i, j - 1, j);
					return;
				}
			}
		}
	}

	public void twoOptAll() { // makes all improving 2-opt moves in one pass
		for (int i = 0; i < route.length; ++i) {
			for (int j = i + 2; j < route.length; ++j) {
				if (twoCost((i - 1 + route.length) % route.length, i, j - 1, j) < cost) {
					twoOpt((i - 1 + route.length) % route.length, i, j - 1, j);
				}
			}
		}
	}

	private double twoCost(int a, int b, int c, int d) {
		double newCost = cost;
		newCost -= map[route[a]][route[b]];
		newCost -= map[route[c]][route[d]];
		newCost += map[route[a]][route[c]];
		newCost += map[route[b]][route[d]];

		return newCost;
	}

	private void twoOpt(int a, int b, int c, int d) {
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
}
