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

	// "rb" stands for "rollbacked"
	private int rbAnswer;

	/**
	 * コンストラクタ。行・列インデックス、ブロック識別番号、ある場合は解答を設定します。
	 * 解答を設定した場合、配置できる数をすべて取り除きます。
	 */
	Box(Horizontal hor, Vertical vert, int initNumber){
		this.hor = hor;
		this.vert = vert;
		this.square = hor.getSquareId() * vert.getSquareId();
		if(initNumber != 0) {
			this.answer = initNumber;
			possibles = new Possibles();
		}
	}

	/**
	 * 問題がゲームのルールに違反している場合、例外を返します。
	 */
	ArrayList<Box> inspect() {
		ArrayList<Box> flaw = new ArrayList<>();
		if(answer != 0) {
			flaw.addAll(areaHorizontal.inspect(answer));
			flaw.addAll(areaVertical.inspect(answer));
			flaw.addAll(areaSquare.inspect(answer));
		}
		if(flaw.size() > 0) {
			flaw.add(this);
		}
		return flaw;
	}

	/**
	 * 同じ縦・横・ブロックにあるマスと紐づけます。
	 * それらに置かれている数を取得し、自身に配置できる数を計算・初期化します。
	 */
	void init(ArrayList<Box> field) {

		this.areaHorizontal = new Area(field, this, hor);
		this.areaVertical = new Area(field, this, vert);
		this.areaSquare = new Area(field, this, square);
		if(answer == 0) {
			this.possibles = new Possibles(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
		}
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

	/**
	 * 配置できる数を取り除きます。
	 */
	void remove(ArrayList<Integer> notAnswer) {
		possibles.remove(notAnswer);
	}

	/**
	 * 自身の解答を、同じ縦横ブロックにあるマスの配置できる数から取り除きます。
	 * また、自身に配置できる数をリセットします。
	 */
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

		// Fieldクラス、FieldSolverクラスのメソッドを使用する
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
	  * 解答を取り消し、配置できる数を計算します。
	  * その後、配置できる数から取り消した解答の数を取り除きます。
	  * さらに、同じ縦横ブロックに配置できる数を再計算します。
	  */
	 void rollback() {
		if(answer != 0) {
			rbAnswer = answer;
			answer = 0;

		}
		recalc();
		areaHorizontal.recalc();
		areaVertical.recalc();
		areaSquare.recalc();
	 }

	 /**
	  * 配置できる数から rollback() により取り消した解答の数を取り除きます。
	  */
	 void recalc() {
		 if(answer == 0 && rbAnswer != 0) {
			 possibles = new Possibles(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
			 possibles.remove(rbAnswer);
		 }
	 }

	 /**
	  * rollback() により取り消した解答を元に戻します。
	  * その後、その解答を同じ縦横ブロックの配置できる数から取り除きます。
	  */
	 void rollforward() {
		 if(answer == 0 && rbAnswer != 0) {
			 answer = rbAnswer;
			 rbAnswer = 0;
			 update();
		 }
	 }

	 /**
	  * 自身と同じ縦横ブロック内に配置できる数の合計個数を調べます。
	  * 自身に配置できる数は含みません。
	  */
	 int countAreaPossibles() {
		 int pCount = 0;
		 pCount += areaHorizontal.countPossibles();
		 pCount += areaVertical.countPossibles();
		 pCount += areaSquare.countPossibles();
		 return pCount;
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
