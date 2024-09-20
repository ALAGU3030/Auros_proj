package com.auros.credentials;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.auros.model.Config;
import com.auros.model.Constant;

public class AurosCredentials {
	private List<String> argList = null;
	private Config config;
	private String userName = "";
	private String passwd = "";
	private PasswordField pw;

	public AurosCredentials(Config config, List<String> argList) {
		this.config = config;
		this.argList = argList;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPw() {
		return passwd;
	}

	public void promptForCredentials(boolean debug) throws IOException {
		if (debug) {
			fileBasedPasswdCredentials();
		} else {
			if (config.doPromptForCredentials()) {
				this.pw = new PasswordField();
				promptForCredentials();
			} else {
				setCredentialsByParameter();
			}

		}

	}

	private void fileBasedPasswdCredentials() throws IOException {

		System.out.println("Please enter user credentials (return to quit):");

		userName = pw.readUserName("User Name> ");

		if (userName.length() == 0) {
			System.out.println("No UserName provided - Exit Application");
			System.exit(1);
		}

		FileInputStream fis = new FileInputStream(new File("src\\com\\auros\\credentials\\passwd.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		passwd = br.readLine();
		br.close();

	}

	private void promptForCredentials() throws IOException {

		userName = pw.readUserName("User Name> ");

		if (userName.length() == 0) {
			System.out.println("No UserName provided - Exit Application");
			System.exit(1);
		}

		passwd = pw.readPassword("Enter password> ");

	}

	private void setCredentialsByParameter() throws IOException {

		for (int i = 0; i < argList.size(); i++) {
			String name = "";
			String value = "";

			String arg = argList.get(i);
			String[] argPair = arg.split(Constant.EQUAL);
			if (argPair != null && argPair.length == 2) {
				name = argPair[0];
				value = argPair[1];

			}

			if (name.equalsIgnoreCase("user")) {
				userName = value;
			}

			if (name.equalsIgnoreCase("passwd")) {
				passwd = value;
			}

		}

		if (userName.isEmpty() || passwd.isEmpty()) {
			System.out.println("Missing User and Password required by configuration PromptForCredentials=\"false\"");
			System.out.println("Please fix and start Report Tool again");
			System.exit(1);
		}

	}

}
