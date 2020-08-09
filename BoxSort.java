package sudoku;

import java.util.Comparator;

/**
 * マスを、配置できる数が少ない順に並べ替えます。
 * 配置できる数がない（＝解答済みまたは仮解答中）マスは最後尾に並べます。
 */
public class BoxSort implements Comparator<Box> {

	public int compare(Box box1, Box box2) {

		int counter1 = box1.getPossibles().count();
		int counter2 = box2.getPossibles().count();

		if(counter1 == 0) {
			counter1 = 10;
		}
		if(counter2 == 0) {
			counter2 = 10;
		}
		if(counter1 > counter2) {
			return 1;
		}
		if(counter1 < counter2) {
			return -1;
		}
		return 0;
	}
}

/**
 * マスを、行番号・列番号順に並べ替えます。
 */
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