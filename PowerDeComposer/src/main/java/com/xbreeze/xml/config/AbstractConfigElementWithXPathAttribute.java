package com.xbreeze.xml.config;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class AbstractConfigElementWithXPathAttribute {
	
	private String _xpath;
	
	@XmlAttribute(name = "xpath")
	public String getXPath() {
		return _xpath;
	}

	public void setXPath(String xPath) {
		_xpath = xPath;
	}
}
