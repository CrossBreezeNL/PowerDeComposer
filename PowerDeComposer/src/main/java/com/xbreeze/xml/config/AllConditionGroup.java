package com.xbreeze.xml.config;

public class AllConditionGroup extends ConditionGroup {

	public AllConditionGroup() {
		super();
	}

	@Override
	protected String getXPathConditionOperator() {
		return "and";
	}
}
