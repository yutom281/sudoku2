package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class Field {
	private ArrayList<Box> field;
	private ArrayList<Box> backlog;


	/**
	 * コンストラクタ。
	 * 問題を読み込み、全マスの Boxインスタンスを生成してFieldに格納します。
	 */
	Field() {
		IOStream input = new IOStream();
		this.field = input.get();
	}

	/**
	 * コンストラクタ。探索のバックトラックに使用します。
	 */
	Field(ArrayList<Box> backup){
		field = backup;
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
	 */
	void run() {
		field.forEach(box -> box.calc());
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
	 * すべてのマスのコピーを作成します。
	 * 前回の計算結果を保存して、解き方を変更するかの判断に使用します。
	 */
	void backlog() {
		this.backlog = backlog(this.field);
	}

	/**
	 * すべてのマスのコピーを作成します。
	 * 解き方変更のほか、探索アルゴリズムでも使用します。
	 */
	ArrayList<Box> backlog(ArrayList<Box> boxList) {
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
		backlog.forEach(logbox -> logAnswers.add(logbox.getAnswer()));

		String currAns = currentAnswers.toString();
		String logAns = logAnswers.toString();

		if(currAns.equals(logAns)) {
			return true;
		}
		return false;
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 */
	void solver() {
		solver(this.field, this.backlog);
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 * 管理インスタンスと実行インスタンスを設定する。
	 * 実行側はbacklogから選択する
	 *
	 * 管理側は実行側（Box）の配置できる数と現在の仮解答、さらに仮解答前の状態（backlog）を把握する
	 * 実行側は仮解答にもとづき、Areaに属する20マスを更新し、解答を継続する。
	 * このとき、実行側はAreaの20マスを複製して解答する。復元するときはこの複製を削除する。
	 * １）仮解答が誤りだった場合、★fieldから「実行者を除いた」backlogを復元する。
	 *     → fieldとbacklogが同期している段階で同時にソートする。fieldからindex=0を取り除き、backlogのindex=0にsolverを実行させる。
	 *     その後、管理側は配置できる数から仮解答を削除し、次の仮解答に進む
	 * ２）再び継続不可となった場合、Fieldを管理者として再度solverを実行する（backlogはインスタンス変数のListに保存し、復元時はインデックスの新しい順に取り出す）
	 *
	 * 	＊管理者がFieldの場合とBoxの場合とで、実行者選択のロジックを分けて記述する。
	 *    →  Boxの場合では、上記１）field を area x3 に読み替えて実装する。両方ともArrayList<Box>なので、共通ロジックを流用可能
	 */
	void solver(ArrayList<Box> data, ArrayList<Box> backup) {

		Collections.sort(data, new BoxSort());
		Collections.sort(backup, new BoxSort());

		ArrayList<Box> deepBackup;

		Box rootBox = backup.get(0).copy();
		rootBox.init(backup);
		Possibles rootPossibles = rootBox.getPossibles();

		for(int possibleNum: rootPossibles.get()) {

			String message = rootBox.solver(backup, possibleNum);

			if(message.equals("solved")) {
				data.clear();
				data.addAll(backup);
				Collections.sort(data, new IndexSort());
				break;
			}
			if(message.equals("contradicted")) {
				backup.clear();
				backup.addAll(data);
				rootBox.replace(backup);
				rootBox.init(backup);
				continue;
			}
			// １回目のBox.solver()が継続不能となった場合、それまでのログを記録し、「別の実行者」を選択して２回目のsolver()を実行する
			// 別の実行者を全体から選ぶ→ｎ回目の実行者候補から n-1 回目までの実行者を除く（各バックアップは最後まで残す必要がある）
			// 別の実行者を現在の実行者のAreaから選ぶ→実行者候補から自身を除く処理は簡単になる。実行者選択は非効率になる。結局、仮解答の影響は全体に及ぶため、他に特別なメリットはない？
			// rootBoxは仮解答状態のため、Possibles = null とすれば、sort() により自動的に次の実行者候補から外れる
			if(message.equals("stopped")) {
				deepBackup = backlog(backup);
				rootBox.replace(deepBackup);
				solver(backup, deepBackup);
			}
		}
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
