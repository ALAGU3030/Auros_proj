package com.auros.io;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.auros.model.Config;
import com.auros.model.Constant;
import com.auros.model.Constant.ReportType;
import com.auros.model.ValidArgs;


public class XmlConfigParser {
	
	private Document xmlDocument = null;
	private File configFile = Constant.configFile;
	private Config config = new Config();
	private ReportType reportType;

	public XmlConfigParser(String typeName) throws Exception {
		boolean isIssueType = false;
		if (configFile != null && configFile.canRead()) {
			this.reportType = Constant.ReportType.valueOf(typeName);
			XPathExpression argLogFile = XPathFactory.newInstance().newXPath().compile(Constant.argXPathLogDir);
			parseLogDir(argLogFile);
			switch (reportType) {
			case CHECKLIST:
				XPathExpression argCheckListExpr = XPathFactory.newInstance().newXPath().compile(Constant.argXPathCheckList);
				parseConfig(argCheckListExpr, isIssueType);
				break;
			case ISSUELIST:
				isIssueType = true;
				XPathExpression argIssueListExpr = XPathFactory.newInstance().newXPath().compile(Constant.argXPathIssueList);
				parseConfig(argIssueListExpr, isIssueType);
				break;

			default:
				break;
			}

		} else {
			throw new Exception("Config File: " + configFile.getAbsolutePath() + " not found");
		}

	}

	public Config getConfig() {
		return config;
	}


	private void setPromptForCredentials() throws XPathExpressionException {
		XPathExpression argPromptForCredentials = XPathFactory.newInstance().newXPath().compile(Constant.argXPathPromptForCredentials);
		Node promptItem = (Node) argPromptForCredentials.evaluate(xmlDocument, XPathConstants.NODE);
		String prompt = promptItem.getNodeValue();
		boolean readPrompt = Boolean.valueOf(prompt).booleanValue();
		config.setPromptForCredentials(readPrompt);
		if (readPrompt) {
			config.setParamStart(1);
		} else {
			config.setParamStart(3);
		}

	}
	
	private void parseLogDir(XPathExpression argExpr) throws Exception {
		String xmlFileUriString = configFile.toURI().toString();
		
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(xmlFileUriString);
		
		NodeList logDirNode = (NodeList) argExpr.evaluate(xmlDocument, XPathConstants.NODESET);
		Node item = logDirNode.item(0);
		String logDir = item.getNodeValue();
		config.setLogDir(logDir);
	}

	private void parseConfig(XPathExpression argExpr, boolean isIssueType) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		String xmlFileUriString = configFile.toURI().toString();

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(xmlFileUriString);

		setPromptForCredentials();

		NodeList argNL = (NodeList) argExpr.evaluate(xmlDocument, XPathConstants.NODESET);
		for (int i = 0; i < argNL.getLength(); i++) {

			Node item = argNL.item(i);
			NamedNodeMap attributes = item.getAttributes();

			Node nameNode = attributes.getNamedItem("Name");
			String name = nameNode.getNodeValue();

			Node typeNode = attributes.getNamedItem("Type");
			String type = typeNode.getNodeValue();

			ValidArgs validArgs = new ValidArgs();
			validArgs.setName(name);
			validArgs.setType(type);

			if (isIssueType && item.hasChildNodes()) {
				NodeList childNodes = item.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node childnode = childNodes.item(j);
					if (childnode.getNodeType() == Node.ELEMENT_NODE) {

						NamedNodeMap childAttr = childnode.getAttributes();
						Node nameMapNode = childAttr.getNamedItem("Name");
						Node idMapNode = childAttr.getNamedItem("ID");

						String dispName = nameMapNode.getNodeValue();
						String internalName = idMapNode.getNodeValue();

						validArgs.addValueMap(dispName, internalName);
						validArgs.addKeyMap(internalName, dispName);

					}

				}

			}

			config.addValidArgMap(name, validArgs);

		}

		XPathExpression assessAttrExpr = XPathFactory.newInstance().newXPath().compile(Constant.assessReportAttrXPath);
		NodeList assessAttrNL = (NodeList) assessAttrExpr.evaluate(xmlDocument, XPathConstants.NODESET);
		for (int i = 0; i < assessAttrNL.getLength(); i++) {
			Node item = assessAttrNL.item(i);
			NamedNodeMap attributes = item.getAttributes();

			Node orderNode = attributes.getNamedItem("Order");
			Node nameNode = attributes.getNamedItem("Name");
			Node dispNameNode = attributes.getNamedItem("DisplayName");

			int order = Integer.parseInt(orderNode.getNodeValue());
			String name = nameNode.getNodeValue();
			String dispName = dispNameNode.getNodeValue();

			config.addAssessHeader(order, name);
			config.addAssessDisplayHeader(order, dispName);

		}

		XPathExpression issueAttrExpr = XPathFactory.newInstance().newXPath().compile(Constant.issueReportAttrXPath);
		NodeList issueAttrNL = (NodeList) issueAttrExpr.evaluate(xmlDocument, XPathConstants.NODESET);
		for (int i = 0; i < issueAttrNL.getLength(); i++) {
			Node item = issueAttrNL.item(i);
			NamedNodeMap attributes = item.getAttributes();

			Node orderNode = attributes.getNamedItem("Order");
			Node nameNode = attributes.getNamedItem("Name");
			Node dispNameNode = attributes.getNamedItem("DisplayName");

			int order = Integer.parseInt(orderNode.getNodeValue());
			String name = nameNode.getNodeValue();
			String dispName = dispNameNode.getNodeValue();

			config.addIssueHeader(order, name);
			config.addIssueDisplayHeader(order, dispName);

		}

		XPathExpression wslExpr = XPathFactory.newInstance().newXPath().compile(Constant.wslXPath);
		Node wslNode = (Node) wslExpr.evaluate(xmlDocument, XPathConstants.NODE);
		config.setWslURL(wslNode.getNodeValue());

		XPathExpression aurosExpr = XPathFactory.newInstance().newXPath().compile(Constant.aurosServiceXPath);
		NodeList aurosNL = (NodeList) aurosExpr.evaluate(xmlDocument, XPathConstants.NODESET);

		for (int i = 0; i < aurosNL.getLength(); i++) {
			Node item = aurosNL.item(i);
			NamedNodeMap attributes = item.getAttributes();

			Node nameNode = attributes.getNamedItem("Name");
			String name = nameNode.getNodeValue();

			Node urlNode = attributes.getNamedItem("URL");
			String url = urlNode.getNodeValue();

			config.getAurosServices().put(name, url);

		}

	}

}
