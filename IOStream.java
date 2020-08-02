package sudoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class IOStream {

	ArrayList<Box> rawField = new ArrayList<>();

	// 問題を入力するコンストラクタ
	IOStream(){
		try {
			FileInputStream fis=new FileInputStream(new File("C:\\Users\\chiec\\Desktop\\pleiades-2020-03-java-win-64bit-jre_20200322\\pleiades\\workspace\\sudoku\\mondai.xlsx"));
			Workbook wb=new XSSFWorkbook(fis);
			Sheet sht=wb.getSheetAt(0);
			Iterator<Row> iterator = sht.iterator();
			int index_H = 1;
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();
	            Iterator<Cell> cellIterator = currentRow.iterator();

	            int index_V = 1;
	            while (cellIterator.hasNext()) {

	            	Cell currentCell = cellIterator.next();

	            	Horizontal hor = new Horizontal(index_H);
	            	Vertical vert = new Vertical(index_V);
	            	int initAnswer = 0;

	            	if(currentCell.getNumericCellValue() != 0) {
	            		initAnswer = (int)currentCell.getNumericCellValue();
	            	}
	            	Box box = new Box(hor, vert, initAnswer);
	            	rawField.add(box);

	            	index_V++;
	            }
	            index_H++;
			}
			wb.close();
			fis.close();

		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	// 解答を出力するコンストラクタ
	IOStream(ArrayList<Box> field){
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("answer");
			int index = 0;

			for(int i = 0; i < 9; i++) {
				Row row = sheet.createRow(i);
				row.setHeightInPoints(20);
				for(int j = 0; j < 9; j++) {
					Box currentBox = field.get(index);
					sheet.setColumnWidth(i, 1024);
					Cell cell = row.createCell(j);
					cell.setCellValue(currentBox.getAnswer());
					index++;
				}
			}
			FileOutputStream outputStream = new FileOutputStream("answer.xlsx");
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// デバッグ　途中経過出力
	static void debug(ArrayList<Box> field) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("possibles");
			int index = 0;

			for(int i = 0; i < 9; i++) {
				Row row = sheet.createRow(i);
				row.setHeightInPoints(20);
				for(int j = 0; j < 9; j++) {
					Box currentBox = field.get(index);
					sheet.setColumnWidth(i, 1024);
					Cell cell = row.createCell(j);
					cell.setCellValue(currentBox.getPossibles().get().toString());
					index++;
				}
			}
			FileOutputStream outputStream = new FileOutputStream("debugPossibles.xlsx");
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("answers");
			int index = 0;

			for(int i = 0; i < 9; i++) {
				Row row = sheet.createRow(i);
				row.setHeightInPoints(20);
				for(int j = 0; j < 9; j++) {
					Box currentBox = field.get(index);
					sheet.setColumnWidth(i, 1024);
					Cell cell = row.createCell(j);
					int answer = currentBox.getAnswer();
					if(answer != 0) {
						cell.setCellValue(answer);
					}
					index++;
				}
			}
			FileOutputStream outputStream = new FileOutputStream("debugAnswers.xlsx");
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}


	ArrayList<Box> get(){
		return this.rawField;
	}
}
