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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Main class of the framework.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public class SimpleAcceptFramework {

	private List<File> testDataFiles = null;
	
	/**
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		SimpleAcceptUtility.getLogger().info("SimpleAccept Testing Started...");
		final SimpleAcceptFramework framework = new SimpleAcceptFramework();
		framework.setUp();
		framework.execute();
		SimpleAcceptUtility.getLogger().info("SimpleAccept Testing Finished.");
	}

	/**
	 * Creates base / set up for framework.
	 * 
	 * @throws Exception
	 */
	private void setUp() throws Exception {
		SimpleAcceptUtility.loadProperties();
		final File dir = new File(SimpleAcceptUtility.getProperty(SimpleAcceptUtility.TEST_DATA_FOLDER, true));
		/* If no files specified, all files under given TEST_DATA_FODLER are considered. */
		final String filesToProcess = SimpleAcceptUtility.getProperty(SimpleAcceptUtility.TEST_DATA_FILES, false);
		testDataFiles = SimpleAcceptUtility.getFiles(dir, filesToProcess);
		/* If no retention count specified, no clean-up is done. */
		SimpleAcceptUtility.cleanupHistory(SimpleAcceptUtility.getProperty(SimpleAcceptUtility.TEST_RESULTS_RET_COUNT, false), dir);
	}
	
	/**
	 * Executes actual testing by delegating work to appropriate worker.
	 * 
	 * Also, generates overall testing report containing...
	 * 1. Number of total / executed / passed / failed tests
	 * 2. Test data file names for which tests have failed
	 */
	private void execute() {
		
		boolean areAllTestsPassed = true;
		
		if (testDataFiles == null || testDataFiles.isEmpty())
			return;
		
		final File resultDir = new File(testDataFiles.get(0).getParentFile() + File.separator + new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date()));
		resultDir.mkdir();
		
		SimpleAcceptUtility.getLogger().info("Report would be available at : " + resultDir.getAbsolutePath());
		
		/* Added try-catch in for loop, to allow other files to process if any one is failed. */
		final SimpleAcceptResultBean resultBean = new SimpleAcceptResultBean(testDataFiles.size());
		final ExecutorService executor = Executors.newFixedThreadPool(Integer.valueOf(SimpleAcceptUtility.getProperty(SimpleAcceptUtility.CONCURRENT_PROCESSING_COUNT, true)));
		int totalTests = 0;
		
		for (File testDataFile : testDataFiles) {
			totalTests++;
			try {
				SimpleAcceptUtility.getLogger().info("Executing : " + testDataFile);
				final SimpleAcceptExecutable cmd = new SimpleAcceptExecutable(testDataFile, resultDir, resultBean);
				executor.execute(cmd);
				areAllTestsPassed = areAllTestsPassed ? (resultBean.failedTests.intValue() == 0) : false;
			} catch (final Exception e) {
				e.printStackTrace();
				areAllTestsPassed = false;
				resultBean.failedTests.incrementAndGet();
			}
		}
		
		/* Executor will wait till all the submitted jobs are finished. */
		try {
			resultBean.terminationLatch.await(5, TimeUnit.MINUTES);
			executor.shutdown();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		
		/* Overall testing report. */
		SimpleAcceptWriter testingReport = null;
		try {
			final File overallResult = new File(SimpleAcceptUtility.getReportFileName(resultDir.getAbsolutePath(), "ReadMe.txt"));
			testingReport = SimpleAcceptWriter.getInstace(overallResult);
			testingReport.write("Total number of tests available : " + testDataFiles.size());
			testingReport.write("Total number of tests executed : " + totalTests);
			testingReport.write("Total number of tests passed : " + resultBean.passedTests.intValue());
			testingReport.write("Total number of tests failed : " + resultBean.failedTests.intValue());
			testingReport.write(null);
			testingReport.write("Failed tests are...");
			testingReport.write(null);
			for (String failedTestFile : resultBean.failedTestFiles) {
				testingReport.write(failedTestFile);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				testingReport.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		/* Marking overall status as failed if any one of the test case if failed. */
		if (!areAllTestsPassed)
			throw new Error("Testing failed.");
	}
}
