package com.auros.model;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Vector;

/**
 * @author FNAUROT1
 *
 */
public class Config {

	private String cookie = "";

	private String wslURL = "";
	private String assessmentURL = "";
	private String issuesURL = "";
	private String baseURL = "";

	private Map<String, ValidArgs> validArgMap = new HashMap<String, ValidArgs>();

	private Map<String, String> argMap = new HashMap<String, String>();
	private Map<String, String> orgArgMap = new HashMap<String, String>();
	private Map<String, String> aurosServices = new HashMap<String, String>();

	private Vector<String> assessHeader;
	private Vector<String> issueHeader;

	private Vector<String> assessDisplayHeader;
	private Vector<String> issueDisplayHeader;



	private String outputDir = "";
	private String logDir = "";
	private String status = "";

	private boolean promptForCredentials = true;
	private int paramStart = 1;
	private int paramEnd = 1;
	private String fileSuffix = "";

	private Map<String,String> userArg = new HashMap<String,String>();

	private int chunkSize = 1;
	private int chunkSizeKpac = 1;
	
	private StringJoiner paramString = new StringJoiner("~");
	
	private boolean chunkSizeOne=false;



	public Config() {

	}
	
	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(String chunkSize) {
		this.chunkSize = Integer.parseInt(chunkSize);
	}

	
	public String getUserArgValue(String name) {
		return userArg.get(name);
	}
	
	public void addUserArg(String name, String value) {
		userArg.put(name, value);
	}


	public int getParamStart() {
		return paramStart;
	}

	public void setParamStart(int paramStart) {
		this.paramStart = paramStart;
	}

	public int getParamEnd() {
		return paramEnd;
	}

	public void setParamEnd(int paramEnd) {
		this.paramEnd = paramEnd;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public boolean doPromptForCredentials() {
		return promptForCredentials;
	}

	public void setPromptForCredentials(boolean promptForCredentials) {
		this.promptForCredentials = promptForCredentials;
	}

	public Map<String, String> getOrgArgMap() {
		return orgArgMap;
	}

	public void addOrgArgMap(String key, String value) {
		this.orgArgMap.put(key, value);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean hasKpacAttr() {
		boolean hasKPac = false;
		if (assessHeader.contains(Constant.Type) || assessHeader.contains(Constant.Title) || assessHeader.contains(Constant.AdditionalInformation)
				|| assessHeader.contains(Constant.Justification) || assessHeader.contains(Constant.Description) || assessHeader.contains(Constant.Author)) {

			hasKPac = true;
		}

		return hasKPac;
	}

	public boolean carryOverKpac(AssessReportOcc prevOcc, AssessReportOcc crrntOcc) {
		boolean isProcessed = false;

		if (prevOcc == null) {
			return isProcessed;
		}

		Map<String, String> prevValueMap = prevOcc.getAssessValueMap();
		Map<String, String> crrntValueMap = crrntOcc.getAssessValueMap();

		String type = prevValueMap.get(Constant.Type);
		if (type != null) {
			crrntValueMap.put(Constant.Type, type);
			isProcessed = true;
		}

		String addInfo = prevValueMap.get(Constant.AdditionalInformation);
		if (addInfo != null) {
			crrntValueMap.put(Constant.AdditionalInformation, addInfo);
			isProcessed = true;
		}

		String just = prevValueMap.get(Constant.Justification);
		if (just != null) {
			crrntValueMap.put(Constant.Justification, just);
			isProcessed = true;
		}

		String desc = prevValueMap.get(Constant.Description);
		if (desc != null) {
			crrntValueMap.put(Constant.Description, desc);
			isProcessed = true;
		}

		return isProcessed;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public Vector<String> getAssessDisplayHeader() {
		if (assessDisplayHeader == null) {
			assessDisplayHeader = new Vector<String>();
		}
		return assessDisplayHeader;
	}

	public Vector<String> getIssueDisplayHeader() {
		if (issueDisplayHeader == null) {
			issueDisplayHeader = new Vector<String>();
		}
		return issueDisplayHeader;
	}

	/**
	 * Issue Attributes (display name) Vector in order
	 * 
	 * @param pos
	 * @param name
	 */
	public void addIssueDisplayHeader(int pos, String name) {
		if (this.issueDisplayHeader == null) {
			this.issueDisplayHeader = new Vector<String>();
		}
		if (issueDisplayHeader.size() < pos) {
			issueDisplayHeader.setSize(pos);
		}
		this.issueDisplayHeader.add(pos, name);
	}

	/**
	 * Assessment Attributes (display name) Vector in order
	 * 
	 * @param pos
	 * @param name
	 */
	public void addAssessDisplayHeader(int pos, String name) {
		if (this.assessDisplayHeader == null) {
			this.assessDisplayHeader = new Vector<String>();
		}
		if (assessDisplayHeader.size() < pos) {
			assessDisplayHeader.setSize(pos);
		}
		this.assessDisplayHeader.add(pos, name);
	}

	public Vector<String> getAssessHeader() {
		if (assessHeader == null) {
			assessHeader = new Vector<String>();
		}
		return assessHeader;
	}

	public Vector<String> getIssueHeader() {
		if (issueHeader == null) {
			issueHeader = new Vector<String>();
		}
		return issueHeader;
	}

	/**
	 * Issue Attributes (internal name) Vector in order
	 * 
	 * @param pos
	 * @param name
	 */
	public void addIssueHeader(int pos, String name) {
		if (this.issueHeader == null) {
			this.issueHeader = new Vector<String>();
		}
		if (issueHeader.size() < pos) {
			issueHeader.setSize(pos);
		}
		this.issueHeader.add(pos, name);
	}

	/**
	 * Assessment Attributes (internal name) Vector in order
	 * 
	 * @param pos
	 * @param name
	 */
	public void addAssessHeader(int pos, String name) {
		if (this.assessHeader == null) {
			this.assessHeader = new Vector<String>();
		}
		if (assessHeader.size() < pos) {
			assessHeader.setSize(pos);
		}
		this.assessHeader.add(pos, name);
	}

	public Map<String, ValidArgs> getValidArgMap() {
		return validArgMap;
	}

	public void addValidArgMap(String key, ValidArgs value) {
		this.validArgMap.put(key, value);
	}

	public Map<String, String> getArgMap() {
		return argMap;
	}

	public void setArgMap(String key, String value) {
		this.argMap.put(key, value);
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public Map<String, String> getAurosServices() {
		return aurosServices;
	}

	public void setAurosServices(Map<String, String> aurosServices) {
		this.aurosServices = aurosServices;
	}

	public String getWslURL() {
		return wslURL;
	}

	public void setWslURL(String wslURL) {
		this.wslURL = wslURL;
	}

	public String getAssessmentURL() {
		return assessmentURL;
	}

	public void setAssessmentURL(String assessmentURL) {
		this.assessmentURL = assessmentURL;
	}

	public String getIssuesURL() {
		return issuesURL;
	}

	public void setIssuesURL(String issuesURL) {
		this.issuesURL = issuesURL;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public int getChunkSizeKpac() {
		return chunkSizeKpac;
	}

	public void setChunkSizeKpac(String chunkSizeKpac) {
		this.chunkSizeKpac =  Integer.parseInt(chunkSizeKpac);
	}

	public StringJoiner getParamString() {
		return paramString;
	}

	public boolean isChunkSizeOne() {
		return chunkSizeOne;
	}

	public void setChunkSizeOne(boolean chunkSizeOne) {
		this.chunkSizeOne = chunkSizeOne;
	}


}
