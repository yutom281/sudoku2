package sudoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class IOStream {

	ArrayList<Box> rawField = new ArrayList<>();
	String outputFileName = "";

	// 問題を入力するコンストラクタ
	IOStream(){
		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();

			FileInputStream fis = new FileInputStream(new File(strPath));
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
	IOStream(ArrayList<Box> field, String sheetName, String fileName){
		try {
			outputFileName = fileName + ".xlsx";
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(sheetName);
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
			Path path = Path.of(outputFileName);
	        String strPath = path.toAbsolutePath().toString();
			FileOutputStream outputStream = new FileOutputStream(strPath);
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

		new IOStream(field, "answers", "debugAnswers");
	}

	static void outputInspection(ArrayList<Box>field, ArrayList<Box> flaw) {

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("flaw");
			int index = 0;

			XSSFFont font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.RED.getIndex());

			for(int i = 0; i < 9; i++) {
				Row row = sheet.createRow(i);
				row.setHeightInPoints(20);
				for(int j = 0; j < 9; j++) {
					Box currentBox = field.get(index);
					sheet.setColumnWidth(i, 1024);
					Cell cell = row.createCell(j);
					cell.setCellValue(currentBox.getAnswer());

					if(flaw.contains(currentBox)) {
						CellUtil.setFont(cell, font);
					}

					index++;
				}
			}

			Path path = Path.of("inspection.xlsx");
	        String strPath = path.toAbsolutePath().toString();
			FileOutputStream outputStream = new FileOutputStream(strPath);
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

/*
	 static void solverDebug (ArrayList<Box> log) {

		 try {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet("possibles");
				int index = 0;

				for(int i = 0; i < 9; i++) {
					Row row = sheet.createRow(i);
					row.setHeightInPoints(20);
					for(int j = 0; j < 9; j++) {
						Box currentBox = log.get(index);
						sheet.setColumnWidth(i, 1024);
						Cell cell = row.createCell(j);
						cell.setCellValue(currentBox.getPossibles().get().toString());
						index++;
					}
				}
				FileOutputStream outputStream = new FileOutputStream("solDebugPossibles.xlsx");
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
						Box currentBox = log.get(index);
						sheet.setColumnWidth(i, 1024);
						Cell cell = row.createCell(j);
						int answer = currentBox.getAnswer();
						if(answer != 0) {
							cell.setCellValue(answer);
						}
						index++;
					}
				}
				FileOutputStream outputStream = new FileOutputStream("solDebugAnswers.xlsx");
				workbook.write(outputStream);
				workbook.close();
				outputStream.close();

			} catch(IOException e) {
				e.printStackTrace();
			}

	 }
	 */
}
