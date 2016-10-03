import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brad LaVigne
 */
public class RunTSP {
	private static final String[] FILES = { //
			"input/tsp.txt", //
			"input/bcl380.txt", // 1621
			"input/dka1376.txt", // 4666
			"input/dbj2924.txt", // 10128
			"input/lsb22777.txt" // 60977
	};
	private static final int FILE = 2;
	private static final int N = 5000; // number of cities in random map

	private static float[][] map; // adjacency matrix

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		if (FILE == 0) {
			map = readMap(FILES[FILE]);
		} else if (FILE > 0) {
			map = readTspLib(FILES[FILE]);
		} else if (FILE < 0) {
			map = randomMap(N);
		} else {
			System.out.println("File number " + FILE + " not defined.");
			return;
		}
		System.out.println("Size: " + map.length);

		new ThreadedTSP(map, 60).run();
	}

	private static float[][] readMap(String file) {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String[] tokens = in.readLine().split("\\s+");
			int n = tokens.length;
			float[][] map = new float[n][n];

			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					map[i][j] = map[j][i] = Float.parseFloat(tokens[j - i]);
				}
				tokens = in.readLine().split("\\s+");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	private static float[][] readTspLib(String file) {
		List<City> cities = new ArrayList<City>();
		try (BufferedReader in = new BufferedReader(new FileReader(file));) {
			while (in.ready()) {
				cities.add(new City(in.readLine()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toMatrix(cities);
	}

	private static float[][] toMatrix(List<City> cities) {
		int n = cities.size();
		float[][] map = new float[n][n];

		for (int i = 0; i < n - 0; i++) {
			for (int j = i + 1; j < n; j++) {
				float deltaX = Math.abs(cities.get(i).x - cities.get(j).x);
				float deltaY = Math.abs(cities.get(i).y - cities.get(j).y);
				map[i][j] = map[j][i] = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			}
		}
		return map;
	}

	private static float[][] randomMap(int n) {
		return randomMap(n, 1.0);
	}

	private static float[][] randomMap(int n, double exp) {
		List<City> cities = new ArrayList<City>(n);
		cities.add(new City(0.0f, 0.0f));

		for (int i = 1; i < n; ++i) {
			float x = (float) (n * 100.0 * Math.pow(Math.random(), exp));
			float y = (float) (n * 100.0 * Math.pow(Math.random(), exp));
			cities.add(new City(x, y));
		}

		return toMatrix(cities);
	}
}
