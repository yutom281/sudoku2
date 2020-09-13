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

/**
 * 空白のマスを、解答後に同じ縦横ブロック内に配置できる数の合計個数が多い順
 * (解答したときに、ほかのマスの解答を絞る力が小さい順)に並べ替えます。
 * 解答済みのマスは最後尾に並べます。
 */
class PossiblesSort implements Comparator<Box> {

	public int compare(Box box1, Box box2) {

		int pCount1 = 0;
		int pCount2 = 0;

		if(box1.getAnswer() == 0) {
			box1.rollforward();
			pCount1 = box1.countAreaPossibles();
			box1.rollback();
		}

		if(box2.getAnswer() == 0) {
			box2.rollforward();
			pCount2 = box2.countAreaPossibles();
			box2.rollback();
		}

		if(pCount1 > pCount2) {
			return -1;
		}
		if(pCount1 < pCount2) {
			return 1;
		}
		return 0;

	}
}