package sudoku;

import java.util.ArrayList;

public class Test {

	static String message = "NG";
	static ArrayList<Integer> list  = new ArrayList<>();
	static ArrayList<String> strList = new ArrayList<>();

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		/*
		list.add(1);
		System.out.println(list);
		method2(list);
		System.out.println(list);

		System.out.println(message);
		method3(message);
		System.out.println(message);
		*/

		String str = "before";
		String str2 = "after";
		strList.add(str);
		strList.add(str2);
		System.out.println(strList);
		method4();
		System.out.println(strList);
		method4_2(strList);
		System.out.println(strList);
		method5();
		System.out.println(strList);
		method6(strList);
		System.out.println(strList);
		method7();
		System.out.println(strList);

	}
	static void method() {
		list = new ArrayList<Integer>();
	}

	static void method2(ArrayList<Integer> list) {
		list.clear();
		list.add(2);
	}

	static void method3(String message) {
		message = "OK";
	}

	// 新しいオブジェクトの参照値を渡すのではなく、
	// 既存のオブジェクトを操作する場合は、
	// フィールドと引数のどちらからアクセスしても結果は変わらない
	static void method4() {
		String str = strList.get(0);
		strList.clear();
		strList.add(str);
	}

	static void method4_2(ArrayList<String> strList) {
		strList.clear();
		strList.add("survive");
	}

	// フィールドを参照させると、スコープがメソッド外におよぶ
	static void method5() {
		strList = new ArrayList<>();
	}

	// 引数に渡すと、スコープがメソッド内に限定される
	// アクセス修飾子は無関係
	static void method6(ArrayList<String> strList) {
		strList = new ArrayList<>();
		strList.add("remain");
	}

	static Box method7() {
		strList.add("box");
		return new Box(new Horizontal(1), new Vertical(1), 1);
	}

}
