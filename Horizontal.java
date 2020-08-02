package sudoku;

public class Horizontal {
	private int hor;
	private int squareId;
	Horizontal(int h) {
		this.hor = h;
		if(hor<=3) {
			this.squareId = 3;
		}
		if(4<=hor && hor<=6) {
			this.squareId = 5;
		}
		if(7<=hor) {
			this.squareId = 7;
		}
	}
	int getValue() {
		return this.hor;
	}
	public int getSquareId() {
		return squareId;
	}
	boolean equal(Horizontal hor2) {
		if(this.hor == hor2.getValue()) {
			return true;
		}
		return false;
	}
}
