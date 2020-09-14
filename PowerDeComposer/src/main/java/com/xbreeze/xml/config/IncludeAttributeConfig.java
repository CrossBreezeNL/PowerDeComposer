package com.xbreeze.xml.config;

import javax.xml.bind.annotation.XmlAttribute;

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
