/**
 * Representation of a TSP city on an x, y coordinate grid.
 */
class City {
	float x;
	float y;

	/**
	 * Instantiate a city at location x, y.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	City(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Instantiate a city at location x, y as parsed from the supplied input
	 * String. Input string should contain 3 values of the form
	 * "int float float" separated by whitespace. Note, the integer value
	 * (formerly a city-unique id) is not used, but still required.
	 * 
	 * @param s
	 *            space separated values
	 */
	City(String s) {
		String[] t = s.split("\\s+");
		this.x = Float.parseFloat(t[1]);
		this.y = Float.parseFloat(t[2]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
