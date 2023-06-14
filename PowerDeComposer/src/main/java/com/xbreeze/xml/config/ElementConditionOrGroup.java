package com.xbreeze.xml.config;

import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ElementCondition.class, ConditionGroup.class})
public interface ElementConditionOrGroup {

	public String getXPathExpression();
}
