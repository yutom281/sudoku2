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
	 * マスごとに解答を実行します。
	 * 実行前にログを作成します。
	 */
	void run() {
		backlog = createLog(field);
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
	boolean isSolved() {
		for(Box box: field) {
			if(box.getAnswer() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * すべてのマスの行列インデックス、解答、縦横ブロックの相互関係を複製し、配置できる数を初期化します。
	 */
	 static ArrayList<Box> createLog(ArrayList<Box> boxList) {
		ArrayList<Box> log = new ArrayList<>();
		boxList.forEach(box -> {
			Box backBox = box.clone();
			log.add(backBox);
		});
		log.forEach(backBox -> backBox.init(log));
		return log;
	}

	/**
	 * 現在の解き方からの移行を判断します。
	 * 解答できなくなるか、解答が完了している場合に、移行します。
	 *
	 * @return {@code true} 前回から解答が変化していない場合。
	 */
	boolean isStuck() {
		ArrayList<Integer> currentAnswers = new ArrayList<>();
		ArrayList<Integer> logAnswers = new ArrayList<>();

		field.forEach(box -> currentAnswers.add(box.getAnswer()));
		if(backlog != null) {
			backlog.forEach(backBox -> logAnswers.add(backBox.getAnswer()));
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
	void inspect(boolean IO) throws InputException{
		ArrayList<Box> flaw = new ArrayList<>();
		for(Box box:field) {
			flaw.addAll(box.inspect());
		}
		if(flaw.size() > 0) {
			if(IO == true) {
				input.outputInspection(flaw);
			}
			throw new InputException("問題に誤りがあります。");
		}
	}

	/**
	 * （デバッグ）解答状況をコンソール出力します。
	 */
	void print(ArrayList<Box> boxList) {
		String answers = "\n -----------------------------------\n";
		for(int i = 0; i < 81; i++) {
			if(i%9 == 0) {
				answers += "| ";
			}
			Box box = boxList.get(i);
			if(box.getAnswer() != 0) {
				answers += (box.getAnswer()+" | ");
			} else {
				answers += "  | ";
			}
			if((i+1)%9 == 0) {
				answers += "\n -----------------------------------\n";
			}
		}
		System.out.println(answers);
	}
}
