package com.xbreeze.xml.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class DecomposableElementConfig extends AllConditionGroup {
	private TargetFileNameConfig _targetFileNameConfig;
	private List<IncludeAttributeConfig> _includeAttributeConfigs;
	
	public DecomposableElementConfig() {
		super();
	}
	
	@XmlElement(name = "TargetFileName")
	public TargetFileNameConfig getTargetFileNameConfig() {
		return _targetFileNameConfig;
	}
	
	public void setTargetFileNameConfig(TargetFileNameConfig targetFileNameConfig) {
		this._targetFileNameConfig = targetFileNameConfig;
	}
	
	@XmlElement(name = "IncludeAttribute")
	@XmlElementWrapper(name = "IncludeAttributes")
	public List<IncludeAttributeConfig> getIncludeAttributeConfigs() {
		return _includeAttributeConfigs;
	}
	
	public void setIncludeAttributeConfigs(List<IncludeAttributeConfig> includeAttributeConfigs) {
		this._includeAttributeConfigs = includeAttributeConfigs;
	}
}
