package com.xbreeze.xml.decompose.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"changeDetectionConfig", "nodeRemovalConfigs", "identifierReplacementConfigs", "decomposableElementConfig"})
public class DecomposeConfig {
	private ChangeDetectionConfig _changeDetectionConfig;
	private List<IdentifierReplacementConfig> _identifierReplacementConfigs;
	private List<NodeRemovalConfig> _nodeRemovalConfigs;
	private DecomposableElementConfig _decomposableElementConfig;
	
	public DecomposeConfig() {
		super();
	}
	
	@XmlElement(name = "ChangeDetection")
	public ChangeDetectionConfig getChangeDetectionConfig() {
		return _changeDetectionConfig;
	}
	
	public void setChangeDetectionConfig(ChangeDetectionConfig changeDetectionConfig) {
		this._changeDetectionConfig = changeDetectionConfig;
	}
	
	@XmlElement(name = "NodeRemoval")
	@XmlElementWrapper(name = "NodeRemovals")
	public List<NodeRemovalConfig> getNodeRemovalConfigs() {
		return _nodeRemovalConfigs;
	}

	public void setNodeRemovalConfigs(List<NodeRemovalConfig> nodeRemovalConfigs) {
		this._nodeRemovalConfigs = nodeRemovalConfigs;
	}
	
	
	@XmlElement(name = "IdentifierReplacement")
	@XmlElementWrapper(name = "IdentifierReplacements")
	public List<IdentifierReplacementConfig> getIdentifierReplacementConfigs() {
		return _identifierReplacementConfigs;
	}
	
	public void setIdentifierReplacementConfigs(List<IdentifierReplacementConfig> identifierReplacementConfigs) {
		this._identifierReplacementConfigs = identifierReplacementConfigs;
	}

	@XmlElement(name = "DecomposableElement")
	public DecomposableElementConfig getDecomposableElementConfig() {
		return _decomposableElementConfig;
	}

	public void setDecomposableElementConfig(DecomposableElementConfig decomposableElementConfig) {
		this._decomposableElementConfig = decomposableElementConfig;
	}
}
