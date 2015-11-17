package ru.mydroid;

public class Coordinate {
	public int x;
	public int y;
	
	public boolean equals(Object obj) {
		if (obj instanceof Coordinate) {
			Coordinate p = (Coordinate) obj;
			return (this.x == p.x) && (this.y == p.y);
		}
		return false;
	}
}
