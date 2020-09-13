package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class Factory extends FieldSolver{

	static ArrayList<Box> blankField = new ArrayList<>();
	//ArrayList<Box> rollbackList = new ArrayList<>();

	/**
	 * コンストラクタ。空のBoxインスタンスとフィールドを作成します。
	 *
	 * threshold の案２：多重探索の深さで難易度を決定する
	 * easy = 0, normal = 2~3, hard = 4^5, impossible = limit_reached
	 */
	public Factory(int threshold) {

		super(blankField);

		for(int column = 1; column < 10; column++) {
			for(int row = 1; row < 10; row++) {
				Box box = new Box(new Horizontal(column), new Vertical(row), 0);
				blankField.add(box);
			}
		}

		// 下準備
		init();
		backlog = backlog(field);

		// 空のフィールドを埋める
		fill(field, backlog);
		IOStream.debug(field);

		// 解答を限界まで取り除く
		reverseSolver();
		IOStream.debug(field);

		// 指定された難易度に合わせて、解答を元に戻す
		adjust(threshold);
		System.out.println("配置できる数の個数合計：" + countTotalPossibles(field));

	}

	ArrayList<Box> get(){
		return field;
	}

	/**
	 * 白紙の全マスを解答済みの状態にします。
	 * 解答はランダムに決定します。
	 *
	 * @param field 全Boxインスタンス
	 * @param backlog 第一引数のコピー
	 *
	 * @return 解答済みの全Boxインスタンス
	 */
	String fill(ArrayList<Box> field, ArrayList<Box> backlog) {

		while(check()) {

			Box rndBox = backlog.get((int)(80*Math.random()));
			if(rndBox.getAnswer() != 0) {
				continue;
			}
			Possibles possibles = rndBox.getPossibles();
			int rndAnswer = possibles.get((int)(Math.random()*(possibles.count()-1)));

			String message = rndBox.solver(backlog, rndAnswer);

			if(message.equals("contradicted")) {
				backlog.clear();
				backlog.addAll(backlog(field));
				continue;
			}

			if(message.equals("stopped")) {
				ArrayList<Box> deepBacklog = backlog(backlog);

				String deepMessage = fill(backlog, deepBacklog);

				if(deepMessage.equals("solved")) {
					message = message.replace(message, deepMessage);
				}
			}

			if(message.equals("solved")) {
				field.clear();
				field.addAll(backlog);
				return message;
			}
		}
		return "contradicted";

	}

	/**
	 * 配置できる数の個数の合計を返します。
	 */
	static int countTotalPossibles(ArrayList<Box> boxList) {

		int totalPossibles = 0;
		for(Box box: boxList) {
			totalPossibles += box.getPossibles().count();
		}
		return totalPossibles;
	}

	/**
	 * 解答が1パターンのみの場合に、配置済みの数を減らし、解答パターンが増えるかを調べます。
	 * 数を取り除いたマスにそれ以外の数を配置し、解答(solver)に成功すれば、
	 * 解答パターンが2つ以上存在することになり、問題として成立しないと判断します。
	 * これを全マスについて繰り返し、取り除けるマスがなくなったら処理を終了します。
	 */
	void reverseSolver() {

		int rbCount = 0;
		while(rbCount < 50) {

			// バックアップのマスをランダムに選び、解答を仮除去する
			int rndIndex = (int)(80*Math.random());
			Box rndBox = backlog.get(rndIndex);
			if(rndBox.getAnswer() == 0) {
				continue;
			}
			rndBox.rollback();

			// そのマスにほかの数が置けなければ、仮除去を確定し、本体に反映する
			Possibles rndPossibles = rndBox.getPossibles();
			if(rndPossibles.count() == 0) {
				Box equivBox = field.get(rndIndex);
				equivBox.rollback();
				rbCount++;
				continue;
			}

			String message = "";
			// ほかの数が置ける場合、それらを仮解答として解答が成功するか試す
			for(int testNum: rndPossibles.getValues()) {

				message = message.replace(message, rndBox.solver(backlog, testNum));

				if(message.equals("contradicted")) {
					backlog = backlog(field);
					replace(backlog, rndBox);
					rndBox.rollback();
					//backlog.forEach(logBox -> logBox.recalc());
					continue;
				}

				if(message.equals("stopped")) {
					ArrayList<Box> deepBacklog = backlog(backlog);
					replace(deepBacklog, rndBox);
					rndBox.recalc();

					String deepMessage = solver(backlog, deepBacklog);

					if(deepMessage.equals("solved")) {
						message = message.replace(message, deepMessage);
					}
				}
				// 解答に成功した場合、バックアップの仮除去を差し戻す
				if(message.equals("solved")) {
					rndBox.rollforward();
					break;
				}
			}
			// すべての仮解答が失敗したら、仮除去を確定し、本体に反映する
			if(message.equals("contradicted")) {
				Box equivBox = field.get(rndIndex);
				equivBox.rollback();
				rbCount++;
			}
		}
		init();
	}

	/**
	 * 指定された難易度に合わせて、埋まっているマスを増やします。
	 *
	 * @param threshold 難易度を測る基準値
	 */
	void adjust(int threshold) {

		while(countTotalPossibles(field) > threshold) {

			Collections.sort(field, new PossiblesSort());
			Box fwBox = field.get(0);
			fwBox.rollforward();
		}

		Collections.sort(field, new IndexSort());
		init();
	}

	/**
	 * ロジック
	 * 解答済みのマスをランダムに選ぶ
	 * ★解答を0にして、配置できる数を再計算する
	 * ★areaの配置できる数を再計算する
	 * ★全対象マスの配置できる数からもともとの解答を取り除く
	 * ★をすべての対象マスについて繰り返し、かつ、元の解答を取り除いておく
	 *
	 * ※解答済みのマスのpossiblesに影響してはならない
	 * ※ほかのマスの更新に自身のpossiblesが影響されてはならない
	 */
	static void rollback(ArrayList<Box> boxList, ArrayList<Box> rollbackList) {

		for(Box box: boxList) {
			for(Box rollBox: rollbackList) {
				if(box.equals(rollBox)) {
					box.rollback();
				}
			}
		}
	}

	/**
	 *
	 */
/*	boolean checkIfExist(ArrayList<Box> backup, ArrayList<ArrayList<Box>> fieldList) {

		ArrayList<Integer> answersB = new ArrayList<>();
		backup.forEach(box -> answersB.add(box.getAnswer()));
		String strB = answersB.toString();

		ArrayList<Integer> answersF = new ArrayList<>();

		for(ArrayList<Box> field: fieldList) {
			field.forEach(box -> answersF.add(box.getAnswer()));
			String strF = answersF.toString();
			if(strB.equals(strF)) {
				return true;
			}
		}
		return false;
	}*/
}