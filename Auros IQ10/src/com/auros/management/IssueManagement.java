package com.auros.management;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.ws.Holder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

import com.auros.connector.Binding;
import com.auros.credentials.AurosCredentials;
import com.auros.management.utils.FetchIssues;
import com.auros.model.Config;
import com.auros.model.Constant;
import com.auros.model.IssueReportOcc;
import com.auros.model.IssueReportType;
import com.auros.model.ValidArgs;
import com.auros.utils.JulianDate;
import com.auros.utils.ProgressBarTraditional;
import com.auros.utils.StopWatch;
import com.emergent.e2ks.types.AurosIdentifier;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.Discussion;
import com.emergent.e2ks.types.DiscussionData;
import com.emergent.e2ks.types.E2KSAttribute;
import com.emergent.e2ks.types.InputOptions;
import com.emergent.e2ks.types.KPac;
import com.emergent.e2ks.types.KPacHeader;
import com.emergent.e2ks.types.asses.AssessmentHeader;
import com.emergent.e2ks.types.issue.ExtendedElement;
import com.emergent.e2ks.types.issue.History;
import com.emergent.e2ks.types.issue.HistoryInstance;
import com.emergent.e2ks.types.issue.Issue;
import com.emergent.e2ks.types.issue.IssueAssignees;
import com.emergent.e2ks.types.issue.IssueChapter;
import com.emergent.e2ks.types.issue.IssueInterestedParties;
import com.emergentsys.auros.services.assessments.E2KSAssessmentsPortType;
import com.emergentsys.auros.services.assessments.E2KSFault_Exception;
import com.emergentsys.auros.services.aurosbase.E2KSBasePortType;
import com.emergentsys.auros.services.issues.E2KSIssuesPortType;

public class IssueManagement {

	private E2KSIssuesPortType e2KSIssuesPortType = null;
	private E2KSBasePortType e2KSBasePortType = null;
	private E2KSAssessmentsPortType e2KSAssessmentsPortType = null;
	private IssueReportType allIssuesMap = new IssueReportType();
	private Binding binding = null;
	private List<Issue> issueList = new ArrayList<Issue>();
	private Logger logger = Logger.getLogger(IssueManagement.class);

	private Map<String, FileAppender> fileAppenders = null;
	private String startTime = null;
	private double searchIssuesForCriteriaTime = 0.0;

	public double getSearchIssuesForCriteriaTime() {
		return searchIssuesForCriteriaTime;
	}

	public IssueManagement(AurosCredentials auth, Map<String, FileAppender> fileAppenders, String startTime) {
		this.startTime = startTime;
		this.fileAppenders = fileAppenders;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
		binding = new Binding(auth, fileAppenders);
	}

	public List<Issue> getIssueList() {
		return issueList;
	}

	public void addToIssueList(List<Issue> issueSubList) {
		issueList.addAll(issueSubList);
	}

	public IssueReportType getIssueResult() {
		return allIssuesMap;
	}

	public List<AurosIdentifier> searchIssuesByCreteria(Config config) throws Exception {

		if (e2KSIssuesPortType == null) {
			e2KSIssuesPortType = binding.initIssuesPortAndCookie(config);
		}

		List<E2KSAttribute> searchAttributes = new ArrayList<E2KSAttribute>();
		Holder<List<AurosIdentifier>> searchResults = new Holder<List<AurosIdentifier>>();
		List<InputOptions> options = new ArrayList<InputOptions>();
		Holder<AurosMessages> messages = null;

		E2KSAttribute attrib = null;
		Map<String, String> argMap = config.getArgMap();
		Map<String, ValidArgs> validArgMap = config.getValidArgMap();
		//System.out.println(argMap);
		
		for (Map.Entry<String, String> entry : argMap.entrySet()) {
			attrib = new E2KSAttribute();

			String name = entry.getKey();
			String value = entry.getValue();
			
		
			
			ValidArgs validArgsByType = validArgMap.get(name);
			if (validArgsByType == null) {
				throw new Exception("Invalid argument found : " + name);
			}
			String type = validArgsByType.getType();
			Map<String, String> valueMap = validArgsByType.getValueMap();

			attrib.setAttributeName(name);
			attrib.setAttributeType(type);

			int multiValueSep = value.indexOf(Constant.COMMA);
			if (multiValueSep == -1) {
				String valMap = valueMap.get(value);
				if (valMap != null) {
					attrib.getAttributeValue().add(valMap);
				} else {
					attrib.getAttributeValue().add(value);
				}

			} else {
				String[] multiValArray = value.split(Constant.COMMA);
				for (int i = 0; i < multiValArray.length; i++) {
					String valMap = valueMap.get(multiValArray[i]);
					if (valMap != null) {
						attrib.getAttributeValue().add(valMap);
					} else {
						attrib.getAttributeValue().add(multiValArray[i]);
					}

				}
			}

			String addSearch = "Add Search Creteria - " + "Type=\"" + type + "\" Name=\"" + name + "\" Value=\"" + value
					+ "\"";
			logger.debug(addSearch);
			System.out.println(addSearch);
//			System.out.println(attrib.getAttributeName()+" "+attrib.getAttributeValue()+" "+attrib.getAttributeType());
			searchAttributes.add(attrib);
			
		}
		
		// V363_2026.5 this project  only add type in  acBaisc
		if(searchAttributes.get(1).getAttributeValue().toString().equals("[V363_2026.5]"))
		{
			System.out.print("V363_2026.5...");
			searchAttributes.get(2).setAttributeType("acBasic");
			searchAttributes.get(3).setAttributeType("acBasic");
		}
		//System.out.println("\n");

		logger.debug("Searching Issues Identifiers");
		ProgressBarTraditional progressBar = new ProgressBarTraditional("Searching Issues Identifiers");
		progressBar.start();
		StopWatch searchIssuesForCriteriaTimer = new StopWatch();
		
		e2KSIssuesPortType.searchIssuesForCriteria(searchAttributes, options, searchResults, messages);
		
		searchIssuesForCriteriaTime = ((double) searchIssuesForCriteriaTimer.getElapsedTime());
		progressBar.showProgress = false;
        System.out.println("Search result is..."+searchResults.value.size());
        
       System.out.println("test............"+searchResults.value.get(0).getIdentifiers().get(0));
		return searchResults.value;
	}

	public void getIssues(List<AurosIdentifier> issueIdentifiers, Config config, List<FetchIssues> fetchIssuesThreads) {
		if (e2KSIssuesPortType == null) {
			e2KSIssuesPortType = binding.initIssuesPortAndCookie(config);
		}

		FetchIssues fetchIssuesThread = new FetchIssues(this, config, e2KSIssuesPortType, issueIdentifiers,
				fileAppenders, startTime);
		fetchIssuesThreads.add(fetchIssuesThread);
		fetchIssuesThread.start();

	}

//	public void getIssues_org(List<AurosIdentifier> issueIdentifiers, Config config, int run) throws Exception {
//
//		String runIssueSearch = "Executing Issue Search " + run + ". Run with " + issueIdentifiers.size() + " Issues";
//		logger.debug(runIssueSearch);
//		ProgressBarTraditional attrProgressBar = new ProgressBarTraditional(runIssueSearch);
//		attrProgressBar.start();
//
//		if (e2KSIssuesPortType == null) {
//			e2KSIssuesPortType = binding.initIssuesPortAndCookie(config);
//		}
//
//		List<InputOptions> options = new ArrayList<InputOptions>();
//		Holder<List<Issue>> issueListResult = new Holder<List<Issue>>();
//		Holder<AurosMessages> messages = null;
//
//		e2KSIssuesPortType.getIssues(issueIdentifiers, options, issueListResult, messages);
//
//		List<Issue> issueList = issueListResult.value;
//		attrProgressBar.showProgress = false;
//
//		if (issueList == null) {
//			issueList = new ArrayList<Issue>();
//		}
//
//		logger.debug("start getIssueAttr");
//		getIssueAttr(issueList, config);
//		logger.debug("stop getIssueAttr");
//
//	}

	public void getIssueAttr(List<Issue> issueList, Config config) throws Exception {

		List<IssueReportOcc> issueResultList = new ArrayList<IssueReportOcc>();
		Vector<String> issueHeader = config.getIssueHeader();
		for (int i = 0; i < issueList.size(); i++) {

			String procIssue = "Processing Issue: " + (i + 1) + " of " + issueList.size();
			logger.debug(procIssue);
			ProgressBarTraditional attrProgressBar = new ProgressBarTraditional(procIssue);

			attrProgressBar.start();
			Issue issue = issueList.get(i);
			IssueReportOcc issueOcc = new IssueReportOcc(config);
			String issueId = issue.getIssueId();

			if (issueId.contains("330183")) {
				System.out.println();
			}

			allIssuesMap.put(issueId, issueOcc);

			if (issueHeader.contains(Constant.IssueID)) {
				issueOcc.addIssueValue(Constant.IssueID, issueId);
			}

			if (issueHeader.contains(Constant.Status)) {
				String state = issue.getIssueStatusID();

				Map<String, ValidArgs> validArgMap = config.getValidArgMap();
				ValidArgs validArgs = validArgMap.get(Constant.status);
				Map<String, String> keyMap = validArgs.getKeyMap();
				String status = keyMap.get(state);

				if (status != null) {
					issueOcc.addIssueValue(Constant.Status, status);
				}

			}

			if (issueHeader.contains(Constant.IssueDescription)) {
				String issueDescription = issue.getIssueDescription();

				if (issueDescription != null) {
					issueOcc.addIssueValue(Constant.IssueDescription, issueDescription);
				}
			}

			if (issueHeader.contains(Constant.IssueDiscussion)) {

				List<Discussion> discussion = issue.getIssueDiscussion();
				TreeMap<Integer, String> discussMap = new TreeMap<Integer, String>(Collections.reverseOrder());
				for (int j = 0; j < discussion.size(); j++) {
					Discussion value = discussion.get(j);
					List<DiscussionData> discussionData = value.getDiscussionData();
					for (int k = 0; k < discussionData.size(); k++) {
						DiscussionData dataValue = discussionData.get(k);
						try {
							String julianDateString = dataValue.getDateCommented();
							int julianDateInt = Integer.parseInt(julianDateString);
							
							JulianDate julianDate = new JulianDate(julianDateString);
							
							String gregorianDateStrg = julianDate.getGregorianDate();

							String comments = dataValue.getComments().replaceAll("\n+", "\n");
							String[] simpleDate = gregorianDateStrg.split(":");
							String discussionText = dataValue.getCommentatorId() + "-" + simpleDate[0] + ": "
									+ comments;

							Integer discussKey = Integer.valueOf(julianDateInt);
							if(discussMap.get(discussKey) != null) {
								 discussKey = julianDate.findNextKey(discussKey, discussMap);
							}
							discussMap.put(discussKey, discussionText);
							

						} catch (NullPointerException np) {
							// nothing in, nothing out...
						}

					}

				}

				StringBuilder discussString = new StringBuilder();

				for (Integer julDate : discussMap.keySet()) {
					discussString.append(discussMap.get(julDate) + "\n\n");
					if (issueId.contains("314683")) {
						System.out.println("\n" + julDate.toString() + "***" + discussMap.get(julDate));
					}
				}

				issueOcc.addIssueValue(Constant.IssueDiscussion, discussString.toString());

			}

			if (issueHeader.contains(Constant.IssueAction)) {
				String issueAction = issue.getIssueAction();

				if (issueAction != null) {
					issueOcc.addIssueValue(Constant.IssueAction, issueAction);
				}

			}

			if (issueHeader.contains(Constant.IssueAddIssueInfo)) {
				String developerComments = issue.getDeveloperComments();
				if (developerComments != null) {
					issueOcc.addIssueValue(Constant.IssueAddIssueInfo, developerComments);
				}
			}

			if (issueHeader.contains(Constant.IssueCreator)) {
				String issueCreator = issue.getIssueCreator();
				issueOcc.addIssueValue(Constant.IssueCreator, issueCreator);
			}

			if (issueHeader.contains(Constant.IssueDateClosed)) {
				String dateClosed = issue.getDateClosed();
				if (dateClosed != null) {
					issueOcc.addIssueValue(Constant.IssueDateClosed, dateClosed);
				}

			}

			if (issueHeader.contains(Constant.IssueDateOpened)) {
				String dateOpened = issue.getDateOpened();
				if (dateOpened != null) {
					issueOcc.addIssueValue(Constant.IssueDateOpened, dateOpened);
				}

			}

			if (issueHeader.contains(Constant.IssueChampion)) {
				List<IssueAssignees> championList = issue.getIssueAssignees();
				StringBuilder champions = new StringBuilder();
				for (int j = 0; j < championList.size(); j++) {
					IssueAssignees champion = championList.get(j);
					List<String> championNameList = champion.getIssueAssignee();
					for (int k = 0; k < championNameList.size(); k++) {
						String championName = championNameList.get(k);
						champions.append(championName + ";");
					}

				}

				String championNames = champions.toString();
				if (!championNames.isEmpty()) {
					issueOcc.addIssueValue(Constant.IssueChampion, championNames);
				}

			}

			List<IssueChapter> issueChapters = issue.getIssueChapters();
			for (int j = 0; j < issueChapters.size(); j++) {
				IssueChapter value = issueChapters.get(j);

				List<ExtendedElement> chapterExtendedElements = value.getChapterExtendedElements();
				for (int k = 0; k < chapterExtendedElements.size(); k++) {
					ExtendedElement extValue = chapterExtendedElements.get(k);
					E2KSAttribute chapterExtendedElement = extValue.getChapterExtendedElement();
					String attributeName = chapterExtendedElement.getAttributeName();
					if (issueHeader.contains(attributeName)) {
						List<String> attributeValue = chapterExtendedElement.getAttributeValue();
						for (int l = 0; l < attributeValue.size(); l++) {
							String attributeValueData = attributeValue.get(l);
							String existingValue = issueOcc.getIssueValueMap().get(attributeName);
							if (existingValue != null && !existingValue.isEmpty()) {
								String completedValue = existingValue + ", " + attributeValueData;
								issueOcc.addIssueValue(attributeName, completedValue);
							} else {
								issueOcc.addIssueValue(attributeName, attributeValueData);
							}

						}

					}

				}

			}

			if (issueHeader.contains(Constant.IssueTeamMembers)) {
				StringBuilder teamMembers = new StringBuilder();
				List<IssueInterestedParties> issueInterestedParties = issue.getIssueInterestedParties();

				for (int j = 0; j < issueInterestedParties.size(); j++) {
					IssueInterestedParties value = issueInterestedParties.get(j);
					List<String> issueInterestedParty = value.getIssueInterestedParty();
					for (int k = 0; k < issueInterestedParty.size(); k++) {
						String interestedPartyValue = issueInterestedParty.get(k);
						teamMembers.append(interestedPartyValue + ";");
					}

				}
				String members = teamMembers.toString();
				if (!members.isEmpty()) {
					issueOcc.addIssueValue(Constant.IssueTeamMembers, members);
				}

			}

			if (issueHeader.contains(Constant.IssueCheckListID) || issueHeader.contains(Constant.IssueKPACID)
					|| issueHeader.contains(Constant.Description) || issueHeader.contains(Constant.IssueAddInfo)) {
				logger.debug("start getIssueSourceIdentifiers");

				List<AurosIdentifier> issueSourceIdentifiers = issue.getIssueSourceIdentifiers();
				Map<String, String> issueSourceAttr = getIssueSourceAttr(issueSourceIdentifiers, config);

				if (!issueSourceAttr.isEmpty()) {

					if (issueHeader.contains(Constant.IssueCheckListID)) {
						String chkListId = issueSourceAttr.get(Constant.IssueCheckListID);
						try {
							Integer.parseInt(chkListId);
							if (chkListId != null && !chkListId.isEmpty()) {
								issueOcc.addIssueValue(Constant.IssueCheckListID, chkListId);
							} else {
								issueOcc.addIssueValue(Constant.IssueCheckListID, "0");
							}
						} catch (Exception e) {
							issueOcc.addIssueValue(Constant.IssueCheckListID, "0");
						}
					}

					if (issueHeader.contains(Constant.IssueKPACID)) {
						String kpacId = issueSourceAttr.get(Constant.IssueKPACID);
						if (kpacId != null) {
							int lineNum = kpacId.split("[\n|\r]").length;
							if (lineNum == 1) {
								issueOcc.addIssueValue(Constant.IssueKPACID, kpacId);
							}
						}
					}

					if (issueHeader.contains(Constant.Description)) {
						String description = issueSourceAttr.get(Constant.Description);
						if (description != null) {
							issueOcc.addIssueValue(Constant.Description, description);
						}
					}

					if (issueHeader.contains(Constant.IssueAddInfo)) {
						String addInfo = issueSourceAttr.get(Constant.IssueAddInfo);
						if (addInfo != null) {
							addInfo.replaceAll("^\n", "");
							issueOcc.addIssueValue(Constant.IssueAddInfo, addInfo);
						}
					}

				}

				logger.debug("stop getIssueSourceIdentifiers");
			}

			if (issueHeader.contains(Constant.IssueType)) {
				String issueTypeName = issue.getIssueTypeName();
				issueOcc.addIssueValue(Constant.IssueType, issueTypeName);
			}

			if (issueHeader.contains(Constant.IssueLastModified)) {
				String lastModified = issue.getLastModified();
				if (!lastModified.isEmpty()) {
					issueOcc.addIssueValue(Constant.IssueLastModified, lastModified);
				}

			}

			if (issueHeader.contains(Constant.IssueProgram)) {
				String projectName = issue.getProjectName();
				issueOcc.addIssueValue(Constant.IssueProgram, projectName);
			}

			if (issueHeader.contains(Constant.IssueSeverity)) {
				String issueSeverityId = issue.getIssuePriority();
				String issueSeverity = Constant.issueSeverityTextMap.get(issueSeverityId);
				if (issueSeverity != null) {
					issueOcc.addIssueValue(Constant.IssueSeverity, issueSeverity);
				}

			}

			if (issueOcc.getIssueValueMap().get(Constant.IssueLastModified) != null
					&& issueHeader.contains(Constant.IssueLastModifyUser)) {
				List<History> issueHistory = issue.getIssueHistory();
				if (issueHistory != null && !issueHistory.isEmpty()) {
					History history = issueHistory.get(0);
					List<HistoryInstance> historyInstanceList = history.getHistoryInstance();
					if (historyInstanceList != null && !historyInstanceList.isEmpty()) {
						HistoryInstance historyInstance = historyInstanceList.get(0);
						String modifyUser = historyInstance.getChangedBy();
						if (modifyUser != null) {
							issueOcc.addIssueValue(Constant.IssueLastModifyUser, modifyUser);
						}
					}

				}

			}

			if (issueHeader.contains(Constant.IssueAddIssueInfo)) {
				String addInfo = issue.getQaPlanning();
				if (addInfo != null) {
					issueOcc.addIssueValue(Constant.IssueAddIssueInfo, addInfo);
				}

			}

			issueResultList.add(issueOcc);

			attrProgressBar.showProgress = false;

		}

		addAcCopIDbyList(issueResultList, config);

	}

	private Map<String, String> getIssueSourceAttr(List<AurosIdentifier> issueSourceIdentifiers, Config config)
			throws Exception {
		Vector<String> issueHeader = config.getIssueHeader();
		Map<String, String> clkMap = new HashMap<String, String>();

		List<String> checkListId = new ArrayList<String>();
		List<String> kPacId = new ArrayList<String>();

		StringBuilder kpacIdFragment = null;
		for (int i = 0; i < issueSourceIdentifiers.size(); i++) {
			AurosIdentifier ai = issueSourceIdentifiers.get(i);

			List<String> identifiers = ai.getIdentifiers();

			int iSize = identifiers.size();
			kpacIdFragment = new StringBuilder();
			for (int j = 0; j < iSize; j++) {
				String value = identifiers.get(j);
				switch (j) {
				case 0:
					checkListId.add(value);
					break;
				case 1:
					kpacIdFragment.append(value + "-");
					break;
				case 2:
					kpacIdFragment.append(value);
					break;
				case 3:
					kpacIdFragment.append("(#" + value + ")");
					break;
				default:
					break;
				}
			}

			kPacId.add(kpacIdFragment.toString());

		}

		if (issueHeader.contains(Constant.IssueCheckListID)) {
			clkMap.put(Constant.IssueCheckListID, String.join(",", checkListId));
		}

		if (issueHeader.contains(Constant.IssueKPACID)) {
			clkMap.put(Constant.IssueKPACID, String.join(",", kPacId));
		}

		return clkMap;
	}

	@SuppressWarnings("unused")
	private Map<String, String> getIssueSourceAttrOld(List<AurosIdentifier> issueSourceIdentifiers, Config config)
			throws Exception {
		boolean multiKpac = false;
		Vector<String> issueHeader = config.getIssueHeader();
		Map<String, String> clkMap = new HashMap<String, String>();
		Set<String> checkListId = new HashSet<String>();
		List<String> kPacId = new ArrayList<String>();

		StringBuilder kpacIdFragment = null;

		String copID = "";
		String kpacIdValue = "";
		String kPacDesc = "Multiple K-PACs are associated with this Issue";
		String kPacAddInfo = "Multiple K-PACs are associated with this Issue";

		int size = issueSourceIdentifiers.size();
		if (size > 1) {
			multiKpac = true;
		}

		for (int i = 0; i < size; i++) {
			AurosIdentifier ai = issueSourceIdentifiers.get(i);

			List<String> identifiers = ai.getIdentifiers();
			int iSize = identifiers.size();
			kpacIdFragment = new StringBuilder();
			for (int j = 0; j < iSize; j++) {
				String value = identifiers.get(j);
				switch (j) {
				case 0:
					checkListId.add(value);
					break;
				case 1:
					copID = value;
					kpacIdFragment.append(value + "-");
					break;
				case 2:
					kpacIdValue = value;
					kpacIdFragment.append(value);
					break;
				case 3:
					kpacIdFragment.append("(#" + value + ")");
					break;
				default:
					break;
				}
			}

			kPacId.add(kpacIdFragment.toString());

			if (!multiKpac && (!copID.isEmpty() || !kpacIdValue.isEmpty())) {
				KPac issueKPac = getIssueKPac(copID, kpacIdValue, config);
				if (issueKPac != null) {
					if (issueHeader.contains(Constant.IssueAddInfo)) {
						String additionalInfo = issueKPac.getAdditionalInfo();
						if (additionalInfo != null) {
							Document doc = Jsoup.parse(additionalInfo);
							HtmlToPlainText formatter = new HtmlToPlainText();
							kPacAddInfo = formatter.getPlainText(doc);

						} else {
							additionalInfo = " ";
						}

					}
					if (issueHeader.contains(Constant.Description)) {
						KPacHeader kpacHeader = issueKPac.getKpacHeader();
						if (kpacHeader != null) {
							String description = kpacHeader.getDescription();
							if (description != null) {
								Document doc = Jsoup.parse(description);
								HtmlToPlainText formatter = new HtmlToPlainText();
								kPacDesc = formatter.getPlainText(doc);
							} else {
								kPacDesc = " ";
							}

						}

					}

				}

			}

		}

		if (issueHeader.contains(Constant.IssueCheckListID)) {
			clkMap.put(Constant.IssueCheckListID, String.join(",", checkListId));
		}

		if (issueHeader.contains(Constant.IssueKPACID)) {
			clkMap.put(Constant.IssueKPACID, String.join(",", kPacId));
		}

		if (issueHeader.contains(Constant.Description)) {
			clkMap.put(Constant.Description, kPacDesc);
		}

		if (issueHeader.contains(Constant.IssueAddInfo)) {
			clkMap.put(Constant.IssueAddInfo, kPacAddInfo);
		}

		return clkMap;

	}

	private void addAcCopIDbyList(List<IssueReportOcc> issueResultList, Config config) throws Exception {

		List<String> acList = new ArrayList<String>();

		for (int i = 0; i < issueResultList.size(); i++) {
			IssueReportOcc issueReportOcc = issueResultList.get(i);
			String chklistIdFrag = issueReportOcc.getIssueValueMap().get(Constant.IssueCheckListID);
			acList.add(chklistIdFrag);
		}

		Holder<AurosMessages> message = new Holder<AurosMessages>();
		List<InputOptions> inputOptions = new ArrayList<InputOptions>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		Holder<List<AssessmentHeader>> results = new Holder<List<AssessmentHeader>>();
		try {
			e2KSAssessmentsPortType.getAssessmentHeaders(acList, inputOptions, results, message);
		} catch (E2KSFault_Exception e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "ISSUELIST");
			e.printStackTrace();
			System.exit(1);
		}

		List<AssessmentHeader> resultList = results.value;
		String copId = "0";
		String chkListId = "";

		for (int i = 0; i < resultList.size(); i++) {
			IssueReportOcc issueReportOcc = issueResultList.get(i);

			AssessmentHeader assessmentHeader = resultList.get(i);
			if (assessmentHeader != null) {
				copId = assessmentHeader.getCopId();
				chkListId = copId + "-CK" + acList.get(i);
				issueReportOcc.getIssueValueMap().put(Constant.IssueCheckListID, chkListId);
			} else {
				issueReportOcc.getIssueValueMap().put(Constant.IssueCheckListID, "");
			}
		}

	}

	private KPac getIssueKPac(String copID, String kpacId, Config config) throws Exception {

		if (e2KSBasePortType == null) {
			e2KSBasePortType = binding.initBasePortAndCookie(config);
		}

		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<KPac> result = new Holder<KPac>();

		List<InputOptions> inputOptions = new ArrayList<InputOptions>();
		InputOptions inputOption = new InputOptions();
		inputOption.setName("queryKPacAttributes");
		inputOption.setValue("true");
		inputOptions.add(inputOption);

		e2KSBasePortType.getKPac(copID, kpacId, "", inputOptions, result, messages);
		KPac kpac = result.value;
		return kpac;

	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

}
