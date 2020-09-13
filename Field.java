package sudoku;

import java.util.ArrayList;

public class Field {

	protected IOStream input;
	protected ArrayList<Box> field;
	protected ArrayList<Box> backlog;

	/**
	 * コンストラクタ。
	 * 問題を読み込み、全マスの Boxインスタンスを生成してFieldに格納します。
	 */
	Field(IOStream input) {
		this.input = input;
		this.field = input.get();
	}

	Field(){}

	/**
	 * 全マスの Area, Possiblesインスタンスを生成します。
	 * 各マスを、同じ行／列／ブロックにある8マス（延べ24マス）と紐づけます。
	 * また、開始時に置かれている数をもとに、配置できる数を計算・初期化します。
	 */
	void init() {
		field.forEach(box -> box.init(field));
	}

	/**
	 * マスごとに計算を実行します。
	 * 実行前にログを作成します。
	 */
	void run() {

		backlog = backlog(field);
		//field.forEach(box -> box.calc());
		for(Box box: field) {
			box.calc();
		}
	}

	/**
	 * 解答の完了を判断します。
	 *
	 * @return {@code true} 未解答のマスがある場合。
	 */
	boolean check() {
		for(Box box: field) {
			if(box.getAnswer() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * すべてのマスの行列インデックス、解答、縦横ブロックの相互関係を複製し、配置できる数を初期化します。
	 * 探索でも使用します。
	 */
	 static ArrayList<Box> backlog(ArrayList<Box> boxList) {
		ArrayList<Box> log = new ArrayList<>();
		boxList.forEach(box -> {
			Box logBox = box.copy();
			log.add(logBox);
		});
		log.forEach(logBox -> logBox.init(log));
		return log;
	}

	/**
	 * 現在の解き方からの移行を判断します。
	 * 解答できなくなるか、解答が完了している場合に、次のブロックへ移行します。
	 *
	 * @return {@code true} 前回から解答が変化していない場合。
	 */
	boolean changeMode() {

		ArrayList<Integer> currentAnswers = new ArrayList<>();
		ArrayList<Integer> logAnswers = new ArrayList<>();

		field.forEach(box -> currentAnswers.add(box.getAnswer()));

		if(backlog != null) {
			backlog.forEach(logbox -> logAnswers.add(logbox.getAnswer()));
		}

		String currAns = currentAnswers.toString();
		String logAns = logAnswers.toString();

		if(currAns.equals(logAns)) {
			return true;
		}
		return false;
	}

	/**
	 * 探索を起動します。
	 */
	void solver() {
		FieldSolver fsolver = new FieldSolver(field);
		fsolver.solver();
	}

	/**
	 * 解答結果をExcelに出力します。
	 */
	void output() {
		input.output();
	}

	/**
	 * 問題がゲームのルールに違反している場合、例外を返します。
	 */
	void inspect() throws InputException{

		ArrayList<Box> flaw = new ArrayList<>();

		for(Box box:field) {
			flaw.addAll(box.inspect());
		}
		if(flaw.size() > 0) {
			input.outputInspection(flaw);
			throw new InputException("問題に誤りがあります。");
		}
	}

	/**
	 * 二つのフィールドインスタンスを比較します。
	 *
	 * @return [@code true] 同じ行番号・列番号を持つマスの解答がすべて等しい場合
	 */
	boolean equals(Field anotherField) {

		for(int index = 0; index < 81; index++) {

			Box selfBox = getBox(index);
			Box otherBox = anotherField.getBox(index);

			int selfAnswer = selfBox.getAnswer();
			int otherAnswer = otherBox.getAnswer();

			if(selfAnswer != otherAnswer) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 指定した行番号・列番号のBoxインスタンスを返します。
	 *
	 * @param index this.fieldのインデックス
	 * @return 指定したBoxインスタンス
	 */
	Box getBox(Horizontal hor, Vertical vert) {

		int index = 0;
		for(Box box: field) {
			if(box.getHorizontal().equal(hor) && box.getVertical().equal(vert)) {
				break;
			}
			index++;
		}
		// プログラムが正常なら、この処理には到達しない
		//Box dummy = new Box(new Horizontal(0), new Vertical(0), 0);
		return getBox(index);
	}

	Box getBox(int index) {

		return field.get(index);
	}

	/**
	 * （デバッグ用）
	 * 解答状況と、マスごとに配置できる数の一覧をExcelに出力します。
	 */
	void debug() {
		input.debug();
	}

	void print() {
		ArrayList<Integer> currentAnswers = new ArrayList<>();
		field.forEach(box -> currentAnswers.add(box.getAnswer()));
		String currAns = currentAnswers.toString();
		System.out.println(currAns);
		System.out.println(currentAnswers.size());
	}
}
