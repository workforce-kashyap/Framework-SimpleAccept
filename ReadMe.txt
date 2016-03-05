********************************************************************************
* SimpleAccept
********************************************************************************

One more user acceptance testing tool at your service - SimpleAccept!

--------------------------------------------------------------------------------
IT TAKES DATA INPUT IN CSV FORMAT.
--------------------------------------------------------------------------------
The expected format should abide by certain rules.
Rules are simple
1. File Header
	First line should contain
		a. TestClass
		b. <fully.qualified.test.class.name> (without those angular brackets)
	This line can occur only once in the file.
2. Test Data Header
	This line should contain
		a. TestParams
		b. <proprties-to-set-same-as-TestClassName> (which are considered as input values)
		c. <proprties-to-get-same-as-TestClassName>? (which are considered as result/output values. Note: '?' at the end is must)
	This line can occur multiple times in a file, but only once for given set of data.
3. Actual Test Data
	This line should contain
		a. First element as empty
		b. Input and expected values. (All input values should come before result/output values)
	This line can occur multiple times in a file, and multiple times to form set of data.
	
Exmaple...
TestClass,foo.bar.SomeClassName
TestParams,blah1, blah2, blah3,result1?
,1,2,3,111
,1,2,4,222
,1,2,5,222
TestParams,blah1, blah2, blah3,result2?
,a,b,c,xxx
,a,b,d,yyy
,a,b,e,zzz

--------------------------------------------------------------------------------
FRAMEWORK VALUES ARE CONFIGURABLE
--------------------------------------------------------------------------------
Refer SimpleAccept.properties to learn configurable properties.
You can use same file / can create your own properties file.
Just make sure that file is included in classpath while executing testing.

--------------------------------------------------------------------------------
RESULT FORMAT
--------------------------------------------------------------------------------
Testing result can be saved in .html, .csv , .txt or .xml format. (.txt is default)
Format can be mentioned in SimpleAccept.properties file.
Framework also generates overall test result in ReadMe.txt file containing
	1. Number of total / executed / passed / failed tests
	2. Test data file names for which tests have failed
Results are stored with the name same as respective test data file, appended by "_Result".
	Example...
		Test data file : SampleTestData.csv
		Result file: SampleTestData_Result.csv / SampleTestData_Result.txt / SampleTestData_Result.html
The results are stored in folder in timestamped directory.
	Example...
		Test data files are saved in : TestFolder
		Result files would be stored in : TestFolder/20160227_14-33-07, TestFolder/20160227_15-45-00, TestFolder/20160228_10-00-39

--------------------------------------------------------------------------------
ADVANTAGES
--------------------------------------------------------------------------------
1. No server is required for this tool to run.
2. Framework starts with simple java class, can be integrated with tools like ANT.
3. Results are stored in timed manner. Results retention can be configured.
4. Easy to create data. Input file is as simple as CSV file. Data can be created in XLS file and then can be saved as CSV file.
5. Overall result can be seen in one go! Refer ReadMe.txt for each test run result folder.
6. Supports concurrent test execution - faster turn around time.

--------------------------------------------------------------------------------
EXAMPLES
--------------------------------------------------------------------------------
One set of example of test artifacts is provided with this distribution.
1. Test class : ./test/testing/framework/simple/accept/TestSampleTestFile.java
2. Test data : ./resources/test
3. Result data : ./Result-csv, ./Result-html, ./Result-txt, ./Result-xml

--------------------------------------------------------------------------------
Contact Information
--------------------------------------------------------------------------------
Developed by Kashyap Deshpande
Feel free to reach out to kashyaprdeshpande@gmail.com for your suggestions / feedback.
