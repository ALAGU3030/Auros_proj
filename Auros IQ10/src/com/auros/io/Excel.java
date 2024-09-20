package com.auros.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.auros.model.AssessIssues;
import com.auros.model.AssessReportOcc;
import com.auros.model.AssessResult;
import com.auros.model.Config;
import com.auros.model.Constant;
import com.auros.model.IssueReportOcc;
import com.auros.model.IssueReportType;

public class Excel {
	private Map<String, AssessReportOcc> assessmentResult = null;
	private AssessIssues assessmentIssues;
	private Map<String, IssueReportOcc> issueResult = null;
	private Config config = null;
	private Map<String, CellStyle> cellStyle = new HashMap<String, CellStyle>();
	private boolean isIssueReport = false;
	private static Logger logger = Logger.getLogger(Excel.class);
	private String queryTime = "";
	private String startTime = null;

	public Excel(Config config, AssessResult assessmentResult, AssessIssues assessmentIssues, String queryTime,
			Map<String, FileAppender> fileAppenders, String startTime) {
		this.assessmentResult = assessmentResult;
		this.assessmentIssues = assessmentIssues;
		this.config = config;
		this.queryTime = queryTime;
		this.startTime = startTime;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
	}

	public Excel(Config config, IssueReportType issueResult, String queryTime, Map<String, FileAppender> fileAppenders,
			String startTime) {
		this.issueResult = issueResult;
		this.config = config;
		this.isIssueReport = true;
		this.queryTime = queryTime;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
	}

	public void writeExcel(Constant.ReportType reportType) throws IOException {
		String dateToday = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss").format(Calendar.getInstance().getTime());
		String outputDir = config.getOutputDir();
		String excelFileName = "";
		String suffix = config.getFileSuffix();
		excelFileName = outputDir + "\\" + reportType.prefix() + suffix + "_" + dateToday + ".xlsx";
		FileOutputStream out = saveNextTry(excelFileName);

		if (out != null) {
			SXSSFWorkbook workbook = new SXSSFWorkbook(Constant.ExcelFlushSize);
			createStyles(workbook);
			createSheet(workbook);
			workbook.write(out);
			out.close();

			logger.debug("Result saved successfully to :" + excelFileName.replace("\\\\", "\\"));
			System.out.println("\nResult saved successfully to :" + excelFileName.replace("\\\\", "\\"));

		} else {
			String saveErr = "Error saving File: " + excelFileName;
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(saveErr + ";" + startTime + ";" + crrntTime + ";" + "EXCEL");
			System.out.println(saveErr);
		}

	}

	@SuppressWarnings("unused")
	private void autoSizeSheet(Sheet sheet) {
		Vector<String> header = config.getAssessHeader();
		int maxColSize = 15500;
		int firstColSize = 4500;

		for (int col = 0; col < header.size(); col++) {
			sheet.autoSizeColumn(col);
			int cwidth = sheet.getColumnWidth(col);
			if (cwidth > maxColSize) {
				sheet.setColumnWidth(col, maxColSize);
			}
			if (col == 0) {
				sheet.setColumnWidth(col, firstColSize);
			}
		}

	}

	private FileOutputStream saveNextTry(String excelFileName) throws IOException {
		try {
			return new FileOutputStream(new File(excelFileName));
		} catch (FileNotFoundException e1) {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String message = e1.getMessage();
				int indexOf = message.indexOf("(");
				if (indexOf != -1) {
					message = message.substring(0, indexOf) + "\n" + message.substring(indexOf, message.length());
				}

				System.out.println("\nUnable to save file: " + message + "\n\nTry again?");
				System.out.println("y = Yes, n = No and Exit");
				String decision = input.readLine();

				switch (decision) {
				case "y":
					try {
						FileOutputStream out = new FileOutputStream(new File(excelFileName));
						return out;
					} catch (FileNotFoundException e) {
						saveNextTry(excelFileName);
					}
					break;
				case "n":
					logger.debug("Save issue - manually abort");
					System.out.println("Exit Application. The Result is lost! :(\n");
					System.exit(1);
					break;
				default:
					System.out.println("Your decision '" + decision + "' is invalid, try again!\n");
					saveNextTry(excelFileName);
				}
			}
		}

	}

	private Sheet createSheet(SXSSFWorkbook workbook) {
		String sheetName = "";
		if (isIssueReport) {
			sheetName = Constant.issueSheetName;
		} else {
			sheetName = Constant.assessSheetName;
		}
		Sheet sheet = workbook.createSheet(sheetName);
		sheet.createFreezePane(2, Constant.startTable + 1);
		createSheetInfo(sheet);
		createHeader(sheet);
		int rowCount = Constant.startTable + 1;

		if (isIssueReport) {
			for (Map.Entry<String, IssueReportOcc> entry : issueResult.entrySet()) {
				IssueReportOcc occ = entry.getValue();
				fillRow(occ, sheet, rowCount);
				rowCount++;
			}

		} else {
			for (Map.Entry<String, AssessReportOcc> entry : assessmentResult.entrySet()) {
				AssessReportOcc occ = entry.getValue();
				fillRow(occ, sheet, rowCount);
				rowCount++;
			}

		}

		return sheet;

	}

	private void createSheetInfo(Sheet sheet) {
		String typeCreteriaMsg = "";
		String numMsg = "";
		int resultSize = 0;

		if (isIssueReport) {
			typeCreteriaMsg = "Source Issue Criteria: [";
			numMsg = "Number of Issues: ";
			resultSize = issueResult.size();
		} else {
			typeCreteriaMsg = "Source Assessment Criteria: [";
			numMsg = "Number of Occurences: ";
			resultSize = assessmentResult.size();
		}

		List<String> infoList = new ArrayList<String>();
		StringBuilder creteria = new StringBuilder();
		String today = new SimpleDateFormat("EEE MMM dd HH:mm:ss zz yyyy").format(Calendar.getInstance().getTime());

		Map<String, String> orgArgMap = config.getOrgArgMap();

		// {IssueType=1, project=9345, status=2, UnUp=Up}

		for (Entry<String, String> entry : orgArgMap.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (!name.equals(Constant.cops)) {
				creteria.append(name + "=" + value + " and ");
			}

		}

		int length = creteria.length();
		int lastAnd = length - creteria.lastIndexOf(" and ");

		creteria.setLength(length - lastAnd);

		creteria.insert(0, typeCreteriaMsg);

		creteria.append("]");

		infoList.add(creteria.toString());
		infoList.add("Generated On: " + today);
		infoList.add(numMsg + resultSize);
		infoList.add(queryTime);

		for (int i = 0; i < infoList.size(); i++) {
			Row infoRow = sheet.createRow(i);
			Cell infoCell = infoRow.createCell(0);
			infoCell.setCellValue(infoList.get(i));
		}

	}

	private void createHeader(Sheet sheet) {
		Vector<String> header = null;
		if (isIssueReport) {
			header = config.getIssueDisplayHeader();
		} else {
			header = config.getAssessDisplayHeader();
		}

		// Header Row and Values:
		Cell headerCell = null;
		Row headerRow = sheet.createRow(Constant.startTable);

		for (int i = 0; i < header.size(); i++) {
			String headerName = header.get(i);

			headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerName);
			headerCell.setCellStyle(cellStyle.get("Header"));

		}

	}

	private Row fillRow(AssessReportOcc occ, Sheet sheet, int row) {
		Row dataRow = sheet.createRow(row);
		Cell dataCell = null;

		String issueIds = "";
		Vector<String> header = config.getAssessHeader();

		if (header.contains(Constant.AssessIssue)) {
			String assessmentIssueId = occ.getAssessmentIssueId();
			issueIds = assessmentIssues.get(assessmentIssueId);
		}

		for (int i = 0; i < header.size(); i++) {
			String cellValue = "";

			String cellName = header.get(i);
			String userArgValue = config.getUserArgValue(cellName);

			if (userArgValue != null) {
				cellValue = userArgValue;
			} else {
				cellValue = occ.getAssessValueMap().get(cellName);
			}

			dataCell = dataRow.createCell(i);
			dataCell.setCellValue(cellValue);
			switch (cellName) {
			case Constant.Status:
				String colorStyle = Constant.acStatusColorMap.get(cellValue);
				dataCell.setCellStyle(cellStyle.get(colorStyle));
				break;
			case Constant.AssessIssue:
				dataCell.setCellValue(issueIds);
				dataCell.setCellStyle(cellStyle.get("String"));
				break;

			default:
				dataCell.setCellStyle(cellStyle.get("String"));
				break;
			}

		}
		return dataCell.getRow();

	}

	private Row fillRow(IssueReportOcc occ, Sheet sheet, int row) {
		Row dataRow = sheet.createRow(row);
		Cell dataCell = null;
		Vector<String> header = config.getIssueHeader();
		for (int i = 0; i < header.size(); i++) {
			String cellValue = "";
			String cellName = header.get(i);
			String userArgValue = config.getUserArgValue(cellName);

			if (userArgValue != null) {
				cellValue = userArgValue;
			} else {
				cellValue = occ.getIssueValueMap().get(cellName);
			}

			dataCell = dataRow.createCell(i);
			dataCell.setCellValue(cellValue);
			switch (cellName) {
			case Constant.IssueSeverity:
				String colorStyle = Constant.issueSeverityColorMap.get(cellValue);
				dataCell.setCellStyle(cellStyle.get(colorStyle));
				break;
			case Constant.Status:
				if (cellValue == null) {
					cellValue = "closed";
					dataCell.setCellValue(cellValue);
				}
				colorStyle = Constant.issueStatusColorMap.get(cellValue);

				dataCell.setCellStyle(cellStyle.get(colorStyle));
				break;
			case Constant.IssueDateOpened:
				try {
					dataCell.setCellValue(getDateOpened(cellValue));
					dataCell.setCellStyle(cellStyle.get("Date"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			default:
				dataCell.setCellStyle(cellStyle.get("String"));
				break;
			}

		}
		return dataCell.getRow();

	}

	private Date getDateOpened(String aurosDate) throws ParseException {
		String dateOpenedPattern = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateOpenedPattern, Locale.ENGLISH);
		return simpleDateFormat.parse(aurosDate);
	}

	private void createStyles(SXSSFWorkbook workbook) {
		CellStyle style;

		Font headerFont = workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 8);
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		headerFont.setBold(true);

		Font font = workbook.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 8);
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(false);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(headerFont);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Header", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setWrapText(true);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("String", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setWrapText(false);
		style.setDataFormat(Constant.ExcelDateFormat);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Date", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Gray", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("White", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Green", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Blue", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Red", style);

		style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(Constant.SOLID_FOREGROUND);
		style.setWrapText(false);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.put("Yellow", style);

	}

}
