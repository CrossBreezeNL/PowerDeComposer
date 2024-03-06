package com.xbreeze.xml.utils;

import java.nio.charset.Charset;
import java.util.logging.Logger;

public class FileContentAndCharset {
	private static final Logger logger = Logger.getGlobal();
	
	private String _fileContents;
	private Charset _fileCharset;
	private String _lineSeparator;
	
	public FileContentAndCharset(String fileContents, Charset fileCharset) {
		this._fileContents = fileContents;
		this._fileCharset = fileCharset;
	}

	public String getFileContents() {
		return this._fileContents;
	}

	public Charset getFileCharset() {
		return this._fileCharset;
	}
	
	/**
	 * Get the line separator of the file.
	 * @return The line separator for the file.
	 * @throws Exception If the line separator can't be found and exception is thrown.
	 */
	public String getLineSeparator() throws Exception {
		// If the line separator is not set yet, derive it from the file contents.
		if (this._lineSeparator == null) {
			// Find the first line-feed character.
			int firstNewLineIndex = this._fileContents.indexOf('\n');
			
			if (firstNewLineIndex == -1)
				throw new Exception("Cannot detect line separator. No line-feed character found in file!");
			
			// Check whether there is a carriage return before the line-feed character.
			if (this._fileContents.charAt(firstNewLineIndex - 1) == '\r') {
				this._lineSeparator = "\r\n";				
			} else {
				this._lineSeparator = "\n";
			}
			logger.fine(String.format("Found line separator: %s", this._lineSeparator.replace("\n", "[LF]").replace("\r", "[CR]")));
		}
		return this._lineSeparator;
	}
	
	public byte[] getBytes() {
		// Currently the encoding is set to US_ASCII, cause this solves the issue for special characters and doesn't seem to break anything.
		// Question is asked to the vtd-gen developer if this is a bug in vtg-gen.
		// https://stackoverflow.com/questions/51507388/vtd-xml-element-fragment-incorrect
		//vg.setDoc(xmlDocument.getBytes(StandardCharsets.US_ASCII));
		// Disabled the bytes retrieval using a specific charset, because this breaks the PowerDesigner special characters.
		return this._fileContents.getBytes(this._fileCharset);
	}
}
