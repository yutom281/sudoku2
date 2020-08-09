package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class FieldSolver extends Field {

	/**
	 * コンストラクタ。これを呼び出したFieldインスタンスの情報を引き継ぎます。
	 */
	public FieldSolver(ArrayList<Box> boxList){
		this.field = boxList;
		this.backlog = backlog(this.field);
	}

	/**
	 * 探索アルゴリズムを開始します。コンストラクタを呼び出したFieldインスタンスより実行します。
	 */
	void solver() {
		solver(this.field, this.backlog);
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 * バックトラック（仮解答前の状態の復元）のため、仮解答は backup 上で行います。
	 * オリジナルの情報を変更しないので、バックトラックは backup を削除・再生成するだけで完了します。
	 * このとき、for文での NullPointerException を回避するため、仮解答を実行中のマスのみ再生成せずに
	 * ★元のインスタンスを引き継ぎます。（ArrayList.clear()でインスタンスが失われない場合、この操作は不要）
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
	String solver(ArrayList<Box> boxList, ArrayList<Box> backup) {

		ArrayList<Box> deepBackup;

		/*
		 * 仮解答を配置するマス＝実行者を選択する。
		 */
		Collections.sort(backup, new BoxSort());
		Box rootBox = backup.get(0);
		Possibles rootPossibles = rootBox.getPossibles();

		/*
		 * 仮解答を選択し、探索を実行する。
		 */
		for(int possibleNum: rootPossibles.get()) {

			String message = rootBox.solver(backup, possibleNum);

			/*
			 * 矛盾が生じた場合、次の仮解答に進む。
			 */
			if(message.equals("contradicted")) {
				backup.clear();
				backup.addAll(backlog(boxList));
				replace(backup, rootBox);
				continue;
			}
			/*
			 * 解答を続けられなくなった場合、現在の状態を保存し、二重で探索を実行する。
			 */
			if(message.equals("stopped")) {
				deepBackup = backlog(backup);
				replace(deepBackup, rootBox);
				rootBox.setTmpAnswer(possibleNum);
				/*
				 * rootBox は setTmpAnswer() により配置できる数＝0 となっている。
				 * したがって、この二重探索 solver() 内の BoxSort() によりリストの最後尾に並べられ、
				 * 二重探索の実行者（ = deepBackup.get(0)）には必ず他のマスが選択される。
				 */
				String deepMessage = solver(backup, deepBackup);

				/*
				 * 二重探索のすべての仮解答で矛盾が生じたら、この探索の仮解答が誤りとなり、次の仮解答に進む。
				 */
				if(deepMessage.equals("contradicted")) {
					deepBackup.clear();
					continue;
				}
				/*
				 * 二重探索で解答が完了したら、この探索に結果と完了メッセージを返す。
				 */
				if(deepMessage.equals("solved")) {
					backup.clear();
					backup.addAll(deepBackup);
					message = message.replace(message, deepMessage);
				}
			}
			/*
			 * 解答が完了した場合、処理を終了する。
			 */
			if(message.equals("solved")) {
				boxList.clear();
				boxList.addAll(backup);
				Collections.sort(boxList, new IndexSort());
				return message;
			}
		}
		/*
		 * すべての仮解答で矛盾が生じた場合、バックトラックにおいてひとつ上の仮解答が誤りとなり、メッセージを返して終了する。
		 * その後、ひとつ上の solver() のforループは次の仮解答に進む。
		 *
		 * 最上位＝最初の探索では、仮解答のうちひとつは必ず正しいため、上記は発生しない（必ず "solved" を返して終了する）。
		 */
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
	 * 引き継いだマスと復元フィールドを互いに紐づけるため、初期化も行います。
	 *
	 * 仮解答前の状態を復元した後に使用します。
	 *
	 * @param boxList 復元した backup
	 * @param rootBox 探索実行中のBoxインスタンス
	 */
	 static void replace(ArrayList<Box> boxList, Box rootBox) {


		Collections.sort(boxList, new BoxSort());
		boxList.remove(0);
		boxList.add(rootBox);
		boxList.forEach(box -> box.init(boxList));
		/*
		for(Box box: boxList) {
			if(box.equals(rootBox)) {
				boxList.remove(box);
				boxList.add(rootBox);
				break;
			}
		}
		boxList.forEach(box -> box.init(boxList));
		*/
	 }

}
