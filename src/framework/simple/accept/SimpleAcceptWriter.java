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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016, Kashyap Deshpande.
 * 
 * Base writer class for generating test results.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
public abstract class SimpleAcceptWriter {

	public static final String FORMAT_TXT = "TXT";
	public static final String FORMAT_CSV = "CSV";
	public static final String FORMAT_HTML = "HTML";
	public static final String FORMAT_XML = "XML";

	public static final String LINE_TYPE_FILE_HEADER = "FILEHEADER";
	public static final String LINE_TYPE_DATA_HEADER = "DATAHEADTER";
	public static final String LINE_TYPE_ERROR = "ERROR";
	public static final String LINE_TYPE_FAIILED = "FAILED";

	protected final BufferedWriter writer;

	public SimpleAcceptWriter(final File testResultFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(testResultFile));
	}

	/**
	 * Returns appropriate writer, considering file type.
	 * 
	 * @param file
	 * @return writer
	 * @throws IOException
	 */
	public static SimpleAcceptWriter getInstace(final File file) throws IOException {
		final String format = SimpleAcceptUtility.getType(file);
		if (FORMAT_HTML.equalsIgnoreCase(format))
			return new HtmlWriter(file);
		else if (FORMAT_CSV.equalsIgnoreCase(format))
			return new CsvWriter(file);
		else if (FORMAT_XML.equalsIgnoreCase(format))
			return new XmlWriter(file);
		else
			return new TextfileWriter(file);
	}

	/**
	 * Flushes data and closes writer.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		writer.flush();
		writer.close();
	}

	/**
	 * To writer simple line (string) to a file.
	 * 
	 * @param line
	 * @throws IOException
	 */
	protected abstract void write(final String line) throws IOException;

	/**
	 * To write a line (string) to a file, considering line type.
	 * 
	 * @param line
	 * @param type
	 * @throws IOException
	 */
	protected abstract void write(final String line, final String type) throws IOException;
	
	/**
	 * To write a line (string) to a file, considering line type, especially if it is an error message.
	 * 
	 * @param line
	 * @param type
	 * @throws IOException
	 */
	protected abstract void write(final String line, final String type, final String comment) throws IOException;
}

/**
 * Writer implementation for CSV file format.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
class CsvWriter extends SimpleAcceptWriter {

	public CsvWriter(final File testResultFile) throws IOException {
		super(testResultFile);
	}

	@Override
	protected void write(final String line) throws IOException {
		write(line, null);
	}

	@Override
	protected void write(final String line, final String type) throws IOException {
		write(line, type, null);
	}

	@Override
	protected void write(final String line, final String type, final String comment) throws IOException {
		if (LINE_TYPE_ERROR.equals(type)) {
			writer.write("================================================================================");
			writer.newLine();
		}
		writer.write(line != null ? line : "");
		if (comment != null && !comment.trim().isEmpty()) {
			writer.newLine();
			writer.write(comment != null ? comment : "");
		}
		writer.newLine();
		if (LINE_TYPE_ERROR.equals(type)) {
			writer.write("================================================================================");
			writer.newLine();
		}
	}
}

/**
 * Writer implementation for HTML file format.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
class HtmlWriter extends SimpleAcceptWriter {

	private int colCount = 0;
	private boolean isTableOpened = false;

	public HtmlWriter(final File testResultFile) throws IOException {
		super(testResultFile);
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<style>");
		sb.append("table {border-collapse: collapse;}");
		sb.append("table, th, td {border: 1px solid black;}");
		sb.append("tr {color: green; font-family: Arial; font-size: 11px;}");
		sb.append("tr." + LINE_TYPE_FILE_HEADER + " {color: black; font-weight: bold;}");
		sb.append("tr." + LINE_TYPE_DATA_HEADER + " {color: black; font-weight: bold;}");
		sb.append("tr." + LINE_TYPE_ERROR + " {color: red;}");
		sb.append("tr." + LINE_TYPE_FAIILED + " {color: red;}");
		sb.append("</style>");
		sb.append("</head>");
		sb.append("<body>");
		writer.write(sb.toString());
	}

	@Override
	protected void write(final String line) throws IOException {
		write(line, null);
	}

	@Override
	protected void write(final String line, final String type) throws IOException {
		write(line, type, null);
	}
	
	@Override
	protected void write(final String line, final String type, final String comment) throws IOException {
		
		if (line == null || line.trim().isEmpty())
			return;

		final StringBuilder sb = new StringBuilder();

		if (LINE_TYPE_FILE_HEADER.equals(type)) {
			sb.append("<table>");
			sb.append("<tr class='" + getTrClass(type) + "'>");
			for (String token : line.split(","))
				sb.append("<td>" + token + "</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br>");
		} else if (LINE_TYPE_DATA_HEADER.equals(type)) {
			if (isTableOpened)
				sb.append("</table>").append("<br>");
			isTableOpened = true;
			colCount = line.split(",").length;
			sb.append("<table>");
			sb.append("<tr class='" + getTrClass(type) + "'>");
			for (String token : line.split(","))
				sb.append("<td>" + token + "</td>");
			sb.append("</tr>");
		} else {
			if (!isTableOpened)
				sb.append("<table>");
			sb.append("<tr class='" + getTrClass(type) + "'>");
			if (LINE_TYPE_ERROR.equals(type))
				sb.append("<td colspan='" + colCount + "'>" + line + "<br>" + comment + "</td>");
			else
				for (String token : line.split(","))
					sb.append("<td>" + token + "</td>");
			sb.append("</tr>");
		}

		writer.write(sb.toString());
	}

	@Override
	public void close() throws IOException {
		writer.write("</table></body></html>");
		super.close();
	}

	/**
	 * Retrieves CSS class name for HTML row : <TR>.
	 * 
	 * @param type
	 * @return css class
	 */
	private String getTrClass(final String type) {
		return type;
	}
}

/**
 * Writer implementation for simple TXT file format.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
class TextfileWriter extends SimpleAcceptWriter {

	public TextfileWriter(final File testResultFile) throws IOException {
		super(testResultFile);
	}

	@Override
	protected void write(final String line) throws IOException {
		write(line, null);
	}

	@Override
	protected void write(final String line, final String type) throws IOException {
		write(line, type, null);
	}

	@Override
	protected void write(final String line, final String type, final String comment) throws IOException {
		if (LINE_TYPE_ERROR.equals(type)) {
			writer.write("================================================================================");
			writer.newLine();
		}
		writer.write(line != null ? line : "");
		if (comment != null && !comment.trim().isEmpty()) {
			writer.newLine();
			writer.write(comment != null ? comment : "");
		}
		writer.newLine();
		if (LINE_TYPE_ERROR.equals(type)) {
			writer.write("================================================================================");
			writer.newLine();
		}
	}
}

/**
 * Writes line in XML format.
 * 
 * @author kashyaprdeshpande@gmail.com
 *
 */
class XmlWriter extends SimpleAcceptWriter {

	private static final String ROOT_TAG = "Report";
	private static final String ERROR_TAG = "Error";
	private static final String TEST_RESULT_GROUP = "TestResultGroup";
	private static final String TEST_RESULT_TAG = "TestResult";

	private final List<String> tags;
	private boolean isGroupOpened = false;

	public XmlWriter(final File testResultFile) throws IOException {
		super(testResultFile);
		this.tags = new ArrayList<String>();
		final StringBuilder sb = new StringBuilder();
		sb.append(startTag(ROOT_TAG));
		writer.write(sb.toString());
	}

	@Override
	protected void write(final String line) throws IOException {
		write(line, null);
	}

	@Override
	protected void write(final String line, final String type) throws IOException {
		write(line, type, null);
	}
	
	@Override
	protected void write(final String line, final String type, final String comment) throws IOException {

		final StringBuilder sb = new StringBuilder();

		if (LINE_TYPE_FILE_HEADER.equals(type))
			sb.append(populateHeader(line));
		else if (LINE_TYPE_DATA_HEADER.equals(type))
			sb.append(populateTagNamesAndGetResultGroup(line));
		else if (LINE_TYPE_ERROR.equals(type))
			sb.append(populateErrorTags(line, comment));
		else
			sb.append(populateTagValues(line));

		writer.write(sb.toString());
	}

	private String populateHeader(final String line) {
		final StringBuilder sb = new StringBuilder();
		final String[] tokens = line.split(",");
		sb.append(startTag(tokens[0]));
		sb.append(tokens[1]);
		sb.append(endTag(tokens[0]));
		return sb.toString();
	}

	private String populateTagNamesAndGetResultGroup(final String line) throws IOException {
		
		final StringBuilder sb = new StringBuilder();
		
		if (isGroupOpened)
			sb.append(endTag(TEST_RESULT_GROUP));
		
		isGroupOpened = true;
		tags.clear();
		
		sb.append(startTag(TEST_RESULT_GROUP));
		for (String token : line.split(","))
			tags.add(token);
		
		return sb.toString();
	}

	private String populateErrorTags(final String line, final String comment) {
		final StringBuilder sb = new StringBuilder();
		if (!isGroupOpened)
			sb.append(startTag(TEST_RESULT_GROUP));
		sb.append(startTag(TEST_RESULT_TAG));
		sb.append(startTag(ERROR_TAG) + "[" + line + "] - [" + comment + "]" + endTag(ERROR_TAG));
		sb.append(endTag(TEST_RESULT_TAG));
		return sb.toString();
	}

	private String populateTagValues(final String line) {

		if (line == null || line.trim().isEmpty())
			return line;

		int index = 1;
		final String[] values = line.split(",");
		final StringBuilder sb = new StringBuilder();
		sb.append(startTag(TEST_RESULT_TAG));
		while (index < tags.size()) {
			final String tagValue = values[index];
			String tagName = tags.get(index);
			tagName = tagName.endsWith("?") ? tagName.replace("?", "").trim() : tagName;
			final String formattedTagName = tags.get(index).endsWith("?") ? tagName.concat(" expected") : tagName;
			sb.append(startTag(formattedTagName) + tagValue + endTag(tagName));
			index++;
		}
		sb.append(endTag(TEST_RESULT_TAG));
		return sb.toString();
	}

	private String startTag(final String tagName) {
		return "<" + tagName + ">";
	}

	private String endTag(final String tagName) {
		return "</" + tagName + ">";
	}

	/**
	 * Closes XML content and calls close() method of parent class.
	 * 
	 * @see framework.simple.accept.SimpleAcceptWriter#close()
	 */
	@Override
	public void close() throws IOException {
		writer.write(endTag(TEST_RESULT_GROUP));
		writer.write(endTag(ROOT_TAG));
		super.close();
	}
}
