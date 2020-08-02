package sudoku;

public class Vertical {
	private int vert;
	private int squareId;
	Vertical(int v) {
		this.vert = v;
		if(vert<=3) {
			this.squareId = 1;
		}
		if(4<=vert && vert<=6) {
			this.squareId = 2;
		}
		if(7<=vert) {
			this.squareId = 3;
		}
	}
	int getValue() {
		return this.vert;
	}
	public int getSquareId() {
		return squareId;
	}
	boolean equal(Vertical vert2) {
		if(this.vert == vert2.getValue()) {
			return true;
		}
		return false;
	}
}
