package sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Possibles {

	private Integer[] nums = {1,2,3,4,5,6,7,8,9};
	private ArrayList<Integer> possibleNums = new ArrayList<>(Arrays.asList(nums));

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
	int remove(ArrayList<Integer> notAnswer){
		if(! possibleNums.equals(notAnswer)) {
			possibleNums.removeAll(notAnswer);
		}
		int check = checkAnswer();
		return check;
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

	ArrayList<Integer> get(){
		return possibleNums;
	}

	 int search(ArrayList<Integer> searcher){
		 for(Integer number: possibleNums) {
			 if(! searcher.contains(number)) {
				 return number;
			 }
		 }
		 return 0;
	}
}
