package com.auros.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.auros.model.Config;
import com.auros.model.Constant;

public class LogFile {

	private String logFileName = "";

	public LogFile(Config config, String type, String params) {
		
		String outputDir = config.getLogDir();

		String logDirName = outputDir  + File.separator;
		File logDir = new File(logDirName);
		if (!logDir.exists()) {
			logDir.mkdir();
		}

		logFileName = logDirName + type + "~"+ params + "~"+ getDateTime() + Constant.logfileSuffix;
		logFileName = logFileName.replaceAll("~~~", "~");
		logFileName = logFileName.replaceAll("~~", "~");
		System.out.println();

	}

	private String getDateTime() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return dateFormat.format(date);
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

}
