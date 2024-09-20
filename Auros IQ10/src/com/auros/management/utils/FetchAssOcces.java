package com.auros.management.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.auros.connector.Binding;
import com.auros.management.AssessmentManagement;
import com.auros.model.Config;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.InputOptions;
import com.emergent.e2ks.types.asses.Occurrence;
import com.emergentsys.auros.services.assessments.E2KSAssessmentsPortType;
import com.emergentsys.auros.services.assessments.E2KSFault_Exception;

public class FetchAssOcces extends Thread {
	private E2KSAssessmentsPortType e2KSAssessmentsPortType;
	private Config config;
	private List<String> iDs;
	private AssessmentManagement assessmentManagement;
	private Binding binding;
	private Logger logger = Logger.getLogger(FetchAssOcces.class);
	private String startTime = null;

	public FetchAssOcces(AssessmentManagement assessmentManagement, Binding binding,
			E2KSAssessmentsPortType e2KSAssessmentsPortType, Config config, List<String> iDs,
			Map<String, FileAppender> fileAppenders, String startTime) {
		this.binding = binding;
		this.assessmentManagement = assessmentManagement;
		this.e2KSAssessmentsPortType = e2KSAssessmentsPortType;
		this.config = config;
		this.iDs = iDs;
		this.startTime = startTime;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
	      }

	}

	public void run() {
		try {
			processOccurrences(iDs, config);
		} catch (E2KSFault_Exception e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "CHECKLIST");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void processOccurrences(List<String> iDs, Config config) throws E2KSFault_Exception {

		String id = "";
		for (int i = 0; i < iDs.size(); i++) {
			id = iDs.get(i);
			List<InputOptions> inputOptions = null;
			inputOptions = new ArrayList<InputOptions>();

			Holder<AurosMessages> messages = new Holder<AurosMessages>();
			Holder<List<Occurrence>> result = new Holder<List<Occurrence>>();

			if (e2KSAssessmentsPortType == null) {
				e2KSAssessmentsPortType = binding.initACPortAndCookie(config);
			}

			e2KSAssessmentsPortType.getOccurrences(id, inputOptions, result, messages);

			List<Occurrence> assessmentOccList = result.value;
			assessmentManagement.setAssOccMap(id, assessmentOccList);
		}

	}

}