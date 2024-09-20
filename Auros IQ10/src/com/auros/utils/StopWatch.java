/*
 * utils4j - StopWatch.java, May 7, 2011 2:20:34 PM
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.auros.utils;

import java.util.Date;

/**
 * An Utility StopWatch which prints the elapsed time details in milli seconds.
 * 
 * @author Rajakrishna V. Reddy
 * @version 1.0
 * 
 */
public class StopWatch
{
	
	/** The start time. */
	private transient long startTime;
	
	/** The end time. */
	private transient long endTime;
	
	/** The elapsed time. */
	private transient long elapsedTime;
	
	/**
	 * Instantiates a new StopWatch with the startTime and endTime provided.
	 * 
	 * @param startTime
	 *            the start time
	 * @param endTime
	 *            the end time
	 */
	public StopWatch(final long startTime, final long endTime)
	{
		super();
		
		this.startTime = startTime;
		this.endTime = endTime;
		
		this.elapsedTime = endTime - startTime;
	}
	
	/**
	 * Instantiates a new StopWatch with the startTime and endTime provided.
	 * 
	 * @param startTime
	 *            the start time
	 * @param endTime
	 *            the end time
	 */
	public StopWatch(final Date startTime, final Date endTime)
	{
		super();
		
		this.startTime = startTime.getTime();
		this.endTime = endTime.getTime();
		
		this.elapsedTime = this.endTime - this.startTime;
	}
	
	/**
	 * Instantiates a new StopWatch.
	 */
	public StopWatch()
	{
		super();
		
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Reset the counter and start counting from scratch.
	 */
	public void reset()
	{
		this.startTime = System.currentTimeMillis();
		this.endTime = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		boolean isEndTimeNotSet = false; // pessimistic, this is to allow the
											// user to elapse the timer till he
											// prints the time.
		if (this.endTime == 0)
		{
			this.endTime = System.currentTimeMillis();
			isEndTimeNotSet = true;
		}
		this.elapsedTime = this.endTime - this.startTime;
		
		final StringBuilder builder = new StringBuilder();
		
		builder.append("Timer [startTime = ");
		builder.append(startTime);
		builder.append(" ms, endTime = ");
		builder.append(endTime);
		builder.append(" ms, elapsedTime = ");
		builder.append(elapsedTime);
		builder.append(" ms]");
		
		if (isEndTimeNotSet)
		{
			this.endTime = 0;
		}
		return builder.toString();
	}
	
	/**
	 * Gets the start time.
	 * 
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
	/**
	 * Gets the end time.
	 * 
	 * @return the endTime
	 */
	public long getEndTime()
	{
		return endTime;
	}
	
	/**
	 * Gets the elapsed time.
	 * 
	 * @return the elapsedTime
	 */
	public long getElapsedTime()
	{
		this.endTime = System.currentTimeMillis();
		
		return (elapsedTime = endTime - startTime);
	}
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args)
	{
		final StopWatch timer = new StopWatch();
		System.out.println("Timer: " + timer);
		System.out.println("ElapsedTime: " + timer.getElapsedTime());
	}
}