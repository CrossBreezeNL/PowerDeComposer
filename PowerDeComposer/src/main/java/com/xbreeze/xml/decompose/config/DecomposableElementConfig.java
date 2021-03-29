package com.xbreeze.xml.decompose.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.xbreeze.xml.config.AllConditionGroup;

@XmlType(propOrder = {"targetFolderNameConfigs", "targetFileNameConfigs", "includeAttributeConfigs"})
public class DecomposableElementConfig extends AllConditionGroup {
	private List<TargetFolderNameConfig> _targetFolderNameConfigs;
	private List<TargetFileNameConfig> _targetFileNameConfigs;
	private List<IncludeAttributeConfig> _includeAttributeConfigs;
	
	public DecomposableElementConfig() {
		super();
	}
	
	@XmlElement(name = "TargetFolderName")
	@XmlElementWrapper(name = "TargetFolderNames")
	public List<TargetFolderNameConfig> getTargetFolderNameConfigs() {
		return _targetFolderNameConfigs;
	}
	
	public void setTargetFolderNameConfigs(List<TargetFolderNameConfig> targetFolderNameConfigs) {
		this._targetFolderNameConfigs = targetFolderNameConfigs;
	}
	
	@XmlElement(name = "TargetFileName", required = true)
	@XmlElementWrapper(name = "TargetFileNames")
	public List<TargetFileNameConfig> getTargetFileNameConfigs() {
		return _targetFileNameConfigs;
	}
	
	public void setTargetFileNameConfigs(List<TargetFileNameConfig> targetFileNameConfigs) {
		this._targetFileNameConfigs = targetFileNameConfigs;
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
