package sudoku;

import java.util.Comparator;

public class BoxSort implements Comparator<Box> {

	public int compare(Box box1, Box box2) {

		int counter1 = box1.getPossibles().count();
		int counter2 = box2.getPossibles().count();

		if(counter1 == 0) {
			return 10;
		}
		if(counter2 == 0) {
			return -10;
		}
		return counter1 - counter2;
	}
}
class IndexSort implements Comparator<Box> {

	public int compare(Box box1, Box box2) {

		int hor1 = box1.getHorizontal().getValue();
		int hor2 = box2.getHorizontal().getValue();
		int vert1 = box1.getVertical().getValue();
		int vert2 = box2.getVertical().getValue();

		if(hor1 > hor2) {
			return 1;
		}
		if(hor1 < hor2) {
			return -1;
		}
		if(vert1 > vert2) {
			return 1;
		}
		if(vert1 < vert2) {
			return -1;
		}
		return 0;
	}
}