package com.auros.utils;

public class ProgressBarTraditional extends Thread {
	public boolean showProgress = true;
	private String output;

	public ProgressBarTraditional(String output) {
		this.output = output;
	}

	public void run() {
		if (System.console() != null) {

			String anim = "=====================";
			int x = 0;
			while (showProgress) {
				System.out.print("\r" + output + " " + anim.substring(0, x++ % anim.length()) + " ");
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}

			}
			

		} else {
			// we are running from Eclipse
			System.out.print("\n=========Eclipse:"+output+" =========");
		}

	}
}