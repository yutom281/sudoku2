package sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Possibles {

	static private Integer[] tmp = {1,2,3,4,5,6,7,8,9};
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

	/**
	 * ダブル数字により、配置できる数を絞り込みます。
	 * ダブル数字では、解答前に配置できる数がなくなることはありえないため、
	 * 配置できる数と取り除く数が完全に一致するときは処理を行いません。
	 */
	void remove(ArrayList<Integer> notAnswerList){
		if(! possibleNums.equals(notAnswerList)) {
			possibleNums.removeAll(notAnswerList);
		}
	}

	/**
	 * 配置できる数を絞り込みます。
	 * 探索では、「解答前に配置できる数がなくなること」をもって矛盾を判定するため、
	 * ダブル数字とは異なり、このメソッドによる上記の発生を許容しています。
	 */
	void remove(int notAnswer) {
		possibleNums.remove(Integer.valueOf(notAnswer));
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

	void recalc(ArrayList<Integer> numsH, ArrayList<Integer> numsV, ArrayList<Integer> numsSQ) {
		Integer[] tmp = {1,2,3,4,5,6,7,8,9};
		possibleNums = new ArrayList<>(Arrays.asList(tmp));
		possibleNums.removeAll(numsH);
		possibleNums.removeAll(numsV);
		possibleNums.removeAll(numsSQ);
		Collections.sort(possibleNums);
	}

}