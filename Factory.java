package sudoku;

import java.util.ArrayList;
import java.util.Collections;

public class Factory extends FieldSolver{

	static ArrayList<Box> blankField = new ArrayList<>();
	//ArrayList<Box> rollbackList = new ArrayList<>();

	/**
	 * コンストラクタ。問題を作成します。
	 *
	 * @param threshold 難易度ごとの基準値
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
		backlog = createLog(field);

		// 空のフィールドを埋める
		while(! isSolved()) {
			solver(field, backlog, true);
		}
		// （デバッグ）埋めたフィールドを出力
		IOStream.debug(field);

		// マスを限界まで取り消す
		reverseSolver();

		// 指定された難易度に合わせて、解答を元に戻す
		adjust(threshold);

		// コンソール出力
		print(field);
		System.out.println("配置できる数の個数合計：" + countTotalPossibles(field));
	}

	/**
	 * 逆算により、解答から問題を作成します。
	 * 全マス解答済みの状態から、解答が一意に定まる（問題として成立する）限界までマスを取り消すメソッドです。
	 * 核となるロジックは探索と同じものであり、微分に対する積分のような逆演算ではありません。
	 *
	 * まず、1マスを選択して取り消し、配置できる数を再計算します。
	 * その後、探索を実行し、他の解答パターンが存在するか調べます。
	 * 存在しない場合、そのマスを取り消しても問題の解答は一意のままです。
	 * これを、それ以上どのマスを取り消しても問題が成立しない状態まで繰り返します。
	 *
	 * 準備処理として、探索可能な（配置できる数を2つ以上もつ）マスを発生させるため、
	 * 必要最低限のマス（5~6個）を無条件に取り消します。
	 * これにより、探索前に限界以上のマスが取り消されることを回避します。
	 */
	void reverseSolver() {
		// （準備処理）
		// 探索可能なマスを発生させる
		Collections.shuffle(field);
		// 取消
		Box rbBox = field.get(0);
		int rbAnswer = rbBox.rollback();
		// マスAに置ける数が2つある ← このとき、同じ縦or横orブロックのマスBが空である
		// ← マスBが空であるとき、マスBに置ける数が2つある ← ......のループ処理
		while(findSolvableBox(field).size() == 0) {
			// 取り消したマスの縦横ブロックから1マス取得
			ArrayList<Box> areaBoxList = rbBox.getAreaBox(rbAnswer, "NOT");
			Collections.shuffle(areaBoxList);
			Box nextBox = areaBoxList.get(0);
			// 取消＋次の起点に設定
			int rbAnswerNext = nextBox.rollback();
			rbBox = nextBox;
			rbAnswer = rbAnswerNext;
		}
		// （メイン処理）
		// 全マスをひとつずつ取り消し、その度に元の解答以外の全解答パターンを調べる
		// 一度でも解答に成功したら、そのマスは取り消せない
		for(int i = 0; i < 81; i++) {
			Box box = field.get(i);
			if(box.getAnswer() == 0) {
				continue;
			}
			box.rollback();
			backlog = createLog(field);
			ArrayList<Box> slvBacklog = createLog(backlog);
			// 探索可能なマスを取得
			ArrayList<Box> solvables = findSolvableBox(slvBacklog);

			// 全解答パターンを調べる
			for(Box slvBox: solvables) {
				// 最初の解答を発生させないため、そのとき置かれていた数を置けなくする
				int slvAnswer = slvBox.rollback();
				slvBox.getPossibles().remove(slvAnswer);

				String message = solver(backlog, slvBacklog, slvBox);
				// 解答に成功した場合、取消前の状態に戻して、次の取消に進む
				if(message.equals("solved")) {
					box.rollforward();
					rollback(backlog, field);
					break;
				}
				// 解答できなかった場合、取消後・探索前の状態に戻して、次の探索可能マスに進む
				if(message.equals("contradicted")) {
					rollback(backlog, field);
					rollback(slvBacklog, backlog);
					// ＋こうすると、ループ中すでに探索したパターンも出なくなる
					slvBox.rollforward();
				}
			}
			//すべての探索可能マスで解答できなかった場合、取消を確定し、次の取消に進む
		}
		Collections.sort(field, new IndexSort());
	}

	/**
	 * 探索を実行可能なマスを取得します。
	 *
	 * @param boxList 探索するフィールドまたはバックアップ
	 *
	 * @return solvables 探索可能なマスのリスト
	 */
	ArrayList<Box> findSolvableBox(ArrayList<Box> boxList) {
		ArrayList<Box> solvables = new ArrayList<>();
		for(Box box: boxList) {
			if(box.getPossibles().count() >= 2) {
				solvables.add(box);
			}
		}
		return solvables;
	}

	/**
	 * 指定された難易度に合わせて、取り消したマスを復元します。
	 *
	 * @param threshold 難易度ごとの、配置できる数の個数合計の上限値
	 */
	void adjust(int threshold) {
		while(countTotalPossibles(field) > threshold) {
			Collections.sort(field, new PossiblesSort());
			Box fwBox = field.get(0);
			fwBox.rollforward();
		}
		Collections.sort(field, new IndexSort());
	}

	/**
	 * 配置できる数の個数の合計を返します。
	 *
	 * @param boxList 調べるフィールドまたはバックアップ
	 *
	 * @return totalPossibles 個数合計
	 */
	static int countTotalPossibles(ArrayList<Box> boxList) {
		int totalPossibles = 0;
		for(Box box: boxList) {
			totalPossibles += box.getPossibles().count();
		}
		return totalPossibles;
	}

	// getter
	ArrayList<Box> get(){
		return field;
	}


	// （デバッグ）
	//
	//System.out.println("初期数の個数合計：" + countFilledBox(field));
	/*
	static int countFilledBox(ArrayList<Box> boxList) {
		int filled = 0;
		for(Box box: boxList) {
			if(box.getAnswer() != 0) {
				filled++;
			}
		}
		return filled;
	}
	*/
}
