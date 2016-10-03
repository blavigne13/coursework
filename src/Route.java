import java.util.Arrays;

public class Route {
	int[] route;
	double cost;

	public Route(float[][] matrix) {
		// initialize to sequential tour
		this.route = new int[matrix.length];
		this.route[0] = 0;
		this.cost += matrix[0][matrix.length - 1];
		for (int i = 1; i < matrix.length; ++i) {
			this.route[i] = i;
			this.cost += matrix[i - 1][i];
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param r
	 *            the route to copy
	 */
	public Route(Route r) {
		this.route = Arrays.copyOf(r.route, r.route.length);
		this.cost = r.cost;
	}

	// may switch to Arrays.spliterator(array) in future
	/**
	 * Reverse the ordering of the route over the closed interval from b through
	 * c: [b, c]
	 * 
	 * @param b
	 * @param c
	 * @param matrix
	 */
	public void reverse(int b, int c, float[][] matrix) {
		int a = (route.length + b - 1) % route.length;
		int d = (c + 1) % route.length;

		cost -= matrix[a][b] + matrix[c][d];
		cost += matrix[a][c] + matrix[b][d];

		for (int i = 0; i < (c - b + 1) / 2; ++i) {
			route[b + i] ^= route[c - i];
			route[c - i] ^= route[b + i];
			route[b + i] ^= route[c - i];
		}
	}
}
