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

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Executable (Runnable) class of framework, delegating actual processing to the Processor.
 * This is to support concurrent tests execution.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public class SimpleAcceptExecutable implements Runnable {

	private final File testDataFile;
	private final File resultDir;
	private final SimpleAcceptResultBean bean;
	
	public SimpleAcceptExecutable(final File testDataFile, final File resultDir,final SimpleAcceptResultBean bean) {
		this.testDataFile = testDataFile;
		this.resultDir = resultDir;
		this.bean = bean;
	}

	/**
	 * Creates Processor instance and delegates work to it.
	 * Checks the test result and updates...
	 * 1. passed/failed tests count
	 * 2. collection of failed test files, if test fails.
	 */
	@Override
	public void run() {
		final SimpleAcceptProcessor processor = new SimpleAcceptProcessor(testDataFile, resultDir);
		processor.process();
		if (processor.isTestPassed()) {
			bean.passedTests.incrementAndGet();
		} else {
			bean.failedTests.incrementAndGet();
			bean.failedTestFiles.add(testDataFile.getName());
		}
		bean.terminationLatch.countDown();
	}
}
