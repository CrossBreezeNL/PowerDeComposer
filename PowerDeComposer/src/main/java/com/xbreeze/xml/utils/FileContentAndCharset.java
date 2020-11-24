package com.xbreeze.xml.utils;

import java.nio.charset.Charset;

public class FileContentAndCharset {
	private String _fileContents;
	private Charset _fileCharset;
	
	public FileContentAndCharset(String fileContents, Charset fileCharset) {
		this._fileContents = fileContents;
		this._fileCharset = fileCharset;
	}

	public String getFileContents() {
		return _fileContents;
	}

	public Charset getFileCharset() {
		return _fileCharset;
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
