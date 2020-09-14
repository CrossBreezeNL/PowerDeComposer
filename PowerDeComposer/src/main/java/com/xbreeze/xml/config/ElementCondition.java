package com.xbreeze.xml.config;

public class ElementCondition extends AbstractConfigElementWithXPathAttribute implements ElementConditionOrGroup {

	public ElementCondition() {
		super();
	}

	@Override
	public String getXPathExpression() {
		return this.getXPath();
	}
}
