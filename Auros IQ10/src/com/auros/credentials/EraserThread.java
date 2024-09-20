package com.auros.credentials;

class EraserThread implements Runnable {
	private boolean start;
	private boolean keyIn;

	/**
	 * @param The
	 *            prompt displayed to the user
	 */
	public EraserThread(String prompt) {
		System.out.print(prompt);
	}

	/**
	 * Begin masking...display asterisks (*)
	 */
	public void run() {
		start = true;
		keyIn = false;
		while (start) {
			if (keyIn) {
				System.out.print("\010*");
			}
			keyIn = true;

			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Instruct the thread to stop masking
	 */
	public void stopMasking() {
		this.start = false;
	}
}
