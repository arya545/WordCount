package com.floow.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//This class is to read the config.properties file
public class ReadProperties {

	InputStream inputstream;
	Properties prop;
	private String dbName;
	private String collectionName;

	// Method to read the property file in classpath
	public void getPropValues() {
		try {
			prop = new Properties();
			String propertyFileName = "config.properties";
			inputstream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputstream != null) {
				prop.load(inputstream);
			} else {
				throw new FileNotFoundException("Property file " + propertyFileName + " not found in the classpath ");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		dbName = prop.getProperty("databaseName");
		return dbName;
	}

	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		collectionName = prop.getProperty("collectionName");
		return collectionName;
	}

	/**
	 * @param dbName
	 *            the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = prop.getProperty("databaseName");
	}

	/**
	 * @param collectionName
	 *            the collectionName to set
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = prop.getProperty("collectionName");
	}

}
