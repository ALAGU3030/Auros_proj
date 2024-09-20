package com.auros.credentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * This class prompts the user for a password and attempts to mask input with
 * "*"
 */

public class PasswordField {

	public PasswordField() {
		System.out.println("Please enter user credentials (return to quit):");
	}

	public String readPassword(String prompt) {
		String password = "";

		try {
			Terminal terminal = TerminalBuilder.terminal();
			LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
			Character mask = '*';

			while (password.length() == 0) {
				password = reader.readLine(prompt, mask);
			}

		} catch (IOException ex) {
			final JPasswordField pf = new JPasswordField();
			password = JOptionPane.showConfirmDialog(null, pf, prompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION ? new String(pf.getPassword()) : "";
		}

		return password;
	}

	public String readUserName(String prompt) {
		String userName = null;

		try {
			Terminal terminal = TerminalBuilder.terminal();
			LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
			userName = reader.readLine(prompt);

		} catch (IOException e) {
			System.out.print(prompt);

			// open up standard input
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// read the username from the command-line; need to use try/catch with the
			// readLine() method
			try {
				userName = br.readLine();
			} catch (IOException ioe) {
				System.out.println("IO error trying to read your name!");
				System.exit(1);
			}

		}

		return userName;

	}

	public static String readUserNameOld(String message) {
		System.out.print(message);

		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String userName = null;

		// read the username from the command-line; need to use try/catch with the
		// readLine() method
		try {
			userName = br.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your name!");
			System.exit(1);
		}
		return userName;
	}

}
