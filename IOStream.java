package sudoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

public class IOStream {

	ArrayList<Box> rawField = new ArrayList<>();
	String sheetName;

	// 問題を入力するコンストラクタ
	// 問題の9x9マス以外の書式に「数値」を設定していると、空白のマスを取得しなくなる
	IOStream(String sheetName){

		this.sheetName = sheetName;
		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();

			FileInputStream fis = new FileInputStream(new File(strPath));
			Workbook wb=new XSSFWorkbook(fis);
			Sheet sht=wb.getSheet(sheetName);

			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sht.getRow(index_H);
				for(int index_V = 1; index_V < 10; index_V++) {
					Cell cell = row.getCell(index_V);

					Horizontal hor = new Horizontal(index_H);
	            	Vertical vert = new Vertical(index_V);
	            	int initAnswer = 0;

	            	if(cell != null) {
	            		initAnswer = (int)cell.getNumericCellValue();
	            	}

	            	Box box = new Box(hor, vert, initAnswer);
	            	rawField.add(box);
				}
			}

			wb.close();
			fis.close();

		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	// 問題を作成するコンストラクタ
	IOStream(int threshold){

		Factory factory = new Factory(threshold);
		ArrayList<Box> output = factory.get();

		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();

			FileInputStream fis = new FileInputStream(new File(strPath));
			Workbook wb=new XSSFWorkbook(fis);
			Sheet sht=wb.createSheet();
			sheetName = sht.getSheetName();

			int index = 0;
			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sht.createRow(index_H);
				row.setHeightInPoints(20);
				for(int index_V = 1; index_V < 10; index_V++) {
					Cell cell = row.createCell(index_V);
					sht.setColumnWidth(index_V, 1024);

					Box currentBox = output.get(index);
					int answer = currentBox.getAnswer();
					if(answer != 0) {
						cell.setCellValue(answer);
					}
					index++;
				}
			}
			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sht.getRow(index_H);
				for(int index_V = 11; index_V < 20; index_V++) {
					sht.setColumnWidth(index_V, 1024);
					row.createCell(index_V);
				}
			}

			drawLattice(wb, sht);

			fis.close();

			FileOutputStream fos = new FileOutputStream(strPath);
			wb.write(fos);
			wb.close();
			fos.close();

		}catch(IOException e) {
			e.printStackTrace();
		}
	}


	// 解答を出力する
	// OutputStream() の第二パラメータに true を指定すると、 excelブックにバグが発生する
	void output(){

		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();
	        FileInputStream fis = new FileInputStream(strPath);
			XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet(sheetName);
			int index = 0;

			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sheet.getRow(index_H);
				for(int index_V = 11; index_V < 20; index_V++) {
					Box currentBox = rawField.get(index);
					sheet.setColumnWidth(index_V, 1024);
					Cell cell = row.createCell(index_V);
					int answer = currentBox.getAnswer();
					cell.setCellValue(answer);
					index++;
				}
			}

			drawLattice(workbook, sheet);

			fis.close();

			FileOutputStream fos = new FileOutputStream(strPath);
			workbook.write(fos);
			workbook.close();
			fos.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// デバッグ、途中経過出力
	void debug() {

		output();

		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();
	        FileInputStream fis = new FileInputStream(strPath);
			XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet(sheetName);
			int index = 0;

			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sheet.getRow(index_H);
				row.setHeightInPoints(20);
				for(int index_V = 21; index_V < 30; index_V++) {
					Box currentBox = rawField.get(index);
					sheet.setColumnWidth(index_V, 6200);
					Cell cell = row.getCell(index_V);
					cell.setCellValue(currentBox.getPossibles().getValues().toString());
					index++;
				}
			}
			fis.close();

			FileOutputStream fos = new FileOutputStream(strPath);
			workbook.write(fos);
			workbook.close();
			fos.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	static void debug(ArrayList<Box> field) {

		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();
	        FileInputStream fis = new FileInputStream(strPath);
			XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(fis);
			XSSFSheet sheet = workbook.createSheet();
			int index = 0;

			for(int index_H = 1; index_H < 10; index_H++) {
				Row row = sheet.createRow(index_H);
				row.setHeightInPoints(20);
				for(int index_V = 1; index_V < 10; index_V++) {
					sheet.setColumnWidth(index_V, 1024);
					Cell cell = row.createCell(index_V);
					Box currentBox = field.get(index);
					int answer = currentBox.getAnswer();
					if(answer != 0) {
						cell.setCellValue(answer);
					}
					index++;
				}
			}
			fis.close();

			FileOutputStream fos = new FileOutputStream(strPath);
			workbook.write(fos);
			workbook.close();
			fos.close();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	void outputInspection(ArrayList<Box> flaw) {

		try {
			Path path = Path.of("mondai.xlsx");
	        String strPath = path.toAbsolutePath().toString();
	        FileInputStream fis = new FileInputStream(strPath);
			XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet(sheetName);
			int index = 0;

			XSSFFont font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.RED.getIndex());

			for(int i = 1; i < 10; i++) {
				Row row = sheet.getRow(i);
				row.setHeightInPoints(20);
				for(int j = 11; j < 20; j++) {
					Box currentBox = rawField.get(index);
					sheet.setColumnWidth(j, 1024);
					Cell cell = row.getCell(j);
					int answer = currentBox.getAnswer();
					if(answer != 0) {
						cell.setCellValue(answer);
					}

					if(flaw.contains(currentBox)) {
						CellUtil.setFont(cell, font);
					}

					index++;
				}
			}

			FileOutputStream fos = new FileOutputStream(strPath);
			workbook.write(fos);
			workbook.close();
			fos.close();

		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	ArrayList<Box> get(){
		return this.rawField;
	}

	void drawLattice(Workbook wb, Sheet sht) {

		CellStyle top = wb.createCellStyle();
		top.setBorderTop(BorderStyle.THICK);
		CellStyle bottom = wb.createCellStyle();
		bottom.setBorderBottom(BorderStyle.THICK);
		CellStyle right = wb.createCellStyle();
		right.setBorderRight(BorderStyle.THICK);
		CellStyle left = wb.createCellStyle();
		left.setBorderLeft(BorderStyle.THICK);
		CellStyle upLeftCorner = wb.createCellStyle();
		upLeftCorner.setBorderLeft(BorderStyle.THICK);
		upLeftCorner.setBorderTop(BorderStyle.THICK);
		CellStyle downLeftCorner = wb.createCellStyle();
		downLeftCorner.setBorderLeft(BorderStyle.THICK);
		downLeftCorner.setBorderBottom(BorderStyle.THICK);
		CellStyle upRightCorner = wb.createCellStyle();
		upRightCorner.setBorderRight(BorderStyle.THICK);
		upRightCorner.setBorderTop(BorderStyle.THICK);
		CellStyle downRightCorner = wb.createCellStyle();
		downRightCorner.setBorderRight(BorderStyle.THICK);
		downRightCorner.setBorderBottom(BorderStyle.THICK);

		for(int index_H = 1; index_H < 10; index_H++) {
			Row row = sht.getRow(index_H);
			for(int index_V = 1; index_V < 10; index_V++) {
				Cell cell = row.getCell(index_V);

				if(index_H == 1 || index_H == 4 || index_H == 7) {
					if(index_V == 1 || index_V == 4 || index_V == 7) {
						cell.setCellStyle(upLeftCorner);
					} else if(index_V == 9) {
						cell.setCellStyle(upRightCorner);
					} else {
						cell.setCellStyle(top);
					}
				} else if(index_V == 1 || index_V == 4 || index_V == 7) {
					cell.setCellStyle(left);
				} else if(index_V == 9) {
					cell.setCellStyle(right);
				}

				if(index_H == 9) {
					if(index_V == 1 || index_V == 4 || index_V == 7) {
						cell.setCellStyle(downLeftCorner);
					} else if(index_V == 9) {
						cell.setCellStyle(downRightCorner);
					} else {
						cell.setCellStyle(bottom);
					}
				}
			}
			for(int index_V = 11; index_V < 20; index_V++) {
				Cell cell = row.getCell(index_V);

				if(index_H == 1 || index_H == 4 || index_H == 7) {
					if(index_V == 11 || index_V == 14 || index_V == 17) {
						cell.setCellStyle(upLeftCorner);
					} else if(index_V == 19) {
						cell.setCellStyle(upRightCorner);
					} else {
						cell.setCellStyle(top);
					}
				} else if(index_V == 11 || index_V == 14 || index_V == 17) {
					cell.setCellStyle(left);
				} else if(index_V == 19) {
					cell.setCellStyle(right);
				}

				if(index_H == 9) {
					if(index_V == 11 || index_V == 14 || index_V == 17) {
						cell.setCellStyle(downLeftCorner);
					} else if(index_V == 19) {
						cell.setCellStyle(downRightCorner);
					} else {
						cell.setCellStyle(bottom);
					}
				}
			}
		}
	}

}
