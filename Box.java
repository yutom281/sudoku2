package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class Box {

	private Horizontal hor;
	private Vertical vert;
	private int square;
	private int answer = 0;

	private Area areaHorizontal;
	private Area areaVertical;
	private Area areaSquare;

	private Possibles possibles;

	// デバッグ
	ArrayList<Box> field;

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

		this.areaHorizontal = new Area(field, this, hor);
		this.areaVertical = new Area(field, this, vert);
		this.areaSquare = new Area(field, this, square);
		if(answer == 0) {
			this.possibles = new Possibles(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
		}

		// デバッグ
		this.field = field;
	}

	/*
	 * 解答を計算します。
	 * 配置できる数を絞り込み、残りひとつになるか、縦／横／ブロック内に自身にしか置けない数があるとき、
	 * その数を解答します。
	 */
	void calc() {
		if(answer == 0) {
			remove(areaHorizontal.calc());
			remove(areaVertical.calc());
			remove(areaSquare.calc());
			answer = possibles.checkAnswer();
		}
		if(answer == 0) {
			answer = areaHorizontal.search(possibles);
		}

		if(answer == 0) {
			answer = areaVertical.search(possibles);
		}

		if(answer == 0) {
			answer = areaSquare.search(possibles);
		}
		update();
	}

	void remove(ArrayList<Integer> notAnswer) {
		possibles.remove(notAnswer);
	}
	void check() {
		answer = possibles.checkAnswer();
	}
	void update() {
		if(answer != 0) {
			areaHorizontal.update(answer);
			areaVertical.update(answer);
			areaSquare.update(answer);
			possibles = new Possibles();
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

		answer = possibleNum;
		update();

		/*
		 * Fieldクラス、FieldSolverクラスのメソッドを使用するため、
		 * backupを引数にFieldSolverインスタンスを生成する。
		 */
		FieldSolver fsolver = new FieldSolver(backup);

		while(! fsolver.changeMode()) {
			fsolver.run();
		}

		if(! fsolver.check()) {
			return "solved";
		}

		if(! fsolver.prove()) {
			answer = 0;
			return "contradicted";
		}

		return "stopped";

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

	// デバッグ
	void solverDebug (ArrayList<Box> log) {
		ArrayList<Box> loglog = Field.backlog(log);
		Collections.sort(loglog, new IndexSort());
		IOStream.solverDebug(loglog);
	}
}
