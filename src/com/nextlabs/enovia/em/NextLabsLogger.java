package com.nextlabs.enovia.em;
/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.nextlabs.enovia.common.NextLabsConstant;


public final class NextLabsLogger implements NextLabsConstant {

	private final transient Logger logg; // NOPMD by klee on 6/6/12 3:09 PM

	public transient boolean isNXLDebugEnable = false;

	private static String sConfigPath = null;

	static {
		sConfigPath = NextLabsAgentUtil.getConfigPath();
	}

	/**
	 * @param log	
	 */
	public NextLabsLogger(final Logger logger) {
		logg = logger;
		
		if (sConfigPath!=null){
			// Configure log4j rolling properties
			PropertyConfigurator.configure(sConfigPath + LOG4J_CONFIG_FILE);
		}
		else{
			configureLog4jManually(logg);
			error("Config path is null, manually configure log4j");
		}

		if (logg.isDebugEnabled()) {
			isNXLDebugEnable = true;
		}
	}
	
	private void configureLog4jManually(final Logger logger) {

		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		// add appender to any Logger (here is root)

		FileAppender fa = new FileAppender();
		fa.setName("EMLOG");
		fa.setFile("nextlabsEM.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c|%C{1}] %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();

		// add appender to any Logger (here is root)
		
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		
		Logger em = logger.getLoggerRepository().getLogger("EMLOGGER");
		em.setLevel(Level.DEBUG);
		em.addAppender(console);
		em.addAppender(fa);
	
		
		// repeat with all other desired appenders
	}
	
	/**
	 * @param formatter
	 * @param args
	 */
	public void trace(final String formatter, final Object... args) {
		log(Level.TRACE, formatter, args);
	}
	
	/**
	 * @param formatter
	 * @param args
	 */
	public void debug(final String formatter, final Object... args) {
		log(Level.DEBUG, formatter, args);
	}

	/**
	 * @param formatter
	 * @param args
	 */
	public void info(final String formatter, final Object... args) {
		log(Level.INFO, formatter, args);
	}

	/**
	 * @param formatter
	 * @param args
	 */
	public void fatal(final String formatter, final Object... args) {
		log(Level.FATAL, formatter, args);
	}

	/**
	 * @param formatter
	 * @param args
	 */
	public void error(final String formatter, final Object... args) {
		log(Level.ERROR, formatter, args);
	}

	/**
	 * @param formatter
	 * @param thr
	 * @param args
	 */
	public void error(final String formatter, final Throwable thr ,final Object... args) {
		log(Level.ERROR, formatter, thr, args);
	}

	/**
	 * @param level
	 * @param formatter
	 * @param args
	 */
	public void log(final Level level, final String formatter, final Object... args) {
		if (logg.isEnabledFor(level)) {
			/* 
			 * Now the message is constructed with the invocation of toString()
			 */
			logg.log(level, String.format(formatter, args));
		}
	}

	/**
	 * @param level
	 * @param formatter
	 * @param thr
	 * @param args
	 */
	public void log(final Level level, final String formatter, final Throwable thr, final Object... args) {
		if (logg.isEnabledFor(level)) {
			/* 
			 * Now the message is constructed with the invocation of toString()
			 */
			logg.log(level, String.format(formatter, args), thr);
		}
	}

}
