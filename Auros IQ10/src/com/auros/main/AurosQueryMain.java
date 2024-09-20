package com.auros.main;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.LevelRangeFilter;

import com.auros.connector.Binding;
import com.auros.credentials.AurosCredentials;
import com.auros.io.Excel;
import com.auros.io.XmlConfigParser;
import com.auros.management.AssessmentManagement;
import com.auros.management.IssueManagement;
import com.auros.management.utils.FetchIssues;
import com.auros.model.AssessIssues;
import com.auros.model.AssessResult;
import com.auros.model.Config;
import com.auros.model.Constant;
import com.auros.model.Constant.ReportType;
import com.auros.model.IssueReportType;
import com.auros.model.ValidArgs;
import com.auros.utils.LogFile;
import com.auros.utils.ProgressBarTraditional;
import com.auros.utils.StopWatch;
import com.auros.utils.Utils;
import com.emergent.e2ks.client.E2KSAppManager;
import com.emergent.e2ks.types.AurosIdentifier;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.InputOptions;
import com.emergentsys.auros.services.aurosbase.E2KSBasePortType;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;

public class AurosQueryMain {

	private boolean DEBUG = false;
	private static List<String> argList = null;
	private static String reportTypeArg = "";
	private static DecimalFormat df2 = new DecimalFormat("####.##");
	private static DecimalFormat df1 = new DecimalFormat("####");
	private String startTime;
	private String GP_ID = "";
	private String cops = "";
	private String issueType = "";

	private static Logger logger = Logger.getLogger(AurosQueryMain.class);

	public static void main(String[] args) {
		argList = Arrays.asList(args);
		reportTypeArg = argList.get(0);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new AurosQueryMain();
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
	}

	public static void disableAccessWarnings() {
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field field = unsafeClass.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			Object unsafe = field.get(null);

			Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class,
					Object.class);
			Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

			Class<?> loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
			Field loggerField = loggerClass.getDeclaredField("logger");
			Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
			putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
		} catch (Exception ignored) {
		}
	}

	public AurosQueryMain() {
	
	 	disableAccessWarnings();

		StopWatch allTimer = new StopWatch();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		startTime = format.format(new Date());
		XmlConfigParser configParser = null;

		try {

			try {
				configParser = new XmlConfigParser(reportTypeArg);
				System.out.println("using Jar File Version: " + Constant.version + "\n");
			} catch (IllegalArgumentException ie) {
				System.out.println("Report Type (First Argument): \"" + argList.get(0) + "\" is not supported!");
				System.out.println("Must be either CHECKLIST or ISSUELIST");
				System.exit(1);
			}

			Config config = configParser.getConfig();
			parseArgs(config);
			String logIdentifier = GP_ID + "~" + cops + "~" + issueType + "~";

			Map<String, FileAppender> fileAppenders = new HashMap<String, FileAppender>();
			Map<String, String> argMap = config.getArgMap();
			argMap.get(Constant.Project);

			if (argList.get(argList.size() - 2).equals("DEBUG")) {
				FileAppender debugAppender = getlog4jFileAppender(config,"DEBUG", logIdentifier, startTime);
				logger.addAppender(debugAppender);
				fileAppenders.put("DEBUG", debugAppender);
			}

			FileAppender errorAppender = getlog4jFileAppender(config,"ERROR", logIdentifier, startTime);
			fileAppenders.put("ERROR", errorAppender);
			logger.addAppender(errorAppender);

			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			AurosCredentials auth = new AurosCredentials(config, argList);
			auth.promptForCredentials(DEBUG);

			ReportType reportType = Constant.ReportType.valueOf(reportTypeArg);
			switch (reportType) {
			case ISSUELIST:
				logIdentifier = GP_ID + "~" + issueType;
				FileAppender issueAppender = getlog4jFileAppender(config,"ISSUELIST", logIdentifier, startTime);
				fileAppenders.put("ISSUELIST", issueAppender);
				logger.addAppender(issueAppender);

				List<FetchIssues> fetchIssuesThreads = new ArrayList<FetchIssues>();
				IssueManagement issueManagement = new IssueManagement(auth, fileAppenders, startTime);

				List<AurosIdentifier> searchIssuesResult = issueManagement.searchIssuesByCreteria(config);
				double searchIssuesForCriteriaTime = issueManagement.getSearchIssuesForCriteriaTime();
				if (searchIssuesResult == null) {
					String noIssueFound = "No Issue Identifiers found";
					logger.debug(noIssueFound);
					System.out.println(noIssueFound);
					searchIssuesResult = new ArrayList<AurosIdentifier>();
				}

				logger.debug(searchIssuesResult.size() + " Issue Identifiers found");
				System.out
						.println("\r" + searchIssuesResult.size() + " Issue Identifiers found");

				int chunkSize = config.getChunkSize();
				int numOfIssues = searchIssuesResult.size();
				int endSub = 0;

				int connections = 0;

				connections = Utils.mod(numOfIssues, chunkSize);
                System.out.println("chunk size is........"+chunkSize);
                System.out.println("Number of issues.........."+numOfIssues);
				if (connections > Constant.maxConnections) {

					chunkSize = Utils.mod(numOfIssues, 190);
					connections = Utils.mod(numOfIssues, chunkSize);
					System.out.println("Using Chunksize of " + chunkSize + " for maximum Connections of 190");
				}

				if (config.isChunkSizeOne()) {
					chunkSize = 1;
					System.out.println("Using Chunksize of " + chunkSize + " for debuggging");
				}

				ProgressBarTraditional progressBar = new ProgressBarTraditional(
						"Processing " + numOfIssues + " Issues using " + connections + " parallel connections");
				progressBar.start();

				StopWatch fetchIssuesTimer = new StopWatch();
				if (numOfIssues > chunkSize) {
					for (int i = 0; i < numOfIssues; i = i + chunkSize) {
						if (numOfIssues > i + chunkSize) {
							endSub = i + chunkSize;
						} else {
							endSub = numOfIssues;
						}
						List<AurosIdentifier> subIssueResult = searchIssuesResult.subList(i, endSub);
						issueManagement.getIssues(subIssueResult, config, fetchIssuesThreads);
					}
				} else {
					issueManagement.getIssues(searchIssuesResult, config, fetchIssuesThreads);
				}

				for (FetchIssues fetchIssuesThread : fetchIssuesThreads) {
					try {
						fetchIssuesThread.join();
					} catch (InterruptedException e) {
						String endTime = format.format(new Date());
						logger.error(e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + endTime + ";"
								+ "CHECKLIST");
						e.printStackTrace();
						System.exit(1);
					}
				}

				double fetchIssuesTime = ((double) fetchIssuesTimer.getElapsedTime());

				progressBar.showProgress = false;

				progressBar = new ProgressBarTraditional("Getting Issue Attributes");
				progressBar.start();

				StopWatch issueAttrTimer = new StopWatch();
				issueManagement.getIssueAttr(issueManagement.getIssueList(), config);
				double issueAttrTime = ((double) issueAttrTimer.getElapsedTime());

				progressBar.showProgress = false;

				logger.debug("Saving Results...");
				System.out.println("\n\nSaving Results...                                    ");

				double aurosTime = ((double) allTimer.getElapsedTime()) / 1000;
				String queryTime = "Auros Issue Query RunTime: " + df1.format(aurosTime) + " sec";

				IssueReportType issueResult = issueManagement.getIssueResult();

				StopWatch excelTimer = new StopWatch();
				Excel excel = new Excel(config, issueResult, queryTime, fileAppenders, startTime);
				excel.writeExcel(Constant.ReportType.ISSUELIST);
				double excelIssueTime = ((double) excelTimer.getElapsedTime());

				int totalNumOfIssues = issueResult.size();

				double runTime = ((double) allTimer.getElapsedTime()) / 60000;
				df2.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
				String runTimeMin = df2.format(runTime);
				String totalTime = "Overall RunTime: " + runTimeMin + " min";
				System.out.println(totalTime);

				String endTime = format.format(new Date());
				
				// "TotalIssues;SearchIssuesForCriteriaTime;FetchIssuesTime;IssueAttrTime;ExcelTime;runTimMin;startTime;EndTime;InputType"

				logger.info(totalNumOfIssues + ";" + searchIssuesForCriteriaTime + " MilliSec;" + fetchIssuesTime
						+ " MilliSec;" + issueAttrTime + " MilliSec;" + excelIssueTime + " MilliSec;" + runTimeMin + " Min;"
						+ startTime + ";" + endTime + ";" + "ISSUELIST");
				System.out.println(totalTime);

				break;
			case CHECKLIST:
				logIdentifier = GP_ID + "~" + cops;
				FileAppender checklistAppender = getlog4jFileAppender(config,"CHECKLIST", logIdentifier, startTime);
				fileAppenders.put("CHECKLIST", checklistAppender);
				logger.addAppender(checklistAppender);

	
				AssessmentManagement assessmentManagement = new AssessmentManagement(auth, fileAppenders, startTime);

				assessmentManagement.searchAssessmentsByCreteria(config);
				double searchAurosIdentTime = assessmentManagement.getSearchAurosIdentTime();
				double searchAurosIssuesTime = assessmentManagement.getSearchAurosIssuesTime();
				double fetchAssOccesTime = assessmentManagement.getFetchAssOccesTime();
				double processAssOccesTime = assessmentManagement.getProcessOccesTime();
				
				//System.out.println(searchAurosIdentTime+" "+searchAurosIssuesTime+" "+fetchAssOccesTime+" "+processAssOccesTime);

				System.out.println(" ");
				logger.debug("Processing AC Attributes");
				progressBar = new ProgressBarTraditional("Processing AC Attributes");
				progressBar.start();
				StopWatch processACAttrTimer = new StopWatch();
				assessmentManagement.setAcAttr(config);
				double processACAttrTime = ((double) processACAttrTimer.getElapsedTime());
				progressBar.showProgress = false;

				System.out.println(" ");
				logger.debug("Processing SE Attributes");
				progressBar = new ProgressBarTraditional("Processing SE Attributes");
				progressBar.start();
				StopWatch processSEAttrTimer = new StopWatch();
				assessmentManagement.setAcSEAttr(config);
				double processSEAttrTime = ((double) processSEAttrTimer.getElapsedTime());
				progressBar.showProgress = false;

				System.out.println(" ");
				logger.debug("Processing EE Attributes");
				progressBar = new ProgressBarTraditional("Processing EE Attributes");
				progressBar.start();
				StopWatch processEEAttrTimer = new StopWatch();
				assessmentManagement.setAcEEAttr(config);
				double processEEAttrTime = ((double) processEEAttrTimer.getElapsedTime());
				progressBar.showProgress = false;

				System.out.println(" ");
				logger.debug("Processing AC Header Attributes");
				progressBar = new ProgressBarTraditional("Processing AC Header Attributes");
				progressBar.start();
				StopWatch processACHeaderTimer = new StopWatch();
				assessmentManagement.setAcHeaderAttr(config);
				double processACHeaderTime = ((double) processACHeaderTimer.getElapsedTime());
				progressBar.showProgress = false;

				StopWatch processKpacAttrTimer = new StopWatch();
				if (config.hasKpacAttr()) {
					System.out.println(" ");
					assessmentManagement.setKPacAttr(config);
				}
				double processKpacAttrTime = ((double) processKpacAttrTimer.getElapsedTime());

				logger.debug("Saving Results");
				System.out.println("\n\nSaving Results...                                    ");

				AssessResult assessmentResult = assessmentManagement.getAssessmentResult();
				AssessIssues assessmentIssues = assessmentManagement.getAssessmentIssues();

				aurosTime = ((double) allTimer.getElapsedTime()) / 1000;
				queryTime = "Auros Issue Query RunTime: " + df1.format(aurosTime) + " sec";

				StopWatch excelAssTimer = new StopWatch();
				excel = new Excel(config, assessmentResult, assessmentIssues, queryTime, fileAppenders, startTime);
				excel.writeExcel(Constant.ReportType.CHECKLIST);
				double excelAssTime = ((double) excelAssTimer.getElapsedTime());

				int totalNumOfAss = assessmentResult.size();
				int uniqueNumOfAss = assessmentManagement.getNumOfAssessments();
				int numOfKpacs = assessmentManagement.getNumOfKpacs();

				runTime = ((double) allTimer.getElapsedTime()) / 60000;
				runTimeMin = df2.format(runTime);
				totalTime = "Overall RunTime: " + runTimeMin + " min";

				endTime = format.format(new Date());

				logger.info(totalNumOfAss + ";" + uniqueNumOfAss + ";" + numOfKpacs + ";" + searchAurosIdentTime
						+ " MilliSec;" + searchAurosIssuesTime + " MilliSec;" + fetchAssOccesTime + " MilliSec;"
						+ processAssOccesTime + " MilliSec;" + processACAttrTime + " MilliSec;" + processSEAttrTime
						+ " MilliSec;" + processEEAttrTime + " MilliSec;" + processACHeaderTime + " MilliSec;"
						+ processKpacAttrTime + " MilliSec;" + excelAssTime + " MilliSec;" + runTimeMin + " Min;"
						+ startTime + ";" + endTime + ";" + "CHECKLIST");
				System.out.println(totalTime);

				break;

			default:
				break;
			}

		} catch (Exception e) {
			double runTime = ((double) allTimer.getElapsedTime()) / 60000;
			String totalTime = "Overall RunTime until Error: " + df2.format(runTime) + " min";
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "GENERIC");
			System.out.println(totalTime);
			System.out.println("Failed now after second try with:\n" + e.getMessage());
			System.exit(1);
		}

	}

	/**
	 * Parse Command Arguments to Application
	 * 
	 * @param config
	 */
	private void parseArgs(Config config) {

		Map<String, ValidArgs> validArgMap = config.getValidArgMap();
		ReportType reportType = Constant.ReportType.valueOf(reportTypeArg);

		int paramStart = config.getParamStart();
		int paramEnd = (argList.size() - 1);
		boolean userParam = false;
		int headerSize = 999;

		for (int i = paramStart; i < paramEnd; i++) {

			String arg = argList.get(i);
			String[] argPair = arg.split(Constant.EQUAL);

			if (argPair.length != 2)
				continue;

			String name = argPair[0];
			String value = argPair[1];

			if (name.equals("suffix")) {
				config.setFileSuffix(value);
				continue;

			}

			if (name.equalsIgnoreCase("chunksize")) {
				config.setChunkSize(value);
				continue;

			}

			if (name.equalsIgnoreCase("chunksizeKpac")) {
				config.setChunkSizeKpac(value);
				continue;
			}

			if (name.equalsIgnoreCase("chunksizeOne")) {
				config.setChunkSizeOne(true);
				continue;
			}

			if (name.equalsIgnoreCase("GP_ID")) {
				userParam = true;
				GP_ID = value;
			}

			if (name.equalsIgnoreCase("UnUp")) {
			}

			if (name.equalsIgnoreCase("cops")) {
				cops = value;
			}
			if (name.equalsIgnoreCase("IssueType")) {
				issueType = value;
			}

			if (name.equalsIgnoreCase("Manual")) {
				userParam = true;
			}

			if (userParam) {
				switch (reportType) {
				case CHECKLIST:
					headerSize = config.getAssessDisplayHeader().size();
					config.addAssessDisplayHeader(headerSize, name);
					config.addAssessHeader(headerSize, name);
					config.addUserArg(name, value);
					userParam = false;
					break;

				case ISSUELIST:
					headerSize = config.getIssueDisplayHeader().size();
					config.addIssueDisplayHeader(headerSize, name);
					config.addIssueHeader(headerSize, name);
					config.addUserArg(name, value);
					userParam = false;
					break;

				default:
					break;
				}
				continue;
			}

			if (validArgMap.containsKey(name)) {
				config.addOrgArgMap(name, value);
				config.setArgMap(name, value);

			} else {
				String wrongArg = "Argument: " + name + " is not suported!";
				System.out.println(wrongArg);
				System.exit(1);
			}

		}

		switch (reportType) {
		case CHECKLIST:
			for (int i = 0; i < Constant.checklistParams.size(); i++) {
				String paramPart = config.getArgMap().get(Constant.checklistParams.get(i));
				if (paramPart == null) {
					paramPart = "";
				}
				config.getParamString().add(paramPart);
			}
			break;
		case ISSUELIST:
			for (int i = 0; i < Constant.issuelistParams.size(); i++) {
				String paramPart = config.getArgMap().get(Constant.issuelistParams.get(i));
				if (paramPart == null) {
					paramPart = "";
				}
				config.getParamString().add(paramPart);
			}
			break;
		default:
			break;
		}

		String outputDirName = argList.get(argList.size() - 1);
		File outputDir = new File(outputDirName);
		if (outputDir.exists() && outputDir.canWrite()) {
			config.setOutputDir(outputDirName);
		} else {
			String wrongDir = "Directory does not exist or no write access to:" + outputDir;
			System.out.println(wrongDir);
			System.exit(1);
		}

		if (config.getFileSuffix().isEmpty()) {
			String missingSuf = "Missing \"suffix\" argument for Filename: " + config.getFileSuffix();
			System.out.println(missingSuf);
			System.exit(1);
		}
	}

	@SuppressWarnings("unused")
	private boolean isUserAuthenticated(AurosCredentials auth, Binding binding, Config config) {
		boolean isAuthenticated = false;
		E2KSBasePortType e2KSBasePortType = binding.initBasePortAndCookie(config);
		E2KSAppManager e2KSAppManager = new E2KSAppManager(e2KSBasePortType);
		List<InputOptions> inputOptions = new ArrayList<InputOptions>();
		AurosMessages aurosMessages = new AurosMessages();
		try {
			isAuthenticated = e2KSAppManager.authenticateUser(auth.getUserName(), auth.getUserPw(), inputOptions,
					aurosMessages);
		} catch (Exception e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "CHECKLIST");
			return false;
		}

		return isAuthenticated;

	}

	private FileAppender getlog4jFileAppender(Config config, String type, String params, String startTime) {
		FileAppender fileAppender = null;
		LogFile logFile = null;
		PatternLayout patternLayout = null;
		LevelRangeFilter lvlFilter = new LevelRangeFilter();
		lvlFilter.setAcceptOnMatch(true);

		switch (type) {
		case "CHECKLIST":
			logFile = new LogFile(config, type, params);
			patternLayout = new PatternLayout() {
				@Override

				public String getHeader() {
					return "TotalCheck;UniqueAssesmentID;UniqueKPAC;AurosIdentSearchTime;AurosAssesIssueSearchTime;FetchAssOccesTime;"
							+ "ProcessAssOccesTime;ProcessACAttrTime;ProcessSEAttrTime;ProcessEEAttrTime;ProcessACHeaderTime;ProcessKpacAttrTime;"
							+ "ExcelProcessTime;RunTimMin;startTime;EndTime;InputType" + System.getProperty("line.separator");
				};

			};

			lvlFilter.setLevelMin(Level.INFO);
			lvlFilter.setLevelMax(Level.INFO);

			break;
		case "ISSUELIST":
			logFile = new LogFile(config,type, params);
			patternLayout = new PatternLayout() {
				@Override
				public String getHeader() {
					return "TotalIssues;SearchIssuesForCriteriaTime;FetchIssuesTime;IssueAttrTime;ExcelTime;runTimMin;startTime;EndTime;InputType"
							+ System.getProperty("line.separator");
				};
			};

			lvlFilter.setLevelMin(Level.INFO);
			lvlFilter.setLevelMax(Level.INFO);

			break;
		case "ERROR":
			logFile = new LogFile(config,type, params);
			patternLayout = new PatternLayout() {
				@Override
				public String getHeader() {
					return "Error Description;StartTime;EndTime;InputType" + System.getProperty("line.separator");
				};
			};
			lvlFilter.setLevelMin(Level.ERROR);
			lvlFilter.setLevelMax(Level.ERROR);
			break;
		case "DEBUG":
			logFile = new LogFile(config,type, params);
			patternLayout = new PatternLayout(("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %c{1}:%L - %m%n"));
			lvlFilter.setLevelMin(Level.DEBUG);
			lvlFilter.setLevelMax(Level.DEBUG);
			break;

		default:
			break;
		}

		try {
			String logFileName = logFile.getLogFileName();
			fileAppender = new FileAppender(patternLayout, logFileName, false);
			fileAppender.addFilter(lvlFilter);

		} catch (IOException e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "CHECKLIST");
			e.printStackTrace();
			System.exit(1);
		}
		return fileAppender;

	}

}
