package sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Area {

	private ArrayList<Box> area = new ArrayList<>();
	private ArrayList<Integer> numbers = new ArrayList<>();

	Area(ArrayList<Box> field, Box caller, Horizontal hor){
		field.forEach(box -> {
			Horizontal boxHor = box.getHorizontal();
			if(hor.equal(boxHor) && (! box.equals(caller))) {
				area.add(box);
				numbers.add(box.getAnswer());
			}
		});
	}

	Area(ArrayList<Box> field, Box caller, Vertical vert){
		field.forEach(box -> {
			Vertical boxVert = box.getVertical();
			if(vert.equal(boxVert) && (! box.equals(caller))) {
				area.add(box);
				numbers.add(box.getAnswer());
			}
		});
	}
	Area(ArrayList<Box> field, Box caller, int square){
		field.forEach(box -> {
			if(square == box.getSquare() && (! box.equals(caller))) {
				area.add(box);
				numbers.add(box.getAnswer());
			}
		});
	}

	ArrayList<Integer> getNumbers() {
		return this.numbers;
	}

	// N国同盟（ダブル数字）を計算する
	ArrayList<Integer> calc() {
		Map<String, Integer> possiblesMap = new HashMap<>();

		for(Box box: area) {
			if(box.getAnswer() != 0) {
				continue;
			}
			// マップに、配置できる数の組（文字列）をキー、同じ組をもつマスの個数を値として記録する
			Possibles possibles = box.getPossibles();
			String strPossibles = possibles.toString();
			Integer count = possiblesMap.putIfAbsent(strPossibles, 1);
			if(count != null){
				possiblesMap.put(strPossibles, count + 1);
			}
		}
		// 組の要素数＝その組をもつマス数 なら、組を呼び出し元の Box に返し、配置できる数から取り除く。
		ArrayList<Integer> notAnswer = new ArrayList<>();
		for(Map.Entry<String, Integer> entry: possiblesMap.entrySet()) {
			String strPos = entry.getKey();
			Integer count = entry.getValue();
			if(strPos.length() == count) {
				notAnswer.addAll(toPossibles(strPos));
			}
		}
		return notAnswer;

	}

	// Area 内の9マスにおいて、うち1マスにしか置けない数がないか探す
	int search(Possibles possibles) {
		ArrayList<Integer> searcher = new ArrayList<>();
		for(Box box: area) {
			searcher.addAll(box.getPossibles().get());
		}
		return possibles.search(searcher);
	}

	void update(int answer) {
		ArrayList<Integer> notAnswer = new ArrayList<>();
		notAnswer.add(answer);
		for(Box box: area) {
			box.remove(notAnswer);
		}
	}

	ArrayList<Integer> toPossibles(String strPossibles){
		ArrayList<Integer> possibleNums = new ArrayList<>();
		for(String str: strPossibles.split("")) {
			possibleNums.add(Integer.parseInt(str));
		}
		return possibleNums;
	}

	/**
	 * 問題がゲームのルールに違反している場合、例外を返します。
	 *
	 * throws InputException 縦／横／ブロック内に同じ数字が２つ以上配置されている場合。
	 */
	ArrayList<Box> inspect(int answer) {

		ArrayList<Box> flaw = new ArrayList<>();

		for(Box box: area) {
			int initNumber = box.getAnswer();
			if(initNumber == answer) {
				flaw.add(box);
			}
		}
		return flaw;
	}
}