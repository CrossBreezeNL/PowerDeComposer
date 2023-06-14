package com.xbreeze.xml.decompose.config;

import jakarta.xml.bind.annotation.XmlAttribute;

import com.xbreeze.xml.config.AbstractConfigElementWithXPathAttribute;

public class IncludeAttributeConfig extends AbstractConfigElementWithXPathAttribute {
	private String _name;
	
	@XmlAttribute(name = "name")
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}
	
	public IncludeAttributeConfig() {
		super();
	}
}
