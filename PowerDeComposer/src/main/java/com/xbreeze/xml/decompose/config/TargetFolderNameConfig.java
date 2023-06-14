package com.xbreeze.xml.decompose.config;

import jakarta.xml.bind.annotation.XmlAttribute;

import com.xbreeze.xml.config.AbstractConfigElementWithXPathAttributeAndCondition;

public class TargetFolderNameConfig extends AbstractConfigElementWithXPathAttributeAndCondition {
	
	private Boolean _overrideParent = false;
	
	@XmlAttribute(name = "overrideParent")
	public Boolean getOverrideParent() {
		return _overrideParent;
	}

	public void setOverrideParent(Boolean overrideParent) {
		if (overrideParent != null)
			_overrideParent = overrideParent;
	}

	public TargetFolderNameConfig() {
		super();
	}
}
