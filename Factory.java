package sudoku;

import java.util.ArrayList;

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

		init();
		backlog = backlog(field);

		//while(check()) {
			create(field, backlog);
		//	init();
			IOStream.debug(field);
		//}

		while(countTotalPossibles(field) < threshold) {
			String message = reverseSolver();
			if(message.equals("limit_reached")) {
				break;
			}
		}
		System.out.println("配置できる数の個数合計：" + countTotalPossibles(field));
		//★rollback(field, rollbackList);
		//Collections.sort(field, new IndexSort());
		//IOStream.debug(field);
	}

	ArrayList<Box> get(){
		return field;
	}

	/**
	 * 全マスを解答済みの状態にします。
	 * マスの解答順と解答は、制約の範囲内でランダムに決定します。
	 */
	String create(ArrayList<Box> field, ArrayList<Box> backlog) {

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

				String deepMessage = create(backlog, deepBacklog);

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
	 *
	 * ロジック
	 * 解答済みのマスをランダムに選ぶ
	 * ★（backlog上で）解答を0にして、配置できる数を再計算する
	 * ★他のマスの配置できる数を再計算する
	 * ★そのマスの配置できる数からもともとの解答を取り除く
	 * ★をすべての対象マスについて繰り返し、かつ、元の解答を取り除いておく
	 * そのマスでsolverを実行する
	 * 解答成功→そのマスを復元、問題を出力して終了
	 * 全仮解答が矛盾→そのマスをFieldでも0にする
	 * 					solver前の状態(rollback直後)を復元する(contradicted, stopped)
	 * 					replaceはrollbackを打ち消してしまうため使えない
	 *
	 * 元の解答を配置できない状態でマス同士の「依存関係」を構築するため、
	 * replace(field, rndBox)時に必ずrndBoxをrollback()する
	 */
	String reverseSolver() {

		ArrayList<Box> rollbackList = new ArrayList<>();

		int rndIndex = (int)(80*Math.random());
		Box rndBox = backlog.get(rndIndex);
		if(rndBox.getAnswer() == 0) {
			return "skip";
		}
		rndBox.rollback();
		rollbackList.add(rndBox);
		Possibles rePossibles = rndBox.getPossibles();

		if(rePossibles.count() == 0) {
			//★
			Box equivBox = field.get(rndIndex);
			equivBox.rollback();
			//★rollbackList.add(rndBox);
			return "goNext";
		}

		for(int testNum: rePossibles.getValues()) {

			String message = rndBox.solver(backlog, testNum);

			if(message.equals("contradicted")) {
				backlog = backlog(field);
				rollback(backlog, rollbackList);
				continue;
			}

			/*
			 * 1.空白マスが増えてsolverを使用しないと計算を進められないとき
			 * 2.solved: 別パターンの解答が見つかった時
			 * →多重探索もrndBoxの配置できる数の情報を引き継がなければならない
			 */
			if(message.equals("stopped")) {
				ArrayList<Box> deepBacklog = backlog(backlog);
				rollback(backlog, rollbackList);
				rollback(deepBacklog, rollbackList);

				String deepMessage = solver(backlog, deepBacklog);

				if(deepMessage.equals("solved")) {
					message = message.replace(message, deepMessage);
				}
			}
			// ★これが返るのが早すぎる
			if(message.equals("solved")) {
				return "limit_reached";
			}
		}
		//★
		Box equivBox = field.get(rndIndex);
		equivBox.rollback();
		//★rollbackList.add(rndBox);
		return "goNext";
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


/*
String create(ArrayList<Box> field, ArrayList<Box> backlog) {

	while(check()) {

		int rndIndex = (int) (80*Math.random());
		Box rndBox = backlog.get(rndIndex);
		int rndAnswer = (int)(Math.random()*8+1);

		if(rndBox.getAnswer() != 0 || ! rndBox.getPossibles().contains(rndAnswer)) {
			continue;
		}

		// 以下、実行マス選択がランダムになる点以外、solver() と共通
		String message = rndBox.solver(backlog, rndAnswer);

		if(message.equals("contradicted")) {
			backlog.clear();
			backlog.addAll(backlog(field));
			replace(backlog, rndBox);
			continue;
		}

		if(message.equals("stopped")) {
			ArrayList<Box> deepBacklog = backlog(backlog);
			replace(deepBacklog, rndBox);

			String deepMessage = solver(backlog, deepBacklog);

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
}*/


