package com.xbreeze.xml.decompose.config;

import com.xbreeze.xml.config.AbstractConfigElementWithXPathAttributeAndCondition;
import com.xbreeze.xml.config.DecomposeFileType;

import jakarta.xml.bind.annotation.XmlAttribute;

public class TargetFileNameConfig extends AbstractConfigElementWithXPathAttributeAndCondition {

	public TargetFileNameConfig() {
		super();
	}
	
	// The target file type to write. This can be any value of DecomposeFileType.
	// By default this is XML.
	@XmlAttribute(name = "targetFileType", required = false)
	private DecomposeFileType _targetFileType;
	
	public DecomposeFileType getDecomposeFileType() {
		return this._targetFileType;
	}
}
