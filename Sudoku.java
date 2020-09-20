package sudoku;

import java.util.Scanner;

public class Sudoku {

	static private Field field;

	public enum Difficulty {

		Easy(270),
		e(270),
		Normal(280),
		n(280),
		Hard(290),
		h(290),
		Professional(300),
		p(300),
		Impossible(100000),
		i(100000);

		private int threshold;

		private Difficulty(int threshold) {
			this.threshold = threshold;
		}

		int get() {
			return threshold;
		}

		static boolean checkIfExist(String input) {
			for(Difficulty dif: values()) {
				if(dif.name().equals(input)) {
					return true;
				}
			}
			System.out.println("正しい値を入力してください");
			return false;
		}

	};

	public static void main(String[] args) {
		// 入力
		if(args.length == 0) {
			Scanner scan = new Scanner(System.in);
			String input;
			do {
				System.out.println("難易度を選択してください");
				System.out.println("[ Easy Normal Hard Professional Impossible ]");
				input = scan.next();
			} while(! Difficulty.checkIfExist(input));

			int threshold = Difficulty.valueOf(input).get();

			new IOStream(threshold);
			System.out.println("問題を作成しました");
			scan.close();
			System.exit(0);

		} else {
			IOStream input = new IOStream(args[0]);
			field = new Field(input);
			field.init();
		}

		// 問題の検査
		try {
			field.inspect(true);
		} catch(InputException ie) {
			ie.printStackTrace();
			System.exit(10);
		}

		// 解答
		while(! field.isStuck()) {
			field.run();
		}
		while(! field.isSolved()) {
			field.solver();
		}

		// 出力
		field.output();
	}
}