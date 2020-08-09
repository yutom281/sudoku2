package sudoku;

import java.util.ArrayList;

public class Box {

	private Horizontal hor;
	private Vertical vert;
	private int square;
	private int answer = 0;

	private Area areaHorizontal;
	private Area areaVertical;
	private Area areaSquare;

	private Possibles possibles;

	Box(Horizontal hor, Vertical vert, int initNumber){
		this.hor = hor;
		this.vert = vert;
		this.square = hor.getSquareId() * vert.getSquareId();
		if(initNumber != 0) {
			this.answer = initNumber;
			possibles = new Possibles();
		}
	}

	void init(ArrayList<Box> field) {
		if(answer == 0) {
			this.areaHorizontal = new Area(field, this, hor);
			this.areaVertical = new Area(field, this, vert);
			this.areaSquare = new Area(field, this, square);
			this.possibles = new Possibles(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
		}
	}

	void calc() {
		if(answer == 0) {
			remove(areaHorizontal.calc());
			remove(areaVertical.calc());
			remove(areaSquare.calc());

			search(areaHorizontal);
			search(areaVertical);
			search(areaSquare);
		}
	}

	// 「N国同盟（ダブル数字）」ロジックによりPossiblesから数を取り除く
	void remove(ArrayList<Integer> notAnswer) {
		int check = possibles.remove(notAnswer);
		if(check != 0) {
			this.answer = check;
			areaHorizontal.update(answer);
			areaVertical.update(answer);
			areaSquare.update(answer);
		}
	}

	// Possibles のうち、Area 内で自身しか持たない数を探す
	void search(Area area) {
		int result = area.search(possibles);
		if(result != 0) {
			this.answer = result;
			areaHorizontal.update(answer);
			areaVertical.update(answer);
			areaSquare.update(answer);
		}
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 *
	 * @param backup 探索する field の複製
	 * @param possibleNum 仮解答
	 *
	 * @return "solved" 解答が完了した場合; "contradicted" 矛盾が生じた場合; "stopped" 解答できなくなった場合
	 */

	 String solver(ArrayList<Box> backup, int possibleNum) {

		setTmpAnswer(possibleNum);
		areaHorizontal.update(possibleNum);
		areaVertical.update(possibleNum);
		areaSquare.update(possibleNum);

		/*
		 * Fieldクラス、FieldSolverクラスのメソッドを使用するため、
		 * backupを主体にFieldSolverインスタンスを生成する。
		 */
		FieldSolver fsolver = new FieldSolver(backup);

		while(! fsolver.changeMode()) {
			fsolver.run();
		}

		if(! fsolver.check()) {
			return "solved";
		}

		if(! fsolver.prove()) {
			return "contradicted";
		}

		return "stopped";

	 }

	 /**
	  * 探索において、仮解答を配置し、配置できる数を0にします。
	  *
	  * @param possibleNum 仮解答
	  */
	 final void setTmpAnswer(int possibleNum){

		 if(possibles.count() > 1) {
			 answer = possibleNum;
			 possibles = new Possibles();
		 }
	 }

	/**
	 * 自身のコピーを作成します。
	 * 探索時、前の分岐点に戻るため（バックトラック）に使用します。
	 */
	Box copy(){
		Box replica = new Box(this.hor, this.vert, this.answer);
		return replica;
	}

	/**
	 * Boxインスタンスを比較・判別します。
	 *
	 * @return {@code true} 行番号と列番号が同じ場合。
	 */
	boolean equals(Box box) {
		if(hor.equal(box.getHorizontal()) && vert.equal(box.getVertical())) {
			return true;
		}
		return false;
	}

	// Getter
	Horizontal getHorizontal() {
		return this.hor;
	}
	Vertical getVertical() {
		return this.vert;
	}
	int getSquare() {
		return this.square;
	}
	int getAnswer() {
		return this.answer;
	}
	Possibles getPossibles() {
		return this.possibles;
	}
}
