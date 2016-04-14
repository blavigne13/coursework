package tsp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brad LaVigne
 */
public class RunTSP {
  private static final String[] FILES = {//
      "tsp.txt",//
      "bcl380.txt", // 1621
      "dka1376.txt", // 4666
      "dbj2924.txt", // 10128
      "lsb22777.txt" // 60977
  };
  private static final int FILE = 2;
  private static final int N = 5000;

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
        cities.add(new City(in.readLine().split("\\s+")));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return getMap(cities);
  }

  private static float[][] getMap(List<City> cities) {
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

  private static List<City> cityGen(int n) {
    List<City> cities = new ArrayList<City>(n);
    cities.add(new City(0, 0.0f, 0.0f));

    for (int i = 1; i < n; ++i) {
      float x = (float) (i * 1000 + 1000.0 * Math.random());
      float y = (float) (n * 1000.0 * Math.random());
      cities.add(new City(i, x, y));
    }
    return cities;
  }

  private static float[][] randomMap(int n) {
    return getMap(cityGen(n));
  }
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

  City(String[] tokens) {
    this.id = Integer.parseInt(tokens[0]);
    this.x = Float.parseFloat(tokens[1]);
    this.y = Float.parseFloat(tokens[2]);
  }

  @Override
  public String toString() {
    return id + " " + x + "," + y;
  }
}
