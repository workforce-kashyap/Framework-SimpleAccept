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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Wrapper bean holding different params that constitute the result. 
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public class SimpleAcceptResultBean {
	final List<String> failedTestFiles = new ArrayList<String>();
	final AtomicInteger passedTests = new AtomicInteger(0);
	final AtomicInteger failedTests = new AtomicInteger(0);
	final CountDownLatch terminationLatch;
	public SimpleAcceptResultBean(final int terminationLatchCount) {
		terminationLatch = new CountDownLatch(terminationLatchCount);
	}
}
