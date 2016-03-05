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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Test file processor. 
 *  
 * @author kashyaprdeshpande@gmail.com
 */
public class SimpleAcceptProcessor {

	private static final String TEST_CLASS = "TestClass";
	private static final String TEST_PARAMS = "TestParams";
	private static final String CSV_DELIM = ",";
	static final String SPECIAL_PARAM_DATE = "{today}";
	static final String SPECIAL_PARAM_ANY = "?";
	
	private static final String INPUT_PARAM = "input";
	private static final String OUTPUT_PARAM = "output";
	
	private static final String TEST_RESULT_PASSED = "Passed.";
	private static final String TEST_RESULT_FAILED = "Failed.";
	
	private final File testDataFile;
	private final File testResultFile;
	private String individualTestResult;
	private String consolidatedTestResult = TEST_RESULT_PASSED;
	
	public SimpleAcceptProcessor(final File testDataFile, final File resultDir) {
		this.testDataFile = testDataFile;
		this.testResultFile = new File(SimpleAcceptUtility.getReportFileName(resultDir.getAbsolutePath(), testDataFile.getName()));
	}
	
	/**
	 * Delegated file processing and maintains consolidated test result status.
	 */
	public void process() {
		try {
			processFile();
		} catch (final Exception e) {
			consolidatedTestResult = TEST_RESULT_FAILED;
			e.printStackTrace();
		}
	}
	
	/**
	 * Processes file and delegates test execution.
	 * 
	 * @throws Exception
	 */
	public void processFile() throws Exception {
		
		BufferedReader dataReader = null;
		SimpleAcceptWriter resultWriter = null;
		String line = null;
		
		try {
			dataReader = new BufferedReader(new FileReader(testDataFile));
			resultWriter = SimpleAcceptWriter.getInstace(testResultFile);
			
			line = dataReader.readLine();
			final SimpleAcceptBase testClassInstance = getTestClassInstance(line);
			resultWriter.write(line, getLineType(line));
			
			final Map<String, List<String>> ioParams = new HashMap<String, List<String>>();
			line = dataReader.readLine();
			while (line != null) {
				final String[] ioValues = line.split(CSV_DELIM);
				if (!canSkip(line, ioValues)) {
					if (ioValues[0].equals(TEST_PARAMS)) {
						prepareInputOutputParams(ioValues, ioParams);
						line = SimpleAcceptUtility.arrayToString(ioValues) + CSV_DELIM + "Result";
					} else {
						executeTesting(testClassInstance, ioParams, ioValues);
						line = CSV_DELIM + SimpleAcceptUtility.arrayToString(ioValues) + CSV_DELIM + individualTestResult;
					}
				}
				
				resultWriter.write(line, getLineType(line));
				line = dataReader.readLine();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			consolidatedTestResult = TEST_RESULT_FAILED;
			logErrorMsg(resultWriter, line, e.getMessage());
		} finally {
			dataReader.close();
			resultWriter.close();
		}
	}
	
	/**
	 * Returns type of given line.
	 * Type could be...
	 * 1. TestClass / TestClassParams
	 * 2. Test result status
	 * 
	 * @param line
	 * @return line type
	 */
	private String getLineType(final String line) {
		if (line == null || line.trim().isEmpty())
			return null;
		if (line.contains(TEST_CLASS))
			return SimpleAcceptWriter.LINE_TYPE_FILE_HEADER;
		else if (line.contains(TEST_PARAMS))
			return SimpleAcceptWriter.LINE_TYPE_DATA_HEADER;
		else if (individualTestResult.contains(TEST_RESULT_FAILED))
			return SimpleAcceptWriter.LINE_TYPE_FAIILED;
		else
			return null;
	}
	
	/**
	 * Logs error message.
	 * 
	 * @param writer
	 * @param source
	 * @param errMsg
	 * @throws Exception
	 */
	private void logErrorMsg(final SimpleAcceptWriter writer, final String source, final String errMsg) throws Exception {
		writer.write(source, SimpleAcceptWriter.LINE_TYPE_ERROR, errMsg);
		//writer.write(errMsg, SimpleAcceptWriter.LINE_TYPE_ERROR);
	}
	
	/**
	 * Executes testing by...
	 * 1. Retrieving input values
	 * 2. Calling execute() method of given test class
	 * 3. Fetching output values
	 * 4. Validating actual values against expected ones
	 * 
	 * @param instance
	 * @param ioParams
	 * @param ioValues
	 * @throws Exception
	 */
	private void executeTesting(final SimpleAcceptBase instance, final Map<String, List<String>> ioParams, final String[] ioValues) throws Exception {
		
		validate(ioParams, ioValues);
		
		/* Create instance of <? extends JitNesseBase from given test data file. */
		Class<? extends SimpleAcceptBase> clazz = instance.getClass();
		
		/* Set values of input params on created instance. */
		final List<String> inputParams = ioParams.get(INPUT_PARAM);
		try {
			for (int inputParamIndex=0; inputParamIndex<inputParams.size(); inputParamIndex++) {
				SimpleAcceptUtility.setFieldValue(clazz, instance, inputParams.get(inputParamIndex), ioValues[inputParamIndex]);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			individualTestResult = TEST_RESULT_FAILED;
			consolidatedTestResult = TEST_RESULT_FAILED;
			throw new Exception("Exception occurred while setting values of : " + inputParams + " of class " + instance.getClass(), e);
		}

		/* Execute testing on created instance. */
		try {
			instance.execute();
		} catch (final Exception e) {
			e.printStackTrace();
			individualTestResult = TEST_RESULT_FAILED;
			consolidatedTestResult = TEST_RESULT_FAILED;
			throw new Exception("Exception occurred while executing test : " + instance.getClass(), e);
		}
		
		/* Retrieve results (output params) and validate them against expected values. */
		final List<String> outputParams = ioParams.get(OUTPUT_PARAM);
		try {
			for (int outputParamIndex=0; outputParamIndex<outputParams.size(); outputParamIndex++) {
				final String outputValue = SimpleAcceptUtility.getFieldValue(clazz, instance, outputParams.get(outputParamIndex));
				final int ioValuesIndex = inputParams.size() + outputParamIndex;
				final String expected = ioValues[ioValuesIndex];
				if (expected == null || expected.trim().isEmpty() || expected.equals(SPECIAL_PARAM_ANY)) {
					ioValues[ioValuesIndex] = outputValue;
				} else if (!ioValues[ioValuesIndex].equals(outputValue)) {
					individualTestResult = TEST_RESULT_FAILED + " Expected [" + ioValues[ioValuesIndex] + "]. Actual [" + outputValue + "].";
					consolidatedTestResult = TEST_RESULT_FAILED;
				} else {
					// Test is passed!
					individualTestResult = TEST_RESULT_PASSED;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			individualTestResult = TEST_RESULT_FAILED;
			consolidatedTestResult = TEST_RESULT_FAILED;
			throw new Exception("Exception occurred while retrieving output and determine the test result - " + instance.getClass(), e);
		}
	}
	
	/**
	 * Reads test data file and prepares input / output parameter.
	 * All input params should be before all output params.
	 * 
	 * @param tokens
	 * @param params
	 */
	private void prepareInputOutputParams(final String[] tokens, final Map<String, List<String>> params) {
		params.clear();
		params.put(INPUT_PARAM, new ArrayList<String>());
		params.put(OUTPUT_PARAM, new ArrayList<String>());
		for (String token : tokens) {
			if (token.equals(TEST_PARAMS))
				continue;
			if (token.charAt(token.length() - 1) == '?')
				params.get(OUTPUT_PARAM).add(token.substring(0, token.length() - 1));
			else
				params.get(INPUT_PARAM).add(token);
		}
	}
	
	/**
	 * Determines if given line from test data file can be ignore / skipped from processing.
	 * 
	 * @param line
	 * @param tokens
	 * @return
	 */
	private boolean canSkip(final String line, final String[] tokens) {
		return (line.isEmpty() || tokens == null || tokens.length == 0);
	}
	
	/**
	 * Performs validation.
	 * 1. Checks for number-of-values against number-of-properties.
	 * 
	 * @param ioParams
	 * @param ioValues
	 */
	private void validate(final Map<String, List<String>> ioParams, final String[] ioValues) {
		int ioParamSize = 0;
		for (List<String> paramList : ioParams.values())
			ioParamSize += paramList.size();
		
		if (ioParams == null || ioValues == null || ioParamSize != ioValues.length)
			throw new RuntimeException("Number-of-values " + ioValues.length + " not matching with number-of-properties " + ioParamSize);
	}
	
	/**
	 * Creates and returns instance of test class given in test data file again TestClass
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private SimpleAcceptBase getTestClassInstance(final String line) throws Exception {
		
		final String[] tokens = line.split(CSV_DELIM);
		if (tokens == null || tokens.length != 2)
			throw new Exception("Invalid beginning of test data file. Expected format : " + TEST_CLASS + ",Fully-Qualified-Test-Class-Name");
		if (tokens[0] == null || tokens[0].trim().isEmpty() || !tokens[0].equals(TEST_CLASS))
			throw new Exception("No " + TEST_CLASS + " defined. Expected format : " + TEST_CLASS + ",Fully-Qualified-Test-Class-Name");
		
		final String className = tokens[1];
		
		Class< ? > clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			throw new Exception("ClassNotFoundException occurred while creating Class object for : " + className);
		}
		
		SimpleAcceptBase instance;
		try {
			instance = (SimpleAcceptBase) clazz.newInstance();
		} catch (final InstantiationException e) {
			throw new Exception("InstantiationException occurred while creating instance of test class : " + clazz, e);
		} catch (final IllegalAccessException e) {
			throw new Exception("IllegalAccessException occurred while creating instance of test class : " + clazz, e);
		}
		
		return instance;
	}
	
	/**
	 * Checks final status of test case to determine if test is passed.
	 * 
	 * @return test passing result
	 */
	public boolean isTestPassed() {
		return TEST_RESULT_PASSED.equals(consolidatedTestResult);
	}
}
