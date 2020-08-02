package sudoku;

public class SudokuAugust {

	static Field field;

	public static void main(String[] args) {

		// 入力
		field = new Field();
		field.init();

		// 実行
		while(! field.changeMode()) {

			field.backlog();

			field.run();

			field.debug();

		}
		while(field.check()) {

			field.backlog();

			// 探索アルゴリズム

			field.debug();

		}


		// 出力
		field.output();

	}
}