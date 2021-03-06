package sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Area {

	private ArrayList<Box> area = new ArrayList<>();

	Area(ArrayList<Box> field, Box caller, Horizontal hor){
		field.forEach(box -> {
			Horizontal boxHor = box.getHorizontal();
			if(hor.equal(boxHor) && (! box.equals(caller))) {
				area.add(box);
				//numbers.add(box.getAnswer());
			}
		});
	}

	Area(ArrayList<Box> field, Box caller, Vertical vert){
		field.forEach(box -> {
			Vertical boxVert = box.getVertical();
			if(vert.equal(boxVert) && (! box.equals(caller))) {
				area.add(box);
				//numbers.add(box.getAnswer());
			}
		});
	}
	Area(ArrayList<Box> field, Box caller, int square){
		field.forEach(box -> {
			if(square == box.getSquare() && (! box.equals(caller))) {
				area.add(box);
				//numbers.add(box.getAnswer());
			}
		});
	}

	ArrayList<Integer> getNumbers() {
		ArrayList<Integer> numbers = new ArrayList<>();
		area.forEach(box -> numbers.add(box.getAnswer()));
		return numbers;
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
			searcher.addAll(box.getPossibles().getValues());
		}
		return possibles.search(searcher);
	}

	void update(int answer) {
		for(Box box: area) {
			box.remove(answer);
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

	/**
	 * Box.rollback()に呼び出されます。
	 */
	void recalc() {
		for(Box box: area) {
			box.recalc();
		}
	}

	/**
	 * 配置できる数の合計個数を返します。
	 */
	int countPossibles() {
		int pCount = 0;
		for(Box box: area) {
			pCount += box.getPossibles().count();
		}
		return pCount;
	}

	Box getBox(int answer, String param) {
		Box box = area.get((int)(Math.random()*7.99));
		if(box.getAnswer() != 0 && box.getAnswer() != answer) {
			return box;
		}
		return getBox(answer, param);
	}

	Box getBox(int index) {
		return getBox(index, false);
	}

	Box getBox(int param, boolean searchByAnswer) {
		if(searchByAnswer) {
			for(Box box: area) {
				if(box.getAnswer() == param) {
					return box;
				}
			}
		}
		return null;
	}

	ArrayList<Box> findSolvableBox() {
		ArrayList<Box> solvables = new ArrayList<>();
		for(Box box: area) {
			if(box.getPossibles().count() > 1) {
				solvables.add(box);
			}
		}
		return solvables;
	}
}