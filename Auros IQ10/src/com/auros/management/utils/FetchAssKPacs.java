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
import com.auros.model.Config;
import com.auros.model.KPacAc;
import com.auros.utils.StopWatch;
import com.emergent.e2ks.types.AurosIdentifier;
import com.emergent.e2ks.types.AurosMessages;
import com.emergent.e2ks.types.InputOptions;
import com.emergent.e2ks.types.KPac;
import com.emergentsys.auros.services.aurosbase.E2KSBasePortType;
import com.emergentsys.auros.services.aurosbase.E2KSFault_Exception;

public class FetchAssKPacs extends Thread {
	private E2KSBasePortType e2KSBasePortType;
	private Map<String, KPacAc> kpacAcMap;
	private List<String> subKpacAcList;
	private Config config;
	private Binding binding;
	private Logger logger = Logger.getLogger(FetchAssKPacs.class);
	private String startTime = null;

	public FetchAssKPacs(Binding binding, E2KSBasePortType e2KSBasePortType, Config config, List<String> subKpacAcList,
			Map<String, KPacAc> kpacAcMap, Map<String, FileAppender> fileAppenders, String startTime) {
		this.binding = binding;
		this.e2KSBasePortType = e2KSBasePortType;
		this.config = config;
		this.subKpacAcList = subKpacAcList;
		this.kpacAcMap = kpacAcMap;
		this.startTime = startTime;
		for (Map.Entry<String, FileAppender> entry : fileAppenders.entrySet()) {
			logger.addAppender(fileAppenders.get(entry.getKey()));
		}
	}

	public void run() {
		try {
			
			List<AurosIdentifier> aiList = new ArrayList<AurosIdentifier>();
			
			for (int i = 0; i < subKpacAcList.size(); i++) {
				String string = subKpacAcList.get(i);
				
				AurosIdentifier ai= new AurosIdentifier();
				List<String> identifiers = ai.getIdentifiers();
				
				if(identifiers != null) {
					identifiers.add(string);
					aiList.add(ai);
				}else {
					System.out.println("no identifier for ai");
				}

				getKPacAttr(subKpacAcList.get(i), config);
			}
			
			// getKpacByList(aiList);

		} catch (Exception e) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String crrntTime = format.format(new Date());
			logger.error(
					e.getMessage().replaceAll("\\r|\\n", " ") + ";" + startTime + ";" + crrntTime + ";" + "CHECKLIST");
			e.printStackTrace();
			System.exit(1);
		}
	}

	
	@SuppressWarnings("unused")
	private void getKpacByList(List<AurosIdentifier> kPacAcList)  {
		if (e2KSBasePortType == null) {
			e2KSBasePortType = binding.initBasePortAndCookie(config);
		}

		List<InputOptions> inputOptions = new ArrayList<InputOptions>();
		Holder<AurosMessages> messages = new Holder<AurosMessages>();
		Holder<KPac> result = new Holder<KPac>();
		try {
			e2KSBasePortType.getListedKPacs(kPacAcList, inputOptions, null, messages);
		} catch (E2KSFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		KPac kpac = result.value;
		
		for(int i = 0; i < kPacAcList.size();i++) {
//			KPacAc kPacAc = kpacAcMap.get(kPacAcList.get(i));
//			kPacAc.setKpac(kpac);
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
	private void getKPacAttr(String kpacKey, Config config)
			throws com.emergentsys.auros.services.aurosbase.E2KSFault_Exception {

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

		KPacAc kPacAc = kpacAcMap.get(kpacKey);
		

		// replace next line with "getListedKpacs" implementation (not just excehange
		// line!)
		e2KSBasePortType.getKPac(kPacAc.getCopID(), kPacAc.getKpacID(), kPacAc.getKpacVersion(), inputOptions, result,messages);
	
		KPac kpac = result.value;
		
		kPacAc.setKpac(kpac);
		
	}
}
