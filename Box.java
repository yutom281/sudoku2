package sudoku;

import java.util.ArrayList;

public class Box implements Cloneable{

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
	void remove(ArrayList<Integer> notAnswerList) {
		possibles.remove(notAnswerList);
	}

	/**
	 * 配置できる数を取り除きます。
	 */
	void remove(int notAnswer) {
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

		try {
			fsolver.inspect(false);
		} catch (InputException ie) {
			answer = 0;
			return "contradicted";
		}

		while(! fsolver.isStuck()) {
			fsolver.run();
		}

		if(fsolver.isSolved()) {
			return "solved";
		}

		if(fsolver.isContradicted()) {
			answer = 0;
			return "contradicted";
		}

		return "stopped";

	 }

	 /**
	  * 解答を取り消し、自身と周囲の配置できる数を再計算します。
	  * すでに取り消されている場合、再計算は行わず、戻り値のみを返します。
	  *
	  * @return rbAnswer 取り消した解答
	  */
	 int rollback() {
		if(answer != 0) {
			rbAnswer = answer;
			answer = 0;
			recalc();
			areaHorizontal.recalc();
			areaVertical.recalc();
			areaSquare.recalc();
		}
		return rbAnswer;
	 }

	 /**
	  * 配置できる数を再計算します。
	  */
	 void recalc() {
		possibles.recalc(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
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
	  * 自身の縦横ブロック内に配置できる数の合計個数を調べます。
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
	  * 自身の縦横ブロック内で探索を実行可能なマスを返します。
	  * 「実行可能」とは、「配置できる数が2個以上存在する」ことを指します。
	  */
	 ArrayList<Box> findSolvableBox() {
		 ArrayList<Box> solvables = new ArrayList<>();
		 solvables.addAll(areaHorizontal.findSolvableBox());
		 solvables.addAll(areaVertical.findSolvableBox());
		 solvables.addAll(areaSquare.findSolvableBox());
		 return solvables;
	 }

	 /**
	  * 自身の縦横ブロック内で検索条件に一致するマスを返します。
	  *
	  * @param answer 解答の数
	  */
	 ArrayList<Box> getAreaBox(int answer) {
		 return getAreaBox(answer, "AND");
	 }

	 /**
	  * 自身の縦横ブロック内で検索条件に一致するマスを返します。
	  *
	  * @param answer 解答の数
	  * @param param 第一引数との一致または不一致
	  */
	 ArrayList<Box> getAreaBox(int answer, String param) {
		 ArrayList<Box> results = new ArrayList<>();
		 if(param.equals("NOT")) {
			 results.add(areaHorizontal.getBox(answer, param));
			 results.add(areaVertical.getBox(answer, param));
			 results.add(areaSquare.getBox(answer, param));
		 }
		 if(param.equals("AND")) {
			 results.add(areaHorizontal.getBox(answer, true));
			 results.add(areaVertical.getBox(answer, true));
			 results.add(areaSquare.getBox(answer, true));
		 }
		 return results;
	}

	/**
	 * 自身の複製を作成します。
	 * 探索時、前の分岐点に戻るため（バックトラック）に使用します。
	 * もし rollback されている場合、取り消した解答も取得します。
	 */
	public Box clone(){
		Box clone = new Box(this.hor, this.vert, this.answer);
		if(this.rbAnswer != 0) {
			clone.rbAnswer = this.rbAnswer;
		}
		return clone;
	}

	/**
	 * ログに記録されている自身の情報をコピーします。
	 * もとのインスタンスが維持されます。
	 */
    void copy(Box logInfo) {
    	if(this.hor == logInfo.hor && this.vert == logInfo.vert) {
			this.answer = logInfo.answer;
			this.rbAnswer = logInfo.rbAnswer;
    	}
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
