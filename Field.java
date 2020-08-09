package sudoku;

import java.util.ArrayList;

public class Field {

	protected ArrayList<Box> field;
	protected ArrayList<Box> backlog;

	/**
	 * コンストラクタ。
	 * 問題を読み込み、全マスの Boxインスタンスを生成してFieldに格納します。
	 */
	Field() {
		IOStream input = new IOStream();
		this.field = input.get();
	}

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
	 * すべてのマスを複製します。
	 * 前回の計算結果を保存して、解き方を変更するか判断します。
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
		new IOStream(field);
	}

	/**
	 * （デバッグ用）
	 * 解答状況と、マスごとに配置できる数の一覧をExcelに出力します。
	 */
	void debug() {
		IOStream.debug(field);
	}
}
