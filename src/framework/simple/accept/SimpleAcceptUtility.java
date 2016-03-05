/*
 * Copyright 2016 Kashyap Deshpande.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package framework.simple.accept;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Utility class of the framework. 
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public class SimpleAcceptUtility {

	private static final String DEFAULT_PROPERTIES_FILE = "SimpleAccept.properties";
	static final String TEST_DATA_FOLDER = "simpleaccept.test.data.folder";
	static final String TEST_DATA_FILES = "simpleaccept.test.data.files";
	static final String TEST_RESULTS_RET_COUNT = "simpleaccept.test.results.retention.count";
	static final String TEST_RESULT_FILE_TYPE = "simpleaccept.result.file.type";
	static final String CONCURRENT_PROCESSING_COUNT = "simpleaccept.concurrent.processing.count";
	static final String PROCESSING_LOG_LOCATION = "simpleaccept.processing.log.location";
	private static final String TEST_DATA_FILE_EXT = ".csv"; 
	
	private static final Properties properties = new Properties();
	
	/**
	 * Converts array to string with comma (,) separated tokens.
	 *  
	 * @param args
	 * @return generated string
	 */
	static String arrayToString(final String[] args) {

		if (args == null || args.length == 0)
			return null;
		
		final StringBuilder sb = new StringBuilder();
		for (String arg : args)
			sb.append(arg).append(",");
		return sb.substring(0, sb.length() - 1);
	}
	
	/**
	 * Returns files to process by looking up...
	 * 1. Given root folder
	 * 2. Given files to process, if any.
	 * 	If no particular files are specified, all files under root folder are considered for testing.
	 * 
	 * @param root
	 * @param filesToProcess
	 * @return
	 */
	static List<File> getFiles(final File root, final String filesToProcess) {
		SimpleAcceptUtility.getLogger().info("Retrieving test data files from : " + root);
		final List<String> filesToProcessList = new ArrayList<String>();
		if (filesToProcess != null && !filesToProcess.trim().isEmpty() && 
				!filesToProcess.trim().equals("*") && !filesToProcess.trim().equalsIgnoreCase("ALL")) {
			filesToProcessList.addAll(Arrays.asList(filesToProcess.split(",")));
		}
		final List<File> files = new ArrayList<File>();
		for (File file : root.listFiles()) {
			if (file.getName().endsWith(TEST_DATA_FILE_EXT)) {
				if ((filesToProcessList == null) || (filesToProcess.isEmpty())
						|| (!filesToProcessList.isEmpty() && filesToProcessList.contains(file.getName()))) {
					files.add(file);
				}
			}
		}
		return files;
	}
	
	/**
	 * Get fully qualified report file name.
	 * Format is <given-test-data-file-name>_Report.<report-file-format-as-specified-in-properties-file>
	 * Default is .txt
	 * 
	 * @param resultDir
	 * @param testDataFileName
	 * @return
	 */
	static String getReportFileName(final String resultDir, final String testDataFileName) {
		/* If no type is specified, default is .txt */
		final String resultFileNameSuffix = "_Report." + getProperty(TEST_RESULT_FILE_TYPE, false).toLowerCase();
		return resultDir + File.separator + testDataFileName.replace(TEST_DATA_FILE_EXT, resultFileNameSuffix);
	}
	
	/**
	 * Loads all the properties from given properties file. 
	 * 
	 * @throws IOException
	 */
	static void loadProperties() throws IOException {
		final String configFile = System.getProperty("SIMPLE_ACCEPT_CONFIG_FILE", DEFAULT_PROPERTIES_FILE);
		final InputStream stream = SimpleAcceptFramework.class.getClassLoader().getResourceAsStream(configFile);
		if (stream == null) {
			throw new RuntimeException("Invalid config file [" + configFile + "]");
		}
		properties.load(stream);
	}
	
	/**
	 * Returns value of given property.
	 * If property is not set, exception is thrown.
	 *  
	 * @param propertyName
	 * @return propertyValue
	 */
	static String getProperty(final String propertyName, final boolean isMandatory) {
		final String propertyValue = properties.getProperty(propertyName);
		if (isMandatory && (propertyValue == null || propertyValue.trim().isEmpty()))
			throw new RuntimeException(propertyName + " property is missing. Cannot proceed further.");
		return propertyValue;
		
	}
	
	/**
	 * Sets given value on a given field of given instance.
	 * Also considers special values like...
	 * 1. Date - {today} +/- <number-of-days>
	 * 
	 * @param clazz
	 * @param instance
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	static void setFieldValue(final Class<?> clazz, final SimpleAcceptBase instance, final String fieldName, final String value) throws Exception {
		final Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		if (field.getType() == int.class || Integer.class.isAssignableFrom(field.getType()))
			field.set(instance, Integer.valueOf(value));
		else if (field.getType() == double.class || Double.class.isAssignableFrom(field.getType()))
			field.set(instance, Double.valueOf(value));
		else if (field.getType() == float.class || Float.class.isAssignableFrom(field.getType()))
			field.set(instance, Float.valueOf(value));
		else if (field.getType() == long.class || Long.class.isAssignableFrom(field.getType()))
			field.set(instance, Long.valueOf(value));
		else if (field.getType() == boolean.class || Boolean.class.isAssignableFrom(field.getType()))
			field.set(instance, Boolean.valueOf(value));
		else if (field.getType() == Date.class || Date.class.isAssignableFrom(field.getType()))
			if (value.contains(SimpleAcceptProcessor.SPECIAL_PARAM_DATE))
				field.set(instance, getDate(value));
			else
				field.set(instance, instance.getDateFormatter().parse(value));
		else
			if (value.contains(SimpleAcceptProcessor.SPECIAL_PARAM_DATE))
				field.set(instance, instance.getDateFormatter().format(getDate(value)));
			else
				field.set(instance, value);
	}

	/**
	 * Creates and returns date considering given date value as input parameter.
	 * Expected format is {today} +/- <number-of-days>
	 * 
	 * @param value
	 * @return
	 */
	static Date getDate(final String value) {
		
		final Calendar cal = Calendar.getInstance();
		
		if (value.equals(SimpleAcceptProcessor.SPECIAL_PARAM_DATE))
			return cal.getTime();
		
		String tempVal = value.replace(SimpleAcceptProcessor.SPECIAL_PARAM_DATE, "");
		
		if (tempVal.contains("+")) {
			tempVal = tempVal.replace("+", "");
			cal.add(Calendar.DATE, Integer.valueOf(tempVal.trim()));
		} else if (tempVal.contains("-")) {
			tempVal = tempVal.replace("-", "");
			cal.add(Calendar.DATE, -Integer.valueOf(tempVal.trim()));
		} else {
			throw new RuntimeException("Unhandled date format. Value is : " + value);
		}
		
		return cal.getTime();
	}
	
	/**
	 * Gets the value for given field of given object instance.
	 * 
	 * @param clazz
	 * @param instance
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	static String getFieldValue(final Class<?> clazz, final SimpleAcceptBase instance, final String fieldName) throws Exception {
		final Field outputParam = clazz.getDeclaredField(fieldName);
		outputParam.setAccessible(true);
		return outputParam.get(instance).toString();
	}
	
	static String getExpectedValue(final int offset, final int index, final String[] args) {
		final int indexFromStart = offset + index;
		return args[indexFromStart];
	}
	
	/**
	 * Cleans up result history by considering history retention count
	 * given in properties file.
	 * 
	 * @param testResultRetCount
	 * @param root
	 */
	static void cleanupHistory(final String testResultRetCount, final File root) {
		
		final Comparator<File> resultDirComparator = new Comparator<File>() {
			@Override
			public int compare(final File file1, final File file2) {
				final long lastModified1 = file1.lastModified();
				final long lastModified2 = file2.lastModified();
				return ((lastModified1 == lastModified2) ? 0 : (lastModified1 < lastModified2) ? -1 : 1);
			}
			
		};
		
		if (testResultRetCount == null || testResultRetCount.trim().isEmpty())
			return;
		
		try {
			final int testResultsHistoryInt = Integer.valueOf(testResultRetCount);
			
			final File[] rootContent = root.listFiles();
			
			if (rootContent == null || rootContent.length == 0)
				return;
			
			final List<File> resultDirs = new ArrayList<File>();
			for (File file : rootContent) {
				if (file.isDirectory())
					resultDirs.add(file);
			}
			
			if (resultDirs.size() <= testResultsHistoryInt)
				return;
			
			Collections.sort(resultDirs, resultDirComparator);
			
			/* Currently folder structure supports for files only. If dir-under-dir is allowed, this needs to be changed. */
			for (int i=0; i<resultDirs.size()-testResultsHistoryInt; i++) {
				final File currDir = resultDirs.get(i);
				while (currDir.exists()) {
					if (currDir.list() != null && currDir.list().length > 0)
						currDir.listFiles()[0].delete();
					else
						currDir.delete();
				}
			}
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Deprecated
	static Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns file type by checking file extenssion.
	 * 
	 * @param file
	 * @return type
	 */
	static String getType(final File file) {
		return file.getName().substring(file.getName().lastIndexOf(".") + 1);
	}
	
	static Logger getLogger() {
		final Logger logger = Logger.getLogger("SimpleAcceptance");
		/*try {
			Handler fileHandler = new FileHandler(getProperty(PROCESSING_LOG_LOCATION, false));
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return logger;
	}
}
