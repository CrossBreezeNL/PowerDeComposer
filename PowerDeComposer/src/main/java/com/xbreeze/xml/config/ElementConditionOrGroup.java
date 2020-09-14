package com.xbreeze.xml.config;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public interface ElementConditionOrGroup {

	public String getXPathExpression();
}
