package sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Possibles {

	private Integer[] tmp = {1,2,3,4,5,6,7,8,9};
	private ArrayList<Integer> possibleNums = new ArrayList<>(Arrays.asList(tmp));

	// コンストラクタ。Box に配置できる数を計算する
	Possibles(ArrayList<Integer> numsH, ArrayList<Integer> numsV, ArrayList<Integer> numsSQ){
		possibleNums.removeAll(numsH);
		possibleNums.removeAll(numsV);
		possibleNums.removeAll(numsSQ);
		Collections.sort(possibleNums);
	}
	// コンストラクタ。最初から埋まっているマス用
	Possibles(){
		possibleNums.clear();
	}

	/* 引数を配置できる数から取り除く。配置する前に配置できる数がなくなることは
	 * ありえないので、両者が完全に一致するときは取り除かない
	 */
	void remove(ArrayList<Integer> notAnswer){
		if(! possibleNums.equals(notAnswer)) {
			possibleNums.removeAll(notAnswer);
		}
	}

	void remove(int notAnswer) {
		ArrayList<Integer> rapper = new ArrayList<>();
		rapper.add(notAnswer);
		//remove(rapper);
		possibleNums.removeAll(rapper);
	}

	int search(ArrayList<Integer> searcher){
		 for(int number: possibleNums) {
			 if(! searcher.contains(number)) {
				 return number;
			 }
		 }
		 return 0;
	}

	int checkAnswer(){
		if(possibleNums.size() == 1) {
			int answer = possibleNums.get(0);
			possibleNums.clear();
			return answer;
		}
		return 0;
	}

	public String toString(){
		String strPossibles = "";
		for(Integer number : possibleNums) {
			strPossibles += (number.toString());
		}
		return strPossibles;
	}

	int count() {
		return possibleNums.size();
	}

	ArrayList<Integer> getValues(){
		return possibleNums;
	}

	int get(int index) {
		return possibleNums.get(index);
	}

	boolean contains(int num) {

		if(possibleNums.contains(num)) {
			return true;
		}
		return false;
	}
}