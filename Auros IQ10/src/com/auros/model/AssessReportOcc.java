package com.auros.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.emergent.e2ks.types.asses.Occurrence;

public class AssessReportOcc {

	private String kpacKey;

	private String assessmentIssueId = "";

	private Occurrence assessmentOcc = null;

	// Assessment Attribute Map (internal name - displayed name)
	private Map<String, String> assessAttrMap = new HashMap<String, String>();

	// Assessment Attributes (internal name) Vector in order
	private Vector<String> assessHeader;

	// Assessment Attribute Values (internal name - value)
	private Map<String, String> assessValueMap = new HashMap<String, String>();

	public AssessReportOcc(Config config) {
		this.assessHeader = config.getAssessHeader();
	}

	public String getAssessmentIssueId() {
		return assessmentIssueId;
	}

	public void setAssessmentIssueId(String assessmentIssueId) {
		this.assessmentIssueId = assessmentIssueId;
	}

	public Occurrence getAssessmentOcc() {
		return assessmentOcc;
	}

	public void setAssessmentOcc(Occurrence assessmentOcc) {
		this.assessmentOcc = assessmentOcc;
	}

	public Map<String, String> getAssessValueMap() {
		return assessValueMap;
	}

	public void setAssessValueMap(Map<String, String> assessValueMap) {
		this.assessValueMap = assessValueMap;
	}

	public void setAssessValue(String name, String value) {
		this.assessValueMap.put(name, value);
	}

	public boolean hasAssessValue(String name) {
		if (this.assessValueMap.get(name) != null) {
			return true;
		}
		return false;
	}

	public Map<String, String> getAssessAttrMap() {
		return assessAttrMap;
	}

	public Vector<String> getAssessHeader() {
		return assessHeader;
	}

	public String getKpacKey() {
		return kpacKey;
	}

	public void setKpacKey(String kpacKey) {
		this.kpacKey = kpacKey;
	}

}
