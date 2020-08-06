package sudoku;

import java.util.ArrayList;

public class Box {

	private Horizontal hor;
	private Vertical vert;
	private int square;
	private int answer = 0;

	private Area areaHorizontal;
	private Area areaVertical;
	private Area areaSquare;

	private Possibles possibles;

	Box(Horizontal hor, Vertical vert, int initNumber){
		this.hor = hor;
		this.vert = vert;
		this.square = hor.getSquareId() * vert.getSquareId();
		if(initNumber != 0) {
			this.answer = initNumber;
			possibles = new Possibles();
		}
	}

	void init(ArrayList<Box> field) {
		if(answer == 0) {
			this.areaHorizontal = new Area(field, this, hor);
			this.areaVertical = new Area(field, this, vert);
			this.areaSquare = new Area(field, this, square);
			this.possibles = new Possibles(areaHorizontal.getNumbers(),areaVertical.getNumbers(),areaSquare.getNumbers());
		}
	}

	void calc() {
		if(answer == 0) {
			remove(areaHorizontal.calc());
			remove(areaVertical.calc());
			remove(areaSquare.calc());

			search(areaHorizontal);
			search(areaVertical);
			search(areaSquare);
		}
	}

	// 「N国同盟（ダブル数字）」ロジックによりPossiblesから数を取り除く
	void remove(ArrayList<Integer> notAnswer) {
		int check = possibles.remove(notAnswer);
		if(check != 0) {
			this.answer = check;
			areaHorizontal.update(answer);
			areaVertical.update(answer);
			areaSquare.update(answer);
		}
	}

	// Possibles のうち、Area 内で自身しか持たない数を探す
	void search(Area area) {
		int result = area.search(possibles);
		if(result != 0) {
			this.answer = result;
			areaHorizontal.update(answer);
			areaVertical.update(answer);
			areaSquare.update(answer);
		}
	}

	/**
	 * 探索アルゴリズムを使用して解答します。
	 *
	 * 配置できる数の少ないマスから処理する。
	 * 配置前にこのBoxインスタンスと全フィールドのコピーを作成する。「参照渡し」ではなく別インスタンスを生成することに注意。
	 *
	 * 候補の数のうち一つを配置し、Areaを通じて20マスを更新する。前の分岐点に戻る場合、この20マスも「元に戻す」必要がある。
	 * Area20マスのさらにArea20マス……のように、変更の影響範囲にそってBox.calc()を進める。
	 * １）最後まで到達した場合→終了。
	 * ２）「矛盾」が生じた場合→このBoxのコピー（＝１回目のsolver()の取り消し基準点）に戻り、配置した数を候補から削除して次の数を配置する。
	 * ３）再び解答不能になった場合→二次コピー（＝２回目のsolver()の取り消し基準点）を作成し、
	 *     Areaの20マスの中から、候補の数が最も少ないマスを次に選んでsolver()を実行する。
	 *     次のマスのすべての候補で「矛盾」が発生した場合、二次コピーを復元してこのBoxに戻り、３つめのBoxでsolver()を実行する
	 *
	 *「矛盾」：配置できる数が配置前に0になる。
	 *「元に戻す」：answer = 0 にする。Area内の20マスのanswer, Possiblesを復元する。これを「基準点」に到達するまで繰り返す
	 *「基準点」：コピーを作成した、「候補の数のうち一つを配置し」た時点。
	 */

	 String solver(ArrayList<Box> backup, int possibleNum) {

		answer = possibleNum;
		possibles = new Possibles();
		areaHorizontal.update(possibleNum);
		areaVertical.update(possibleNum);
		areaSquare.update(possibleNum);

		Field fieldBackup = new Field(backup);

		fieldBackup.run();
		fieldBackup.run();

		// 解答が完了した場合
		if(! fieldBackup.check()) {
			return "solved";
		}
		// 解答不能になった場合
		if(fieldBackup.check()) {


			return "stopped";
		}
		// 矛盾が生じた場合
		if(fieldBackup.findContradiction()) {


			return "contradicted";
		}

		return "error";
	 }

	/**
	 * 自身のコピーを作成します。
	 * 探索時、前の分岐点に戻るときに使用します。
	 */
	Box copy(){
		Box replica = new Box(this.hor, this.vert, this.answer);
		return replica;
	}

	/**
	 * Boxインスタンスを比較・判別します。
	 *
	 * @return {@code true} 行番号と列番号が同じ場合。
	 */
	boolean equals(Box box) {
		if(hor.equal(box.getHorizontal()) && vert.equal(box.getVertical())) {
			return true;
		}
		return false;
	}

	// Getter
	Horizontal getHorizontal() {
		return this.hor;
	}
	Vertical getVertical() {
		return this.vert;
	}
	int getSquare() {
		return this.square;
	}
	int getAnswer() {
		return this.answer;
	}
	Possibles getPossibles() {
		return this.possibles;
	}
}
