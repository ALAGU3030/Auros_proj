package com.auros.connector;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.auros.credentials.AurosCredentials;
import com.auros.model.Config;
import com.auros.model.Constant;
import com.emergentsys.auros.services.assessments.E2KSAssessmentsPortType;
import com.emergentsys.auros.services.assessments.E2KSAssessmentsService;
import com.emergentsys.auros.services.aurosbase.E2KSBasePortType;
import com.emergentsys.auros.services.aurosbase.E2KSBaseService;
import com.emergentsys.auros.services.issues.E2KSIssuesPortType;
import com.emergentsys.auros.services.issues.E2KSIssuesService;

public class Binding {

	private AurosCredentials auth = null;
	private static Logger logger = Logger.getLogger(Binding.class);

	public Binding(AurosCredentials auth, Map<String, FileAppender> fileAppenders) {
		this.auth = auth;
		
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
	      }	
	}

	public E2KSAssessmentsPortType initACPortAndCookie(Config config) {
		E2KSAssessmentsPortType assessmentServiceAndPort = createAssessmentServiceAndPort();
		bindCookieToACPort(config, assessmentServiceAndPort);
		return assessmentServiceAndPort;
	}

	public E2KSBasePortType initBasePortAndCookie(Config config) {
		E2KSBasePortType baseServiceAndPort = createBaseServiceAndPort();
		bindCookieToBasePort(config, baseServiceAndPort);
		return baseServiceAndPort;
	}

	public E2KSIssuesPortType initIssuesPortAndCookie(Config config) {
		E2KSIssuesPortType issuesServiceAndPort = createIssueServiceAndPort();
		bindCookieToIssuesPort(config, issuesServiceAndPort);
		return issuesServiceAndPort;
	}

	private void bindCookieToACPort(Config config, E2KSAssessmentsPortType e2KSAssessmentsPortType) {
		if (e2KSAssessmentsPortType instanceof BindingProvider) {
			BindingProvider acBinding = (BindingProvider) e2KSAssessmentsPortType;
			Map<String, Object> requestContext = acBinding.getRequestContext();
			String assessmentURL = config.getAurosServices().get(Constant.assessmentService);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, assessmentURL);
			requestContext.put(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
			requestContext.put(BindingProvider.USERNAME_PROPERTY, auth.getUserName());
			requestContext.put(BindingProvider.PASSWORD_PROPERTY, auth.getUserPw());
		}
	}

	private void bindCookieToBasePort(Config config, E2KSBasePortType e2KSBasePortType) {
		if (e2KSBasePortType instanceof BindingProvider) {
			BindingProvider baseBinding = (BindingProvider) e2KSBasePortType;
			Map<String, Object> requestContext = baseBinding.getRequestContext();
			String baseURL = config.getAurosServices().get(Constant.baseService);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL);
			requestContext.put(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
			requestContext.put(BindingProvider.USERNAME_PROPERTY, auth.getUserName());
			requestContext.put(BindingProvider.PASSWORD_PROPERTY, auth.getUserPw());
			
		}
	}

	private void bindCookieToIssuesPort(Config config, E2KSIssuesPortType e2ksIssuesPortType) {
		if (e2ksIssuesPortType instanceof BindingProvider) {
			BindingProvider issueBinding = (BindingProvider) e2ksIssuesPortType;
			Map<String, Object> requestContext = issueBinding.getRequestContext();
			String baseURL = config.getAurosServices().get(Constant.issuesService);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL);
			requestContext.put(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
			requestContext.put(BindingProvider.USERNAME_PROPERTY, auth.getUserName());
			requestContext.put(BindingProvider.PASSWORD_PROPERTY, auth.getUserPw());
		}
	}

	private E2KSAssessmentsPortType createAssessmentServiceAndPort() {

		URL acWsdlLocation = getClass().getResource("/com/auros/connector/schema/E2ksAssessments.wsdl");
		
		
		String logAssessmentServiceStart = "Before calling Constructor of E2KSAssessmentsService";
		logger.debug(logAssessmentServiceStart);

		E2KSAssessmentsService acService = new E2KSAssessmentsService(acWsdlLocation,
				new QName(Constant.assessmentServiceNs, Constant.assessmentService));

		String logAssessmentServicePort = "After calling Constructor of E2KSAssessmentsService and before calling getE2KSAssessmentsPort";
		logger.debug(logAssessmentServicePort);

		E2KSAssessmentsPortType e2ksAssessmentsPort = acService.getE2KSAssessmentsPort(new MTOMFeature());

		String logAssessmentServiceReady = "After calling getE2KSAssessmentsPort";
		logger.debug(logAssessmentServiceReady);

		return e2ksAssessmentsPort;
	}

	private E2KSBasePortType createBaseServiceAndPort() {
		URL baseWsdlLocation = getClass().getResource("/com/auros/connector/schema/E2ksBase.wsdl");
		E2KSBaseService baseService = new E2KSBaseService(baseWsdlLocation,
				new QName(Constant.baseServiceNs, Constant.baseService));
		E2KSBasePortType e2ksBasePort = baseService.getE2KSBasePort(new MTOMFeature());

		String logBaseServiceAndPort = "com.emergentsys.auros.services.aurosbase.E2KSBaseService.getE2KSBasePort";
		logger.debug(logBaseServiceAndPort);

		return e2ksBasePort;
	}

	private E2KSIssuesPortType createIssueServiceAndPort() {
		URL issueWsdlLocation = getClass().getResource("/com/auros/connector/schema/E2ksIssues.wsdl");

		String logIssueServiceStart = "Before calling Constructor of E2KSIssuesService";
		logger.debug(logIssueServiceStart);

		E2KSIssuesService issuesService = new E2KSIssuesService(issueWsdlLocation,
				new QName(Constant.issuesServiceNs, Constant.issuesService));

		String logIssueServicePort = "After calling Constructor of E2KSIssuesService and before calling getE2KSIssuesPort";
		logger.debug(logIssueServicePort);

		E2KSIssuesPortType e2ksIssuesPort = issuesService.getE2KSIssuesPort(new MTOMFeature());

		String logIssueServiceReady = "After calling getE2KSIssuesPort";
		logger.debug(logIssueServiceReady);

		return e2ksIssuesPort;
	}

}
