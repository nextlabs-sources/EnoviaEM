package com.nextlabs.enovia.em;

/*
 * Created on September 11, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.IEncryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.enovia.common.NextLabsConstant;


public class NextLabsAgentUtil {
	
	private static String OS = null;
	
	public NextLabsAgentUtil() {
	}
	
	/**
	 * Getting the hostname for server currently running
	 * @return Hostname for the server that running this application
	 * @throws Exception
	 */
	public static String getHostName() throws UnknownHostException {
		String hostname = null;
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			throw e;
		}
		
		return hostname;
	}
	
	/**
	 * Function to validate whether the IP is in valid form
	 * @param ipAddress IP address in string format e.g 127.0.0.1
	 * @return true if the IP address is valid, otherwise false
	 */
	public static boolean isIPAddressValid(String ipAddress) {
		String[] parts = ipAddress.split("\\.");
		
		if (parts.length != 4) {
			return false;
		}
		
		for (String s : parts) {
			int i = Integer.parseInt(s);
			
			if ((i < 0) || (i > 255)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Convert the IP from string form to integer form
	 * @param addr IP address in string format e.g 127.0.0.1
	 * @return IP address in integer format for example 0 for IP address 127.0.0.1
	 */
	public static int ipToInt(String addr) {
		String[] addrArray = addr.split("\\.");
		
		int num = 0;
		for (int i = 0; i < addrArray.length; i++) {
			int power = 3 - i;
			
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
		}
		
		return num;
	}
	
	/**
	 * Convert the IP from integer to string form
	 * @param i IP address in the format of integer, e.g 0
	 * @return IP address in the expression of string e.g 127.0.0.1
	 */
	public static String intToIp(int iIP) {
		return ((iIP >> 24 ) & 0xFF) + "." + 
				((iIP >> 16 ) & 0xFF) + "." + 
				((iIP >>  8 ) & 0xFF) + "." + 
				( iIP        & 0xFF);
	}
	
	public static String getOsName() {
		if (OS == null) { 
			OS = System.getProperty("os.name"); 
		}
		
		return OS;
	}
	
	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}
	
	public static String getConfigPath() {
		String sPath = null;
		
		// For windows and Solaris system checking
		if (isWindows()) {
			// Studio platform 
			if (System.getProperty("user.dir").toLowerCase().indexOf("studio") > -1) {
				sPath = System.getenv("STUDIO_CUSTOM_APP_HOME");
				
				if (null != sPath) {
					System.setProperty("STUDIO_CUSTOM_APP_HOME",sPath);
					sPath = sPath + NextLabsConstant.NXL_WIN_CONFIG_SUB_PATH;
				}
			} else {
				sPath = System.getenv("DSPACE_CUSTOM_APP_HOME");
				
				if (null != sPath) {
					System.setProperty("DSPACE_CUSTOM_APP_HOME",sPath);
					sPath = sPath + NextLabsConstant.NXL_WIN_CONFIG_SUB_PATH;
				}
			}
		} else {
			// Studio platform 
			if (System.getProperty("user.dir").toLowerCase().indexOf("studio") > -1) {
				sPath = System.getenv("STUDIO_CUSTOM_APP_HOME");
				
				if (null != sPath) {
					System.setProperty("STUDIO_CUSTOM_APP_HOME",sPath);
					sPath = sPath + NextLabsConstant.NXL_SOL_CONFIG_SUB_PATH;
				}
			} else {
				sPath = System.getenv("DSPACE_CUSTOM_APP_HOME");
				
				if (null != sPath) {
					System.setProperty("DSPACE_CUSTOM_APP_HOME",sPath);
					sPath = sPath + NextLabsConstant.NXL_SOL_CONFIG_SUB_PATH;
				}
			}
		}
		
		return sPath;		
	}
	
	public static String decryptPassword(String encryptedPassword) {
		IDecryptor decryptor = new ReversibleEncryptor();
		return decryptor.decrypt(encryptedPassword);
	}	

	public static String encryptPassword(String password) {
		IEncryptor encryptor = new ReversibleEncryptor();
		return encryptor.encrypt(password);
	}

}
