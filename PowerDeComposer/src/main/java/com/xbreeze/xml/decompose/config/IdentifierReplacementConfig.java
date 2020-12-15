package com.xbreeze.xml.decompose.config;

import javax.xml.bind.annotation.XmlAttribute;

public class IdentifierReplacementConfig {
	private String _identifierNodeXPath;
	private String _replacementValueXPath;
	private String _referencingNodeXPath;
	
	@XmlAttribute(name = "identifierNodeXPath")
	public String getIdentifierNodeXPath() {
		return this._identifierNodeXPath;
	}

	public void setIdentifierNodeXPath(String identifierNodeXPath) {
		this._identifierNodeXPath = identifierNodeXPath;
	}

	@XmlAttribute(name = "replacementValueXPath")
	public String getReplacementValueXPath() {
		return _replacementValueXPath;
	}

	public void setReplacementValueXPath(String _replacementValueXPath) {
		this._replacementValueXPath = _replacementValueXPath;
	}

	@XmlAttribute(name = "referencingNodeXPath")
	public String getReferencingNodeXPath() {
		return _referencingNodeXPath;
	}

	public void setReferencingNodeXPath(String referencingNodeXPath) {
		this._referencingNodeXPath = referencingNodeXPath;
	}
}
