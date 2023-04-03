package com.xbreeze.xml.config;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
public class AbstractConfigElementWithXPathAttributeAndCondition extends AbstractConfigElementWithXPathAttribute {

	private String _condition;
	
	@XmlAttribute(name = "condition")
	public String getCondition() {
		return _condition;
	}

	public void setCondition(String condition) {
		_condition = condition;
	}
}
