package sudoku;

import java.util.Comparator;

public class BoxSort implements Comparator<Box>{

	public int compare(Box box1, Box box2) {

		int possiblesCount1 = box1.getPossibles().get().size();
		int possiblesCount2 = box2.getPossibles().get().size();

		if(possiblesCount1 == 0) {
			return 10;
		}
		if(possiblesCount2 == 0) {
			return -10;
		}
		return possiblesCount1 - possiblesCount2;
	}
}