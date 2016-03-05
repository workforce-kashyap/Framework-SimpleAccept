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

import java.text.SimpleDateFormat;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Base class for a test case.
 * All test cases should extend this class.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public abstract class SimpleAcceptBase {

	/**
	 * Framework calls this method for testing.
	 *  
	 * 
	 * @return execution result
	 * @throws Exception
	 */
	public abstract boolean execute() throws Exception;
	
	/**
	 * Expected date format for input/output dates, if required. 
	 * 
	 * @return SimpleDateFormat
	 */
	public abstract SimpleDateFormat getDateFormatter();
}
