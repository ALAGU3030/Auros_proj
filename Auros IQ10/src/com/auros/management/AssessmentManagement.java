package com.auros.management;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import javax.xml.ws.Holder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

import com.auros.connector.Binding;
import com.auros.credentials.AurosCredentials;
import com.auros.management.utils.FetchAssKPacs;
import com.auros.management.utils.FetchAssOcces;
import com.auros.model.AssessIssues;
import com.auros.model.AssessReportOcc;
import com.auros.model.AssessResult;
import com.auros.model.Config;
import com.auros.model.Constant;
import com.auros.model.KPacAc;
import com.auros.model.ValidArgs;
import com.auros.utils.ProgressBarTraditional;
import com.auros.utils.StopWatch;
import com.auros.utils.Utils;
import com.emergent.e2ks.types.AurosIdentifier;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.AurosRelation;
import com.emergent.e2ks.types.ChangeHistory;
import com.emergent.e2ks.types.ConformanceState;
import com.emergent.e2ks.types.E2KSAttribute;
import com.emergent.e2ks.types.E2KSAttributeList;
import com.emergent.e2ks.types.InputOptions;
import com.emergent.e2ks.types.KPac;
import com.emergent.e2ks.types.KPacHeader;
import com.emergent.e2ks.types.Message;
import com.emergent.e2ks.types.asses.Assessment;
import com.emergent.e2ks.types.asses.AssessmentHeader;
import com.emergent.e2ks.types.asses.Occurrence;
import com.emergentsys.auros.services.assessments.E2KSAssessmentsPortType;
import com.emergentsys.auros.services.assessments.E2KSFault_Exception;
import com.emergentsys.auros.services.aurosbase.E2KSBasePortType;
import com.emergentsys.auros.services.issues.E2KSIssuesPortType;

public class AssessmentManagement {

	private E2KSAssessmentsPortType e2KSAssessmentsPortType = null;
	private E2KSIssuesPortType e2KSIssuesPortType = null;
	private E2KSBasePortType e2KSBasePortType = null;

	private AssessResult assessmentResult = new AssessResult();
	private AssessIssues assessmentIssues = new AssessIssues();
	private List<String> aurosIdList = new ArrayList<String>();
	private Map<String, KPacAc> kpacAcMap = new HashMap<String, KPacAc>();

	private Binding binding = null;
	private static Logger logger = Logger.getLogger(AssessmentManagement.class);
	private Map<String, List<Occurrence>> assessmentOcces = new HashMap<String, List<Occurrence>>();

	private int numOfAssessments = 0;
	private int numOfKpacs = 0;
	private Map<String, FileAppender> fileAppenders = null;
	private String startTime;
	private double searchAurosIdentTime = 0.0;
	private double searchAurosIssuesTime = 0.0;
	private double fetchAssOccesTime = 0.0;
	private double processOccesTime = 0.0;

	public double getSearchAurosIdentTime() {
		return searchAurosIdentTime;
	}

	public double getSearchAurosIssuesTime() {
		return searchAurosIssuesTime;
	}

	public double getFetchAssOccesTime() {
		return fetchAssOccesTime;
	}

	public double getProcessOccesTime() {
		return processOccesTime;
	}

	public AssessmentManagement(AurosCredentials auth, Map<String, FileAppender> fileAppenders, String startTime) {
		this.startTime = startTime;
		this.fileAppenders = fileAppenders;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
		binding = new Binding(auth, fileAppenders);
	}

	public Map<String, List<Occurrence>> getAssOccMap() {
		return assessmentOcces;
	}

	public void setAssOccMap(String id, List<Occurrence> assSubList) {
		assessmentOcces.put(id, assSubList);
	}

	public AssessResult getAssessmentResult() {
		return assessmentResult;
	}

	public AssessIssues getAssessmentIssues() {
		return assessmentIssues;
	}

	/**
	 * Search Assessment by given Creteria
	 * 
	 * @param config
	 * @throws Exception
	 */
	public void searchAssessmentsByCreteria(Config config) throws Exception {
		List<FetchAssOcces> fetchAssOccThreads = new ArrayList<FetchAssOcces>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
			System.out.println();
		}

		Holder<List<AurosIdentifier>> searchResults = new Holder<List<AurosIdentifier>>();
		List<E2KSAttribute> searchAttributes = new ArrayList<E2KSAttribute>();
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
			System.out.println("Name is"+name);
			System.out.println("value is"+value);
			
			ValidArgs validArgsByType = validArgMap.get(name);
			System.out.println("1111111111"+validArgsByType.getName());
			
			if (validArgsByType == null) {
				throw new Exception("Invalid argument found : " + name);
			}
			
			String type = validArgsByType.getType();
			Map<String, String> valueMap = validArgsByType.getValueMap();

			attrib.setAttributeName(name);
			attrib.setAttributeType(type);

			int multiValueSep = value.indexOf(Constant.COMMA);
			System.out.println("22222222222"+multiValueSep);
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
			searchAttributes.add(attrib);
			//System.out.println(attrib.getAttributeName()+" "+attrib.getAttributeValue()+" "+attrib.getAttributeType());
		}

		// V363_2026.5 this project  only add type in  acBaisc
		if(searchAttributes.get(1).getAttributeValue().toString().equals("[V363_2026.5]"))
		{
			System.out.print("V363_2026.5...");
			searchAttributes.get(2).setAttributeType("acBasic");
			searchAttributes.get(3).setAttributeType("acBasic");
		}
		System.out.println("\n");
		logger.debug("Searching Assessment Identifiers");

		ProgressBarTraditional progressBar = new ProgressBarTraditional("Searching Assessment Identifiers");
		progressBar.start();

		StopWatch searchAurosIdentTimer = new StopWatch();
	
		e2KSAssessmentsPortType.searchAssessmentsForCriteria(searchAttributes, options, searchResults, messages);

		
		progressBar.showProgress = false;

		List<AurosIdentifier> aiList = searchResults.value;
		
		if (aiList == null) {
			logger.debug("No Assessment Identifiers found!");
			System.out.println("No Assessment Identifiers found!");
			return;
		}
		
		for (int i = 0; i < aiList.size(); i++) {

			AurosIdentifier ai = aiList.get(i);
			List<String> idList = ai.getIdentifiers();
			for (int j = 0; j < idList.size(); j++) {
				String id = idList.get(j);
				aurosIdList.add(id);
			}

		}
		searchAurosIdentTime = ((double) searchAurosIdentTimer.getElapsedTime());

		logger.debug(aiList.size() + " Assessment Identifiers found, processing start...");

		int size = aurosIdList.size();
		int chunkSize = config.getChunkSize();
		int endSub = 0;

		int connections = Utils.mod(size, chunkSize);

		if (connections > Constant.maxConnections) {

			chunkSize = Utils.mod(size, 190);
			connections = Utils.mod(size, chunkSize);
			System.out.println("Using Chunksize of " + chunkSize + " for maximum Connections of 190\n");
		}

		numOfAssessments = aiList.size();
		String procAssess = "Processing " + numOfAssessments + " Assessments in " + connections
				+ " parallel connections";

		ProgressBarTraditional attrProgressBar = new ProgressBarTraditional(procAssess);
		attrProgressBar.start();

		Vector<String> assessHeader = config.getAssessHeader();
		if (assessHeader.contains(Constant.AssessIssue)) {
			StopWatch searchAurosIssuesTimer = new StopWatch();
			assessmentIssues = getIssuesOfAssessments(aurosIdList, config);
			searchAurosIssuesTime = ((double) searchAurosIssuesTimer.getElapsedTime());
		}

		StopWatch fetchAssOccesTimer = new StopWatch();
		if (size > chunkSize) {

			for (int i = 0; i < size; i = i + chunkSize) {

				if (size > i + chunkSize) {
					endSub = i + chunkSize;
				} else {
					endSub = size;
				}

				List<String> subAurosIdList = aurosIdList.subList(i, endSub);
				FetchAssOcces fetchAssOccThread = new FetchAssOcces(this, this.binding, e2KSAssessmentsPortType, config,
						subAurosIdList, fileAppenders, startTime);
				fetchAssOccThreads.add(fetchAssOccThread);
				fetchAssOccThread.start();
				
			}
		} else {
			FetchAssOcces fetchAssOccThread = new FetchAssOcces(this, this.binding, e2KSAssessmentsPortType, config,
					aurosIdList, fileAppenders, startTime);
			fetchAssOccThreads.add(fetchAssOccThread);
			fetchAssOccThread.start();
		}

		for (int i = 0; i < fetchAssOccThreads.size(); i++) {
			FetchAssOcces fetchAssOccThread = fetchAssOccThreads.get(i);
			try {
				fetchAssOccThread.join();
			} catch (InterruptedException e) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				String crrntTime = format.format(new Date());
				logger.error(e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";"
						+ "CHECKLIST");
				e.printStackTrace();
				System.exit(1);
			}
		}
		fetchAssOccesTime = ((double) fetchAssOccesTimer.getElapsedTime());

		StopWatch processOccesTimer = new StopWatch();
		for (Entry<String, List<Occurrence>> entry : assessmentOcces.entrySet()) {
			String id = entry.getKey();
			List<Occurrence> assOcces = entry.getValue();
			processOccurrences(id, assOcces, config);
		}
		processOccesTime = ((double) processOccesTimer.getElapsedTime());
		
		System.out.println("processOccesTime"+processOccesTime);

		attrProgressBar.showProgress = false;

	}

	/**
	 * Get all Occurrences for Assessments found
	 * 
	 * @param id
	 * @param config
	 * @throws Exception
	 */
	private void processOccurrences(String id, List<Occurrence> assessmentOccList, Config config) throws Exception {

		Map<String, List<String>> assessmentHistory = null;

		Vector<String> assessHeader = config.getAssessHeader();
		Map<String, String> argMap = config.getArgMap();

		if (assessmentOccList == null) {
			String noOccFound = "No Occ found with ID: " + id;
			System.out.println(noOccFound);
			logger.debug(noOccFound);
			return;
		}

		if (assessHeader.contains(Constant.LastModifiedOn) || assessHeader.contains(Constant.LastModifiedBy)) {
			assessmentHistory = getAssessmentHistory(id, config);
		}

		// Collect Assessment Occurrences
		for (int i = 0; i < assessmentOccList.size(); i++) {
			Occurrence assessmentOcc = assessmentOccList.get(i);

			if (assessmentOcc == null) {
				continue;
			}

			String copID = assessmentOcc.getCopID();
			String kpacID = assessmentOcc.getKpacID();
			String kpacVersion = assessmentOcc.getKpacVersion();
			String occurrenceID = assessmentOcc.getOccurrenceID();

			String occKpac = copID + Constant.DASH + kpacID + " V-" + kpacVersion + " (#" + occurrenceID + ")";

			String kpacKey = copID + kpacID + kpacVersion + occurrenceID;
			String occKpacKey = id + Constant.DASH + kpacKey;

			String occIssueKey = id + Constant.DASH + copID + kpacID + occurrenceID;

			AssessReportOcc occ = new AssessReportOcc(config);
			occ.setAssessValue("aurosId", id);

			occ.setAssessmentOcc(assessmentOcc);
			occ.setAssessmentIssueId(occIssueKey);
			occ.setKpacKey(kpacKey);

			String state = assessmentOcc.getState();

			int stateNumber;
			String stateValue = "";

			if (state == null) {
				stateValue = "NULL";
			} else {
				try {
					stateNumber = Integer.parseInt(assessmentOcc.getState());
					stateValue = Constant.acStatusTextMap.get(stateNumber);
				} catch (NumberFormatException e) {
					stateValue = assessmentOcc.getState();
				}

			}

			if (stateValue == null || stateValue.isEmpty()) {
				stateValue = "UNKNOWN";
			}

			if (assessmentHistory != null) {
				List<String> histList = assessmentHistory.get(occKpacKey);
				if (histList != null && histList.size() == 2) {
					occ.setAssessValue(Constant.LastModifiedBy, histList.get(0));
					occ.setAssessValue(Constant.LastModifiedOn, histList.get(1));
				}

			}

			if (assessHeader.contains(Constant.Program)) {
				String program = argMap.get(Constant.Project);
				if (program != null) {
					occ.setAssessValue(Constant.Program, program);
				}

			}

			if (assessHeader.contains(Constant.Status)) {
				occ.setAssessValue(Constant.Status, stateValue);
			}

			if (assessHeader.contains(Constant.KPACId)) {
				occ.setAssessValue(Constant.KPACId, occKpac);
			}

			String cops = config.getArgMap().get(Constant.cops);
			if (cops != null) {
				if (assessHeader.contains(Constant.AssessmentId)) {
					occ.setAssessValue(Constant.AssessmentId, cops + Constant.DASH + "CK" + id);
				}

			}

			// TODO:
			// kpacKey ist aus den dynamischen anteilen der 2. Spalte im Excel
			// Bei gleichem Wert sind die KPAC Attribute (Author, Title,...) die selben.
			// Folglich m�ssen diese nur 1 mal geholt werden und auf die entsprechenden
			// Excel Zeilen verteilt werden.

			KPacAc kpacAc = new KPacAc();
			kpacAc.setKpacKey(kpacKey);
			kpacAc.setCopID(copID);
			kpacAc.setKpacID(kpacID);
			kpacAc.setKpacVersion(kpacVersion);
			kpacAcMap.put(kpacKey, kpacAc);

			assessmentResult.put(occKpacKey, occ);

		}

	}

	private AssessIssues getIssuesOfAssessments(List<String> assessmentIDs, Config config) throws Exception {

		StringBuilder assessIssueIds = null;
		AssessIssues assessIssues = new AssessIssues();
		if (e2KSIssuesPortType == null) {
			e2KSIssuesPortType = binding.initIssuesPortAndCookie(config);
		}
           
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		messages = new Holder<AurosMessages>();
		Holder<List<AurosRelation>> result = new Holder<List<AurosRelation>>();
		List<InputOptions> options = null;

		try {
			e2KSIssuesPortType.getIssuesForAssessments(assessmentIDs, options, result, messages);
		
		}catch (NullPointerException e) {
			if(assessmentIDs != null && !assessmentIDs.isEmpty()) {
				logger.error("nullpointer in emergent code with assessmentIDs, like: "+assessmentIDs.get(0));			
			}else {
				logger.error("missing assessmentIDs for getIssuesForAssessments" );
			}
			
		}

		for (AurosRelation ar : result.value) {
			List<AurosIdentifier> assessIssueAurosIDs = ar.getRelatedAurosIDs();

			for (int i = 0; i < assessIssueAurosIDs.size(); i++) {
				AurosIdentifier assessIssueAurosId = assessIssueAurosIDs.get(i);
				List<String> iDs = assessIssueAurosId.getIdentifiers();
				if (i == 0) {
					assessIssueIds = new StringBuilder(String.join(", ", iDs));

				} else {
					assessIssueIds.append("," + String.join(", ", iDs));
				}
			}

			AurosIdentifier assessAurosID = ar.getAurosID();
			if (assessAurosID == null)
				continue;

			List<String> assessOccIds = assessAurosID.getIdentifiers();

			String assRec = "";
			for (int i = 0; i < assessOccIds.size() - 1; i++) {
				if (i == 0) {
					assRec = assessOccIds.get(i) + Constant.DASH;
				} else {
					assRec = assRec + assessOccIds.get(i);
				}
			}
			assessIssues.put(assRec, assessIssueIds.toString());
		}

		return assessIssues;

	}

	private Map<String, List<String>> getAssessmentHistory(String ac, Config config) throws E2KSFault_Exception {
		Map<String, List<String>> histMap = new HashMap<String, List<String>>();

		Holder<AurosMessages> message = new Holder<AurosMessages>();
		Holder<Assessment> result = new Holder<Assessment>();

		InputOptions histOption = new InputOptions();
		histOption.setName("fetchHistory");
		histOption.setValue("true");

		List<InputOptions> inputOptions = null;
		inputOptions = new ArrayList<InputOptions>();
		inputOptions.add(histOption);

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		e2KSAssessmentsPortType.getAssessment(ac, inputOptions, result, message);

		Assessment assessment = result.value;
		if (assessment == null) {
			return histMap;
		}
		List<ConformanceState> persistedConformanceStates = assessment.getPersistedConformanceStates();
		for (int i = 0; i < persistedConformanceStates.size(); i++) {
			List<String> histData = new ArrayList<String>();
			ConformanceState confState = persistedConformanceStates.get(i);
			if (confState == null)
				continue;
			String copID = confState.getCopID();
			String kpacID = confState.getKpacID();
			String kpacVersion = confState.getKpacVersion();
			String occurrenceID = confState.getOccuranceID();
			String occKpacKey = ac + Constant.DASH + copID + kpacID + kpacVersion + occurrenceID;

			List<ChangeHistory> changeHistory = confState.getChangeHistory();
			if (changeHistory != null && !changeHistory.isEmpty()) {
				ChangeHistory changeHist = confState.getChangeHistory().get(0);
				String changedBy = changeHist.getChangedBy();
				String changedTimeStamp = changeHist.getChangedTimeStamp();
				histData.add(changedBy);
				histData.add(changedTimeStamp);
				histMap.put(occKpacKey, histData);
			}

		}

		return histMap;

	}

	/**
	 * Set AC Attributes to all Assessment Occurrences
	 * 
	 * @param config
	 * @throws E2KSFault_Exception
	 */
	public void setAcAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcAttr = getAcAttr(config);
		Vector<String> assessHeader = config.getAssessHeader();
		
		for (Map.Entry<String, AssessReportOcc> acOcc : assessmentResult.entrySet()) {
			String acKpac = acOcc.getKey();
			AssessReportOcc occ = acOcc.getValue();

			String[] acKpacArray = acKpac.split(Constant.DASH);
			String acId = acKpacArray[0];

			Map<String, String> acAttrMap = allAcAttr.get(acId);
			if (acAttrMap == null) {
				String missingAcAttr = "\nMissing AC Attributes in: " + acKpac;
				System.out.println(missingAcAttr);

				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				String crrntTime = format.format(new Date());
				logger.error(missingAcAttr.replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";"
						+ "CHECKLIST");

				System.exit(1);
			}
			for (Map.Entry<String, String> acValue : acAttrMap.entrySet()) {
				String attrName = acValue.getKey();
				String attrValue = acValue.getValue();

				if (assessHeader.contains(attrName) && !occ.hasAssessValue(attrName)) {
					// logger.info("Found AC Attribute: "+ attrName + "-" + attrValue );
					occ.setAssessValue(attrName, attrValue);
				}
			}
		}
	}

	/**
	 * Get AC Attributes for all ACs
	 * 
	 * @param config
	 * @return
	 * @throws E2KSFault_Exception
	 */
	private Map<String, Map<String, String>> getAcAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcAttr = new HashMap<String, Map<String, String>>();
		List<InputOptions> inputOptions = null;
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<List<E2KSAttributeList>> result = new Holder<List<E2KSAttributeList>>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		e2KSAssessmentsPortType.getAssessmentsAttributes(aurosIdList, inputOptions, result, messages);

		/** New Error logging for catching information to the Auros Experts:**/
		List<E2KSAttributeList> resultList = result.value;
//		System.out.println("resultset vaue is"+resultList);
		if (resultList == null || resultList.isEmpty()) {
			String missingAcAttr = "\nMissing AC Attributes for some aurosIds";
			missingAcAttr= missingAcAttr.replaceAll("\\r|\\n", " ");
			
			if(messages != null) {
				AurosMessages value = messages.value;
				List<Message> errMsg = value.getMessages();
				for(int i =0; i<errMsg.size();i++) {
					Message message = errMsg.get(i);
					missingAcAttr = missingAcAttr+ ";" +message.getMessageText();
				}
			}
			System.out.println(missingAcAttr);

			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(missingAcAttr.replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";"
					+ "CHECKLIST");
			/******************************************************************/
			return allAcAttr;
		}
		for (int i = 0; i < resultList.size(); i++) {
			E2KSAttributeList acAttributeList = resultList.get(i);
			if (acAttributeList == null)
				continue;
			List<E2KSAttribute> attributes = acAttributeList.getAttributeValue();
			Map<String, String> acAttrMap = new HashMap<String, String>();
			for (int j = 0; j < attributes.size(); j++) {
				String attributeName = attributes.get(j).getAttributeName();
				String attributeValue = attributes.get(j).getAttributeValue().toString();
				acAttrMap.put(attributeName, attributeValue);
			}
			allAcAttr.put(aurosIdList.get(i), acAttrMap);
		}

		return allAcAttr;

	}

	/**
	 * Set ACSE Attributes to all Assessment Occurrences
	 * 
	 * @param config
	 * @throws E2KSFault_Exception
	 */
	public void setAcSEAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcSEAttr = getAcSEAttr(config);
		Vector<String> assessHeader = config.getAssessHeader();

		for (Map.Entry<String, AssessReportOcc> acOcc : assessmentResult.entrySet()) {
			String acKpac = acOcc.getKey();

			Map<String, String> acAttrMap = allAcSEAttr.get(acKpac);
			if (acAttrMap == null) {
				continue;
			}

			for (Map.Entry<String, String> acValue : acAttrMap.entrySet()) {
				String attrName = acValue.getKey();
				String attrValue = acValue.getValue();

				AssessReportOcc occ = acOcc.getValue();
				if (assessHeader.contains(attrName) && !occ.hasAssessValue(attrName)) {
					occ.setAssessValue(attrName, attrValue);
				}

			}
		}

	}

	/**
	 * Get ACSE Attributes for all ACs
	 * 
	 * @param config
	 * @return
	 * @throws E2KSFault_Exception
	 */
	private Map<String, Map<String, String>> getAcSEAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcSEAttr = new HashMap<String, Map<String, String>>();
		List<InputOptions> inputOptions = null;
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<List<E2KSAttributeList>> result = new Holder<List<E2KSAttributeList>>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		List<String> singleId = new ArrayList<String>();
		if (aurosIdList.isEmpty()) {
			return allAcSEAttr;
		}
		singleId.add(aurosIdList.get(0));
		e2KSAssessmentsPortType.retrieveACSEAttributesForACs(aurosIdList, inputOptions, result, messages);

		List<E2KSAttributeList> resultList = result.value;
		if (resultList == null) {
			
			return allAcSEAttr;
		}

		for (int i = 0; i < resultList.size(); i++) {
			E2KSAttributeList acSEAttributeList = resultList.get(i);

			if (acSEAttributeList == null)
				continue;

			List<E2KSAttribute> attributes = acSEAttributeList.getAttributeValue();
			if (attributes == null)
				continue;

			Map<String, String> acSEAttrMap = new HashMap<String, String>();

			for (int j = 0; j < attributes.size(); j++) {
				E2KSAttribute e2ksAttribute = attributes.get(j);
				if (e2ksAttribute == null)
					continue;

				String attributeName = e2ksAttribute.getAttributeName();
				String attributeString = "";

				List<String> attributeValue = e2ksAttribute.getAttributeValue();
				if (attributeValue != null) {
					attributeString = e2ksAttribute.getAttributeValue().toString();
				}

				acSEAttrMap.put(attributeName, attributeString);
			}

			List<String> seIdList = acSEAttributeList.getContainerID().getIdentifiers();
			seIdList.remove(seIdList.size() - 1);
			seIdList.add(1, "-");
			String containerId = String.join("", seIdList);

			allAcSEAttr.put(containerId, acSEAttrMap);

		}

		return allAcSEAttr;

	}

	/**
	 * Set ACEE Attributes to all Assessment Occurrences
	 * 
	 * @param config
	 * @throws E2KSFault_Exception
	 */
	public void setAcEEAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcEEAttr = getAcEEAttr(config);
		Vector<String> assessHeader = config.getAssessHeader();

		for (Map.Entry<String, AssessReportOcc> acOcc : assessmentResult.entrySet()) {
			String acKpac = acOcc.getKey();
			AssessReportOcc occ = acOcc.getValue();

			String[] acKpacArray = acKpac.split(Constant.DASH);
			String ac = acKpacArray[0];

			Map<String, String> acAttrMap = allAcEEAttr.get(ac);
			if (acAttrMap == null) {
				continue;
			}
			for (Map.Entry<String, String> acValue : acAttrMap.entrySet()) {
				String attrName = acValue.getKey();
				String attrValue = acValue.getValue();

				if (assessHeader.contains(attrName) && !occ.hasAssessValue(attrName)) {
					occ.setAssessValue(attrName, attrValue);
				}

			}
		}
	}

	/**
	 * Get ACEE Attributes for all ACs
	 * 
	 * @param config
	 * @return
	 * @throws E2KSFault_Exception
	 */
	private Map<String, Map<String, String>> getAcEEAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcEEAttr = new HashMap<String, Map<String, String>>();
		List<InputOptions> inputOptions = null;
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<List<E2KSAttributeList>> result = new Holder<List<E2KSAttributeList>>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		e2KSAssessmentsPortType.retrieveACEEAttributesForACs(aurosIdList, inputOptions, result, messages);

		List<E2KSAttributeList> resultList = result.value;
		if (resultList == null) {
			return allAcEEAttr;
		}

		for (int i = 0; i < resultList.size(); i++) {
			E2KSAttributeList acEEAttributeList = resultList.get(i);
			if (acEEAttributeList == null)
				continue;

			List<E2KSAttribute> attributes = acEEAttributeList.getAttributeValue();
			if (attributes == null)
				continue;

			Map<String, String> acEEAttrMap = new HashMap<String, String>();
			for (int j = 0; j < attributes.size(); j++) {
				E2KSAttribute e2ksAttribute = attributes.get(j);
				if (e2ksAttribute == null)
					continue;

				String attributeName = e2ksAttribute.getAttributeName();
				String attributeString = "";
				List<String> attributeValue = e2ksAttribute.getAttributeValue();
				if (attributeValue != null) {
					attributeString = e2ksAttribute.getAttributeValue().toString();
				}

				acEEAttrMap.put(attributeName, attributeString);
			}
			allAcEEAttr.put(aurosIdList.get(i), acEEAttrMap);
		}

		return allAcEEAttr;

	}

	public void setAcHeaderAttr(Config config) throws E2KSFault_Exception {
		Map<String, Map<String, String>> allAcHeaderAttr = getAcHeaderAttr(config);
		Vector<String> assessHeader = config.getAssessHeader();

		for (Map.Entry<String, AssessReportOcc> acOcc : assessmentResult.entrySet()) {
			String acKpac = acOcc.getKey();
			AssessReportOcc occ = acOcc.getValue();

			String[] acKpacArray = acKpac.split(Constant.DASH);
			String ac = acKpacArray[0];

			Map<String, String> acAttrMap = allAcHeaderAttr.get(ac);
			for (Map.Entry<String, String> acValue : acAttrMap.entrySet()) {
				String attrName = acValue.getKey();

				String attrValue = acValue.getValue();
				if (assessHeader.contains(attrName) && !occ.hasAssessValue(attrName)) {
					occ.setAssessValue(attrName, attrValue);
				}

			}

		}

	}

	private Map<String, Map<String, String>> getAcHeaderAttr(Config config) throws E2KSFault_Exception {

		Map<String, Map<String, String>> allAcHeaderAttr = new HashMap<String, Map<String, String>>();
		Vector<String> assessHeader = config.getAssessHeader();
		List<InputOptions> inputOptions = null;
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<List<AssessmentHeader>> result = new Holder<List<AssessmentHeader>>();

		if (e2KSAssessmentsPortType == null) {
			e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
		}

		e2KSAssessmentsPortType.getAssessmentHeaders(aurosIdList, inputOptions, result, messages);

		List<AssessmentHeader> resultList = result.value;
		if (resultList == null) {
			return allAcHeaderAttr;
		}

		for (int i = 0; i < resultList.size(); i++) {
			AssessmentHeader acHeader = resultList.get(i);
			if (acHeader == null)
				continue;

			String acId = acHeader.getAssessmentId();
			if (!allAcHeaderAttr.containsKey(acId)) {
				allAcHeaderAttr.put(acId, new HashMap<String, String>());
			}

			if (assessHeader.contains(Constant.Program)) {
				String programName = acHeader.getProjectName();
				allAcHeaderAttr.get(acId).put(Constant.Program, programName);
			}

			if (assessHeader.contains(Constant.OperationProcess)) {
				String opValue = acHeader.getSubProjectName();
				allAcHeaderAttr.get(acId).put(Constant.OperationProcess, opValue);
			}

			if (assessHeader.contains(Constant.Timing)) {
				String timingValue = acHeader.getMileStone();
				allAcHeaderAttr.get(acId).put(Constant.Timing, timingValue);
			}

			if (assessHeader.contains(Constant.AssessmentDescriptor)) {
				String acDescValue = acHeader.getDescriptor();
				allAcHeaderAttr.get(acId).put(Constant.AssessmentDescriptor, acDescValue);
			}

			if (assessHeader.contains(Constant.Creator)) {
				String creator = acHeader.getCreator();
				allAcHeaderAttr.get(acId).put(Constant.Creator, creator);
			}

			if (assessHeader.contains(Constant.Evaluator)) {
				String evaluator = acHeader.getEvaluator();
				allAcHeaderAttr.get(acId).put(Constant.Evaluator, evaluator);
			}

		}

		return allAcHeaderAttr;

	}

	/**
	 * Set Kpac Attributes to all Assessment Occurrences
	 * 
	 * @param config
	 * @throws com.emergentsys.auros.services.aurosbase.E2KSFault_Exception
	 */
	public void setKPacAttr(Config config) throws com.emergentsys.auros.services.aurosbase.E2KSFault_Exception {
		List<String> kpacAcList = new ArrayList<String>(kpacAcMap.keySet());
		List<FetchAssKPacs> fetchAssOccThreads = new ArrayList<FetchAssKPacs>();
		Vector<String> assessHeader = config.getAssessHeader();

		logger.debug(kpacAcList.size() + " unique kPacs found, processing start...");
		//System.out.println(kpacAcList.size() + " unique kPacs found, processing start...");

		int size = kpacAcList.size();
		int chunkSize = config.getChunkSizeKpac();
		int endSub = 0;

		int connections = Utils.mod(size, chunkSize);

		if (connections > Constant.maxConnections) {

			chunkSize = Utils.mod(size, 190);
			connections = Utils.mod(size, chunkSize);
			System.out.println("Using Chunksize of " + chunkSize + " for maximum Connections of 190\n");
		}

		numOfKpacs = kpacAcList.size();
		String procKPac = "Processing " + numOfKpacs + " KPacs in " + connections + " parallel connections";

		ProgressBarTraditional kPacProgressBar = new ProgressBarTraditional(procKPac);
		kPacProgressBar.start();

		if (size > chunkSize) {

			for (int i = 0; i < size; i = i + chunkSize) {

				if (size > i + chunkSize) {
					endSub = i + chunkSize;
				} else {
					endSub = size;
				}

				List<String> subKpacAcList = kpacAcList.subList(i, endSub);
				FetchAssKPacs fetchAssKPacThread = new FetchAssKPacs(this.binding, e2KSBasePortType, config,
						subKpacAcList, kpacAcMap, fileAppenders, startTime);
				fetchAssOccThreads.add(fetchAssKPacThread);
				fetchAssKPacThread.start();

			}
		} else {
			FetchAssKPacs fetchAssKPacThread = new FetchAssKPacs(this.binding, e2KSBasePortType, config, kpacAcList,
					kpacAcMap, fileAppenders, startTime);
			fetchAssOccThreads.add(fetchAssKPacThread);
			fetchAssKPacThread.start();
		}

		for (int i = 0; i < fetchAssOccThreads.size(); i++) {
			FetchAssKPacs fetchAssKPacThread = fetchAssOccThreads.get(i);
			try {
				fetchAssKPacThread.join();
			} catch (InterruptedException e) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				String crrntTime = format.format(new Date());
				logger.error(e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";"
						+ "CHECKLIST");
				e.printStackTrace();
				System.exit(1);
			}
		}

		for (Entry<String, AssessReportOcc> entry : assessmentResult.entrySet()) {
			AssessReportOcc occ = entry.getValue();
			String kpacKey = occ.getKpacKey();

			KPacAc kPacAc = kpacAcMap.get(kpacKey);
			KPac kpac = kPacAc.getKpac();

			if (kpac != null && assessHeader.contains(Constant.Title)) {
				KPacHeader kpacHeader = kpac.getKpacHeader();
				String title = " ";
				if (kpacHeader == null || kpacHeader.getTitle() == null) {
					occ.setAssessValue(Constant.Title, title);
				} else {
					title = kpac.getKpacHeader().getTitle();
					occ.setAssessValue(Constant.Title, title);
				}

			}

			if (kpac != null && assessHeader.contains(Constant.Author)) {
				String author = kpac.getAuthor();
				if (author == null) {
					author = " ";
				}
				occ.setAssessValue(Constant.Author, author);
			}

			if (kpac != null && assessHeader.contains(Constant.Type)) {
				String kpacType = " ";
				kpacType = kpac.getKpacGroupType();
				occ.setAssessValue(Constant.Type, kpacType);
			}

			if (kpac != null && assessHeader.contains(Constant.AdditionalInformation)) {
				String additionalInfo = " ";
				additionalInfo = kpac.getAdditionalInfo();
				if (additionalInfo != null) {
					Document doc = Jsoup.parse(additionalInfo);
					HtmlToPlainText formatter = new HtmlToPlainText();
					additionalInfo = formatter.getPlainText(doc);
					occ.setAssessValue(Constant.AdditionalInformation, additionalInfo);
				} else {
					additionalInfo = " ";
				}

			}

			if (kpac != null && assessHeader.contains(Constant.Justification)) {
				String justification = kpac.getJustification();
				occ.setAssessValue(Constant.Justification, justification);
			}

			if (kpac != null && assessHeader.contains(Constant.Description)) {
				String description = " ";
				KPacHeader kpacHeader = kpac.getKpacHeader();
				if (kpacHeader != null) {
					description = kpacHeader.getDescription();
				}

				if (description != null) {
					Document doc = Jsoup.parse(description);
					HtmlToPlainText formatter = new HtmlToPlainText();
					String plainText = formatter.getPlainText(doc);
					occ.setAssessValue(Constant.Description, plainText);
				} else {
					description = " ";
				}

			}

			if (kpac != null) {
				List<E2KSAttribute> kpacAttributes = kpac.getKpacAttributes();
				if (kpacAttributes != null) {
					for (int i = 0; i < kpacAttributes.size(); i++) {
						E2KSAttribute attr = kpacAttributes.get(i);
						if (attr == null)
							continue;

						String attributeName = attr.getAttributeName();
						if (attributeName == null)
							continue;

						List<String> attributeValue = attr.getAttributeValue();

						if (attributeValue != null && !attributeValue.isEmpty()) {
							if (assessHeader.contains(attributeName)) {
								String value = attributeValue.toString();
								occ.setAssessValue(attributeName, value);
							}

						}

					}

				}

			}

		}
	}

	/**
	 * Get the KPac Attributes for a single Occurrence
	 * 
	 * @param occ
	 * @param config
	 * @return
	 * @throws com.emergentsys.auros.services.aurosbase.E2KSFault_Exception
	 */
	@SuppressWarnings("unused")
	private AssessReportOcc getKPacAttr(AssessReportOcc occ, Config config)
			throws com.emergentsys.auros.services.aurosbase.E2KSFault_Exception {

		if (e2KSBasePortType == null) {
			e2KSBasePortType = binding.initBasePortAndCookie(config);
		}

		Vector<String> assessHeader = config.getAssessHeader();

		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<KPac> result = new Holder<KPac>();

		List<InputOptions> inputOptions = new ArrayList<InputOptions>();
		InputOptions inputOption = new InputOptions();
		inputOption.setName("queryKPacAttributes");
		inputOption.setValue("true");
		inputOptions.add(inputOption);
		Occurrence assessmentOcc = occ.getAssessmentOcc();

		e2KSBasePortType.getKPac(assessmentOcc.getCopID(), assessmentOcc.getKpacID(), assessmentOcc.getKpacVersion(),
				inputOptions, result, messages);
		KPac kpac = result.value;

		if (kpac != null && assessHeader.contains(Constant.Title)) {
			KPacHeader kpacHeader = kpac.getKpacHeader();
			String title = " ";
			if (kpacHeader == null || kpacHeader.getTitle() == null) {
				occ.setAssessValue(Constant.Title, title);
			} else {
				title = kpac.getKpacHeader().getTitle();
				occ.setAssessValue(Constant.Title, title);
			}

		}

		if (kpac != null && assessHeader.contains(Constant.Author)) {
			String author = kpac.getAuthor();
			if (author == null) {
				author = " ";
			}
			occ.setAssessValue(Constant.Author, author);
		}

		if (kpac != null && assessHeader.contains(Constant.Type)) {
			String kpacType = " ";
			kpacType = kpac.getKpacGroupType();
			occ.setAssessValue(Constant.Type, kpacType);
		}

		if (kpac != null && assessHeader.contains(Constant.AdditionalInformation)) {
			String additionalInfo = " ";
			additionalInfo = kpac.getAdditionalInfo();
			if (additionalInfo != null) {
				Document doc = Jsoup.parse(additionalInfo);
				HtmlToPlainText formatter = new HtmlToPlainText();
				additionalInfo = formatter.getPlainText(doc);
				occ.setAssessValue(Constant.AdditionalInformation, additionalInfo);
			} else {
				additionalInfo = " ";
			}

		}

		if (kpac != null && assessHeader.contains(Constant.Justification)) {
			String justification = kpac.getJustification();
			occ.setAssessValue(Constant.Justification, justification);
		}

		if (kpac != null && assessHeader.contains(Constant.Description)) {
			String description = " ";
			KPacHeader kpacHeader = kpac.getKpacHeader();
			if (kpacHeader != null) {
				description = kpacHeader.getDescription();
			}

			if (description != null) {
				Document doc = Jsoup.parse(description);
				HtmlToPlainText formatter = new HtmlToPlainText();
				String plainText = formatter.getPlainText(doc);
				occ.setAssessValue(Constant.Description, plainText);
			} else {
				description = " ";
			}

		}

		if (kpac != null) {
			List<E2KSAttribute> kpacAttributes = kpac.getKpacAttributes();
			if (kpacAttributes != null) {
				for (int i = 0; i < kpacAttributes.size(); i++) {
					E2KSAttribute attr = kpacAttributes.get(i);
					if (attr == null)
						continue;

					String attributeName = attr.getAttributeName();
					if (attributeName == null)
						continue;

					List<String> attributeValue = attr.getAttributeValue();

					if (attributeValue != null && !attributeValue.isEmpty()) {
						if (assessHeader.contains(attributeName)) {
							String value = attributeValue.toString();
							occ.setAssessValue(attributeName, value);
						}

					}

				}

			}

		}

		return occ;

	}

	public int getNumOfAssessments() {
		return numOfAssessments;
	}

	public int getNumOfKpacs() {
		return numOfKpacs;
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

}
