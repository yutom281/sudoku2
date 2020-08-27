package sudoku;

import java.util.ArrayList;

public class Factory extends FieldSolver{

	static ArrayList<Box> blankField = new ArrayList<>();

	/**
	 * コンストラクタ。空のBoxインスタンスとフィールドを作成します。
	 */
	public Factory(int difficulty) {

		super(blankField);

		for(int column = 1; column < 10; column++) {
			for(int row = 1; row < 10; row++) {
				Box box = new Box(new Horizontal(column), new Vertical(row), 0);
				blankField.add(box);
			}
		}

		init();
		backlog = backlog(field);
		create(field, backlog);

		while(countTotalPossibles(field) > difficulty) {

			reverseSolver();
		}

	}

	/**
	 * 全マスを解答済みの状態にします。
	 * マスの解答順と解答は、制約の範囲内でランダムに決定します。
	 */
	String create(ArrayList<Box> field, ArrayList<Box> backlog) {

		while(check()) {

			int rndIndex = (int)Math.random()*80;
			Box rndBox = backlog.get(rndIndex);
			int rndAnswer = (int)(Math.random()*8+1);

			if(rndBox.getAnswer() != 0 || ! rndBox.getPossibles().contains(rndAnswer)) {
				continue;
			}

			// 以下、実行マス選択がランダムになる点以外、ロジックは solver() と共通
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
	int countTotalPossibles(ArrayList<Box> boxList) {

		int totalPossibles = 0;
		for(Box box: boxList) {
			totalPossibles += box.getPossibles().count();
		}
		return totalPossibles;
	}

	/**
	 * 解答が1パターンのみの回から、さらにマスをひとつ減らしたとき、パターンが増えるかを調べます。
	 * したがって、減らしたマスにもともと配置されていた以外の数を配置し、解答(solver)に成功すれば、
	 * 解答パターンが2つ以上存在することになり、問題として成立しなくなります。
	 */
	void reverseSolver() {

		ArrayList<Box> deepBacklog;

		int rndIndex = (int)Math.random()*80;
		Box rndBox = backlog.get(rndIndex);
		rndBox.rollback();

		for(int testNum: rndBox.getPossibles().get()) {

			String message = rndBox.solver(backlog, testNum);

			if(message.equals("contradicted")) {
				// ★
				backlog = backlog(field);
				replace(backlog, rndBox);
				continue;
			}

			if(message.equals("stopped")) {
				deepBacklog = backlog(backlog);
				replace(deepBacklog, rndBox);

				String deepMessage = solver(backlog, deepBacklog);

				if(deepMessage.equals("solved")) {
					message = message.replace(message, deepMessage);
				}
			}

			if(message.equals("solved")) {
				new IOStream(field, "extreme", "Auto_Mondai");
				break;
			}
		}
		replace(field, rndBox);
		backlog = backlog(field);
	}


	/**
	 *
	 */
	boolean checkIfExist(ArrayList<Box> backup, ArrayList<ArrayList<Box>> fieldList) {

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
	}

	/**
	 * 難易度を識別する列挙型
	 */
	public enum Difficulty {

		Easy(150),
		Normal(300),
		Hard(450),
		Very_hard(600),
		Extreme(100000);

		private int upperLimit;

		private Difficulty(int upperLimit) {
			this.upperLimit = upperLimit;
		}

		public int getValue() {
			return this.upperLimit;
		}
	};
}


