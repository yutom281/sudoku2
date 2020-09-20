package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class FieldSolver extends Field {

	/**
	 * コンストラクタ。これを呼び出したFieldインスタンスの情報を引き継ぎます。
	 */
	public FieldSolver(ArrayList<Box> boxList){
		field = boxList;
	}

	/**
	 * 探索アルゴリズムを開始します。コンストラクタを呼び出したFieldインスタンスより実行します。
	 */
	void solver() {
		backlog = createLog(field);
		solver(field, backlog);
	}

	/**
	* 探索アルゴリズム(Box引数なし、boolean引数なし定義)
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup) {
		return solver(boxList, backup, null, false);
	}

	/**
	 * 探索アルゴリズム(Box引数なし定義)
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup, boolean isRandom) {
		return solver(boxList, backup, null, isRandom);
	}

	/**
	 * 探索アルゴリズム(boolean引数なし定義)
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup, Box execBox) {
		return solver(boxList, backup, execBox, false);
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 * バックトラック（仮解答前の状態の復元）のため、仮解答は backup 上で行います。
	 * オリジナルの情報を変更しないので、バックトラックは backup を削除・再生成するだけで完了します。
	 * このとき、仮解答を実行中のマスのみ再生成せずに同じインスタンスを引き継ぎます。
	 *
	 * 仮解答が誤りだった場合、前の状態を復元し、次の仮解答に進みます。
	 *
	 * 再び解答を続けられなくなった場合、現在の状態を保存し、二重で探索を実行します。
	 * 二重探索のすべての仮解答が誤りだった場合、最初の探索の仮解答は誤りとなり、次の仮解答に進みます。
	 *
	 * @param boxList 復元の基準となる時点の field
	 * @param backup boxList のログ
	 * @param execBox 探索を開始するマスを指定する（第2引数 backup に紐づいていなければならない）
	 * @param isRandom 探索順と仮解答の選択をランダムにする
	 *
	 * @return "contradicted" すべての仮解答で矛盾が生じた場合; "solved" 解答が完了した場合
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup, Box execBox, boolean isRandom) {

		// 仮解答を配置するマス（実行マス）を選択する。
		Collections.sort(backup, new BoxSort());
		Box rootBox = backup.get(0);
		if(execBox != null && backup.contains(execBox)) {
			rootBox = execBox;
		}
		if(isRandom) {
			do {
				rootBox = backup.get((int)(80*Math.random()));
			} while(rootBox.getAnswer() != 0);
		}

		// 仮解答を選択し、探索を実行する。
		for(int possibleNum: rootBox.getPossibles().getValues()) {

			String message = rootBox.solver(backup, possibleNum);

			// 矛盾が生じた場合、バックトラックして次の仮解答に進む。
			if(message.equals("contradicted")) {
				rollback(backup, boxList);
				continue;
			}
			// 解答を続けられなくなった場合、現在の状態を保存し、二重で探索を実行する。
			if(message.equals("stopped")) {
				ArrayList<Box> deepBackup = createLog(backup);
				String deepMessage = solver(backup, deepBackup, isRandom);

				if(deepMessage.equals("solved")) {
					message = message.replace(message, deepMessage);
				}
			}
			// 解答が完了した場合、結果を反映して処理を終了する。
			if(message.equals("solved")) {
				// 引数の順序を逆にして結果反映に使用
				rollback(boxList, backup);
				Collections.sort(boxList, new IndexSort());
				return message;
			}
		}
		// すべての仮解答で矛盾が生じた場合、バックトラックにおいてひとつ上の仮解答が誤りとなり、メッセージを返して終了する。
		// その後、ひとつ上の solver() は次の仮解答に進む。
		return "contradicted";
	}

	/**
	 * 探索において、仮解答により矛盾が生じたか調べます。
	 * 矛盾が見つかった場合、仮解答を偽と判断します。
	 *
	 * @return {@code false} 未回答かつ配置できる数がないマスが存在する場合。
	 */
	boolean isContradicted() {
		for(Box box: field) {
			if(box.getAnswer() == 0 && box.getPossibles().count() == 0) {
				return true;
			}
		}
		return false;
	}

	 /**
	  * ログに記録されている情報をコピーします。
	  * もとのインスタンスが維持されます。
	  *
	  * @param boxList 回復するフィールド
	  * @param logInfo ログとなるバックアップ
	  */
	static void rollback(ArrayList<Box> boxList, ArrayList<Box> logInfo) {
		for(Box box: boxList) {
			Box log = getBox(logInfo, box);
			box.copy(log);
		}
		for(Box box: boxList) {
			box.recalc();
		}
	}

	static Box getBox(ArrayList<Box> boxList, Box searcher) {
		return getBox(boxList, searcher.getHorizontal(), searcher.getVertical());
	}

	static Box getBox(ArrayList<Box> boxList, Horizontal hor, Vertical vert) {
		int index = 0;
		for(Box box: boxList) {
			if(box.getHorizontal().equal(hor) && box.getVertical().equal(vert)) {
				break;
			}
			index++;
		}
		return boxList.get(index);
	}
}
