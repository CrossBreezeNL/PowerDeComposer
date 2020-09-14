package com.xbreeze.xml.config;

public class OneOffConditionGroup extends ConditionGroup {

	public OneOffConditionGroup() {
		super();
	}

	@Override
	protected String getXPathConditionOperator() {
		return "or";
	}
}
