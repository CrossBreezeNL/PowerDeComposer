package com.xbreeze.xml.config;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ElementCondition.class, ConditionGroup.class})
public interface ElementConditionOrGroup {

	public String getXPathExpression();
}
