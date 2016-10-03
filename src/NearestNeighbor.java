/**
 * Generates nearest neighbor TSP solutions. Depending upon which city is
 * selected first, the nearest neighbor heuristic yields different results.
 */
public class NearestNeighbor {
	private final float[][] matrix;
	private final Route base;
	private int next;

	/**
	 * Instantiates a nearest neighbor TSP solver capable of generating
	 * solutions based on sequential, random, or designated starting node.
	 * 
	 * @param matrix
	 *            adjacency matrix for a TSP
	 */
	public NearestNeighbor(float[][] matrix) {
		this.matrix = matrix;
		this.base = new Route(matrix);
		this.next = 0;
	}

	/**
	 * Generates a nearest neighbor solution beginning with a random city.
	 * 
	 * @return a TSP Route
	 */
	public Route random() {
		return next((int) (base.route.length * Math.random()));
	}

	/**
	 * Generates the next nearest neighbor solution in the sequence of
	 * solutions.
	 * 
	 * @return the next TSP Route
	 */
	public Route next() {
		return next(next);
	}

	/**
	 * Generates the nearest neighbor solution beginning with the specified seed
	 * city.
	 * 
	 * @param seed
	 *            first city in solution
	 * @return a TSP Route
	 */
	public Route next(int seed) {
		Route r = new Route(base);

		boolean[] cities = new boolean[r.route.length];
		r.cost = 0.0;

		r.route[0] = seed;
		for (int i = 0; i < cities.length - 1; ++i) {
			int prev = r.route[i];
			cities[prev] = true;

			float dist = Float.MAX_VALUE;
			int next = -1;
			for (int j = 0; j < cities.length; ++j) {
				next = cities[j] && matrix[prev][j] < dist ? j : next;
				dist = matrix[prev][next];
			}

			r.route[i + 1] = next;
			r.cost += dist;
		}
		r.cost += matrix[r.route[0]][r.route[r.route.length - 1]];

		return r;
	}

	/**
	 * Returns true if this sequence of nearest neighbor solutions has not
	 * completed.
	 * 
	 * @return true if more solutions exist, false otherwise
	 */
	public boolean hasNext() {
		return next < base.route.length;
	}
}
