package sudoku;

import java.util.Scanner;

public class SudokuAugust {

	static Field field;

	public enum Difficulty {

		Easy(400),
		Normal(500),
		Hard(600),
		Expert(700),
		Impossible(100000);

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
				System.out.println("[ Easy Normal Hard Expert Impossible ]");
				input = scan.next();
			} while(! Difficulty.checkIfExist(input));

			int threshold = Difficulty.valueOf(input).get();

			new IOStream(threshold);
			System.out.println("問題を作成しました");
			scan.close();
			System.exit(1);

		} else {
			IOStream input = new IOStream(args[0]);
			field = new Field(input);
			field.init();
		}


		try {

			field.inspect();

		} catch(InputException ie) {
			ie.printStackTrace();
			System.exit(1);
		}

		// 実行
		while(! field.changeMode()) {

			field.run();

		}

		while(field.check()) {

			field.solver();

		}

		// 出力
		if(! field.check()) {

			field.output();

		} else {

			field.debug();
			System.out.println("解答できませんでした。途中経過を出力します");

		}


	}
}