package com.auros.management.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.*;

import javax.xml.ws.Holder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.auros.management.IssueManagement;
import com.auros.model.Config;
import com.emergent.e2ks.types.AurosIdentifier;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.InputOptions;
import com.emergent.e2ks.types.issue.Issue;
import com.emergentsys.auros.services.issues.E2KSIssuesPortType;

public class FetchIssues extends Thread {
	private E2KSIssuesPortType e2KSIssuesPortType;
	private List<AurosIdentifier> issueIdentifiers;
	private IssueManagement issueManagement;
	private Config config;
	private Logger logger = Logger.getLogger(FetchIssues.class);
	private String startTime = null;

	public FetchIssues(IssueManagement issueManagement, Config config, E2KSIssuesPortType e2KSIssuesPortType,
			List<AurosIdentifier> issueIdentifiers, Map<String, FileAppender> fileAppenders, String startTime) {
		this.issueManagement = issueManagement;
		this.e2KSIssuesPortType = e2KSIssuesPortType;
		this.issueIdentifiers = issueIdentifiers;
		this.config = config;
		this.startTime = startTime;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
	}

	public void run() {
		try {
			List<InputOptions> options = new ArrayList<InputOptions>();
			Holder<List<Issue>> issueListResult = new Holder<List<Issue>>();
			
//			System.out.println("33333333   issue result is :"+issueListResult.value);
//			System.out.println("444444444444"+issueListResult);
			Holder<AurosMessages> messages = null;

			logger.debug("start getIssues");
			try {
//				if(issueIdentifiers.get(0).getIdentifiers().get(0) !="365336")
//				{
//					e2KSIssuesPortType.getIssues(issueIdentifiers, options, issueListResult, messages);
//				}	
//				for(AurosIdentifier i:issueIdentifiers)
//				{
//					int count=0;
//					System.out.println(++count+"------------"+i.getIdentifiers());
////					List<String> identifiersToCheck = Arrays.asList("377759","377635","390833","365896");378307
//					
//					List<String> identifiersToCheck = Arrays.asList("377759","377635","390833","365896","378307","380016","391715");
//					if(!i.getIdentifiers().contains(identifiersToCheck))
//					{
//						
//					List<AurosIdentifier> li=new ArrayList();
//					li.add(i);
//				e2KSIssuesPortType.getIssues(li, options, issueListResult, messages);
//					}
//				
//				}
			
//				List<String> li=new ArrayList<String>();
//				li.add("391715");
//				li.add("375278");
//				if(!li.contains(issueIdentifiers.get(0).getIdentifiers().get(0)))
//				{
//						e2KSIssuesPortType.getIssues(issueIdentifiers, options, issueListResult, messages);
//			     }	
				
//				}
				e2KSIssuesPortType.getIssues(issueIdentifiers, options, issueListResult, messages);
				
				//System.out.println("issue list....."+issueIdentifiers)
				
				//System.out.println("issue list....."+issueIdentifiers)
				
			} 
			catch (Exception e) {
//                  System.out.println("test"+issueIdentifiers);
				e.printStackTrace();
				String id = issueIdentifiers.get(0).getIdentifiers().get(0);
				String issuedesc = issueListResult.value.get(0).getIssueDescription();
//				System.out.println("\n\nFound bad Issue with Desc:" + issuedesc + "\n\n");
//				System.out.println("\n\nFound bad Issue with ID:" + id + "\n\n");
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				String crrntTime = format.format(new Date());
				logger.error(e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";"
						+ "ISSUELIST");
				logger.error("Bad Issue ID:" + id + ";" + startTime + ";" + crrntTime + ";" + "ISSUELIST");
				e.printStackTrace();

				if (config.isChunkSizeOne()) {
					return;
				} else {
					System.exit(2);
				}

			}

			logger.debug("stop getIssues");

			List<Issue> result = issueListResult.value;
			issueManagement.addToIssueList(issueListResult.value);
			logger.debug("saved " + result.size() + " Issues to list");

		} catch (Exception e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "ISSUELIST");
			e.printStackTrace();
			System.exit(1);
		}
	}
}