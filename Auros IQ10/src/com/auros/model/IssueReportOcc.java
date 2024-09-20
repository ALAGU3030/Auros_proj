package com.auros.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.emergent.e2ks.types.asses.Occurrence;

public class IssueReportOcc {

	private Occurrence issuementOcc = null;

	// issuement Attributes (internal name) Vector in order
	private Vector<String> issueHeader;

	// issuement Attribute Values (internal name - value)
	private Map<String, String> issueValueMap = new HashMap<String, String>();

	public IssueReportOcc(Config config) {
		this.issueHeader = config.getIssueHeader();
	}

	public Occurrence getIssuementOcc() {
		return issuementOcc;
	}

	public void setIssuementOcc(Occurrence issuementOcc) {
		this.issuementOcc = issuementOcc;
	}

	public Map<String, String> getIssueValueMap() {
		return issueValueMap;
	}

	public void addIssueValue(String name, String value) {
		this.issueValueMap.put(name, value);
	}


	public Vector<String> getIssueHeader() {
		return issueHeader;
	}

}
