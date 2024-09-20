package com.auros.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

public class JulianDate {
	private String dateStr = "";

	public JulianDate(String dateStr) {
		this.dateStr = dateStr;
	}

	public String getGregorianDate() {
		int julianInt = Integer.parseInt(dateStr);
		Date gregorianDate = convertToGregorin(julianInt);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy:mm-ss.SSS");
		String dateStrg = sdf.format(gregorianDate);		
		return dateStrg;
	}
	
	private static Date convertToGregorin(final int julian) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(-4712, 0, 1);
	    calendar.add(Calendar.DATE, julian);
	    return calendar.getTime();
	}
	
	public Integer findNextKey(Integer discussKey, TreeMap<Integer, String> discussMap) {
		Integer nextKey = discussKey;
		if(discussMap.get(discussKey)!= null) {
			nextKey = findNextKey(discussKey+1,discussMap);
		}
		
		return nextKey;
	}

}
