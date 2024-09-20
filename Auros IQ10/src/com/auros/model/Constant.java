package com.auros.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {

	public static final String version = "2.6.1 IQ10";
	public static final int maxConnections = 190;

	public static final String assessSheetName = "Auros Assessment Report";
	public static final String issueSheetName = "Auros Issues Report";

	public static final String logDir = "AurosLog";
	public static final String logfilePrefix = "ReportLog";
	public static final String logfileSuffix = ".log";

	public static final String passwordDef = "passwd";

	public static final File configFile = new File(System.getProperty("user.dir") + File.separator + "Config.xml");
	public static final String Project = "Project";

	public static final String ChkListFilePrefix = "Checklist_";
	public static final String ChkList = "ChkList";

	public static final String IssueListPrefix = "Issue_";
	public static final String IssueList = "IssueList";

	public static final String wslXPath = "/Config/Ford/Service/@URL";
	public static final String aurosServiceXPath = "/Config/Auros/Service";

	public static final String argXPathCheckList = "/Config/ValidArgs/CheckList/Arg";
	public static final String argXPathIssueList = "/Config/ValidArgs/IssueList/Arg";
	public static final String argXPathLogDir = "/Config/@LogDir";
	public static final String argXPathIssueChunkSize = "/Config/IssueReport/@ChunkSize";
	public static final String argXPathPromptForCredentials = "/Config/Auros/@PromptForCredentials";

	public static final String assessReportAttrXPath = "/Config/AssessmentReport/Attr";
	public static final String issueReportAttrXPath = "/Config/IssueReport/Attr";

	public static final String assessmentService = "E2KSAssessmentsService";
	public static final String issuesService = "E2KSIssuesService";
	public static final String baseService = "E2KSBaseService";

	public static final String assessmentServiceNs = "http://auros.emergentsys.com/services/assessments";
	public static final String issuesServiceNs = "http://auros.emergentsys.com/services/issues";
	public static final String baseServiceNs = "http://auros.emergentsys.com/services/aurosbase";

	public static final String cookieCredentials = "WSL-credential=";
	public static final String cookie = "Cookie";

	public static final String COMMA = ",";
	public static final String DASH = "-";
	public static final String EQUAL = "=";

	public static final int startTable = 5;

	// Generic Attributes
	public static final String cops = "cops";
	public static final String AssessmentId = "Assessment ID";
	public static final String KPACId = "K-PAC";
	public static final String AssessIssue = "All Issues";

	// KPAC Attributes
	public static final String Program = "Program";
	public static final String Author = "Author";
	public static final String Title = "Title";
	public static final String Level = "Level";
	public static final String Type = "Type";
	public static final String AdditionalInformation = "Additional Information";
	public static final String Justification = "Justification";
	public static final String Description = "Description";
	public static final String Status = "Status";
	public static final String status = "status";
	public static final String StatusColor = "StatusColor";

	// AC Header Attributes
	public static final String OperationProcess = "Operation / Process";
	public static final String Timing = "Timing";
	public static final String AssessmentDescriptor = "Assessment Descriptor";
	public static final String Creator = "Creator";
	public static final String Evaluator = "Evaluator";
	public static final String LastModifiedOn = "Last Modified On";
	public static final String LastModifiedBy = "Last Modified By";

	// Issue Attributes
	public static final String IssueID = "ID";
	public static final String IssueDescription = "Issue Description";
	public static final String IssueAddInfo = "Additional Information";
	public static final String IssueSeverity = "Severity";
	public static final String IssueTargetClosureDate = "Target Closure Date";
	public static final String IssueDiscussion = "Discussion";
	public static final String IssueAction = "Action";
	public static final String IssueDateClosed = "Date Closed";
	public static final String IssueCreator = "Creator";
	public static final String IssueDateOpened = "Date Opened";
	public static final String IssueChampion = "Champion";
	public static final String IssueTeamMembers = "Team Members";
	public static final String IssueCheckListID = "Checklist ID";
	public static final String IssueKPACID = "K-PAC ID";
	public static final String IssueType = "Issue Type";
	public static final String IssueLastModified = "Date Last Modified";
	public static final String IssueLastModifyUser = "Last Modified By";
	public static final String IssueAddIssueInfo = "Issue Additional Information";
	public static final String IssueProgram = "Program";

	public static final short ExcelDateFormat = 14;
	public static final short SOLID_FOREGROUND = 1;
	public static final int ExcelFlushSize = 100;

	public static final Map<String, String> issueSeverityTextMap = new HashMap<String, String>();
	static {
		issueSeverityTextMap.put("1", "5 - Major");
		issueSeverityTextMap.put("2", "3 - Significant");
		issueSeverityTextMap.put("3", "1 - Minor");

	}

	public static final Map<String, String> issueSeverityColorMap = new HashMap<String, String>();
	static {
		issueSeverityColorMap.put("5 - Major", "Red");
		issueSeverityColorMap.put("3 - Significant", "Yellow");
		issueSeverityColorMap.put("1 - Minor", "White");

	}

	public static enum ReportType {
		CHECKLIST("Checklist_"), ISSUELIST("Issue_");
		String prefix;

		ReportType(String prefix) {
			this.prefix = prefix;
		}

		public String prefix() {
			return prefix;
		}
	}

	public static final Map<Integer, String> issueStatusTextMap = new HashMap<Integer, String>();
	static {
		issueStatusTextMap.put(1, "open");
		issueStatusTextMap.put(2, "pending");
		issueStatusTextMap.put(5, "closed");

	}

	public static final Map<String, String> issueStatusColorMap = new HashMap<String, String>();
	static {
		issueStatusColorMap.put("open", "Red");
		issueStatusColorMap.put("pending", "Yellow");
		issueStatusColorMap.put("closed", "Green");

	}

	public static final Map<Integer, String> acStatusTextMap = new HashMap<Integer, String>();
	static {
		acStatusTextMap.put(0, "Non Scorable");
		acStatusTextMap.put(1, "Not Evaluated");
		acStatusTextMap.put(2, "Green - Compliant");
		acStatusTextMap.put(3, "Carry Over - Conforms");
		acStatusTextMap.put(4, "Blue - Cannot Assess");
		acStatusTextMap.put(5, "Red - Not Compliant");
		acStatusTextMap.put(6, "Design Pending");
		acStatusTextMap.put(7, "Open");
		acStatusTextMap.put(8, "NA - Not Applicable");
		acStatusTextMap.put(9, "Kbe Conformance");
		acStatusTextMap.put(10, "Kbe Non Conformance");
		acStatusTextMap.put(11, "Kbe Non Conformance Accepted");

	}

	public static final Map<String, String> acStatusColorMap = new HashMap<String, String>();

	static {
		acStatusColorMap.put("Non Scorable", "Gray");
		acStatusColorMap.put("Not Evaluated", "White");
		acStatusColorMap.put("Green - Compliant", "Green");
		acStatusColorMap.put("Carry Over - Conforms", "Green");
		acStatusColorMap.put("Blue - Cannot Assess", "Blue");
		acStatusColorMap.put("Red - Not Compliant", "Red");
		acStatusColorMap.put("Design Pending", "Blue");
		acStatusColorMap.put("Open", "Blue");
		acStatusColorMap.put("NA - Not Applicable", "Gray");
		acStatusColorMap.put("Kbe Conformance", "Green");
		acStatusColorMap.put("Kbe Non Conformance", "Red");
		acStatusColorMap.put("Kbe Non Conformance Accepted", "Red");
	}
	
	public static final List<String> checklistParams = new ArrayList<String>();
	
	static {
		checklistParams.add("project");
		checklistParams.add("cops");
		checklistParams.add("UnUp");
		checklistParams.add("Vehicle Line");
	}
	
	public static final List<String> issuelistParams = new ArrayList<String>();
	
	static {
		issuelistParams.add("project");
		issuelistParams.add("UnUp");
		issuelistParams.add("IssueType");
		issuelistParams.add("Vehicle Line");
	}

}
