package com.auros.model;

import java.util.HashMap;
import java.util.Map;

public class ValidArgs {
	String name = "";
	String type = "";
	Map<String, String> valueMap = new HashMap<String, String>();
	Map<String, String> keyMap = new HashMap<String, String>();

	public ValidArgs() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public void addValueMap(String dispName, String internalName) {
		this.valueMap.put(dispName, internalName);
	}
	
	public Map<String, String> getKeyMap() {
		return keyMap;
	}

	public void addKeyMap(String dispName, String internalName) {
		this.keyMap.put(dispName, internalName);
	}

}
