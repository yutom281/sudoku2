package sudoku;

public class SudokuAugust {

	static Field field;

	public static void main(String[] args) {

		// 入力
		field = new Field();
		field.init();

		try {

			field.inspect();

		} catch(InputException ie) {
			ie.printStackTrace();
		}

		// 実行
		while(! field.changeMode()) {

			field.run();

		}

		while(! field.changeMode()) {

			field.solver();

		}

		// 出力
		if(field.check()) {

			field.output();

		} else {

			field.debug();
			System.out.println("解答できませんでした。途中経過を出力します");

		}


	}
}