package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class FieldSolver2 extends Field {

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
		backlog = backlog(field);
		solver(field, backlog);
	}
	
	/**
	* 探索アルゴリズム(boolean引数なし定義)
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup) {
		solver(ArrayList<Box> boxList, ArrayList<Box> backup, false);
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
	 *
	 * @return "contradicted" すべての仮解答で矛盾が生じた場合（二重探索にのみ発生）; "solved" 解答が完了した場合
	 */
	static String solver(ArrayList<Box> boxList, ArrayList<Box> backup, boolean isRandom) {

		// 仮解答を配置するマス＝実行者を選択する。
		if(isRandom == false){
			Collections.sort(backup, new BoxSort());
			Box rootBox = backup.get(0);
		} 
		if(isRandom == true) {
			do {
				Box rootBox = backup.get((int)(80*Math.random()));
			} while(rootBox.getAnswer() == 0);
		}
		
		Possibles possibles = rootBox.getPossibles();

		// 仮解答を選択し、探索を実行する。
		for(int possibleNum: possibles.getValues()) {
			
			if(isRandom == true){
				possibleNum = possibles.get((int)(Math.random()*(possibles.count()-1)));
			}
			String message = rootBox.solver(backup, possibleNum);

			// 矛盾が生じた場合、バックトラックして次の仮解答に進む。
			if(message.equals("contradicted")) {
				backup.clear();
				backup.addAll(backlog(boxList));
				replace(backup, rootBox);
				continue;
			}

			// 解答を続けられなくなった場合、現在の状態を保存し、二重で探索を実行する。
			if(message.equals("stopped")) {
				ArrayList<Box> deepBackup = backlog(backup);
				replace(deepBackup, rootBox);

				String deepMessage = solver(backup, deepBackup, isRandom);

				if(deepMessage.equals("solved")) {
					message = message.replace(message, deepMessage);
				}
			}

			// 解答が完了した場合、結果を反映して処理を終了する。
			if(message.equals("solved")) {
				boxList.clear();
				boxList.addAll(backup);
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
	boolean prove() {
		for(Box box: field) {
			if(box.getAnswer() == 0 && box.getPossibles().count() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 探索のバックトラックにおいて、探索を実行中のマスを引き継ぎます。
	 * 引き継いだマスとフィールドを紐づけるため、初期化も行います。
	 *
	 * @param boxList 復元した backlog
	 * @param rootBox 探索実行中のBoxインスタンス
	 */
	 static void replace(ArrayList<Box> boxList, Box rootBox) {

		for(Box box: boxList) {
			if(box.equals(rootBox)) {
				boxList.remove(box);
				boxList.add(rootBox);
				break;
			}
		}
		boxList.forEach(box -> box.init(boxList));
	 }
}
