package com.xbreeze.xml.decompose.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"changeDetectionConfig", "identifierReplacementConfig", "nodeRemovalConfigs", "decomposableElementConfig"})
public class DecomposeConfig {
	private ChangeDetectionConfig _changeDetectionConfig;
	private IdentifierReplacementConfig _identifierReplacementConfig;
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
	
	@XmlElement(name = "IdentifierReplacement")
	public IdentifierReplacementConfig getIdentifierReplacementConfig() {
		return _identifierReplacementConfig;
	}
	
	public void setIdentifierReplacementConfig(IdentifierReplacementConfig identifierReplacementConfig) {
		this._identifierReplacementConfig = identifierReplacementConfig;
	}

	@XmlElement(name = "NodeRemoval")
	@XmlElementWrapper(name = "NodeRemovals")
	public List<NodeRemovalConfig> getNodeRemovalConfigs() {
		return _nodeRemovalConfigs;
	}

	public void setNodeRemovalConfigs(List<NodeRemovalConfig> nodeRemovalConfigs) {
		this._nodeRemovalConfigs = nodeRemovalConfigs;
	}
	
	@XmlElement(name = "DecomposableElement")
	public DecomposableElementConfig getDecomposableElementConfig() {
		return _decomposableElementConfig;
	}

	public void setDecomposableElementConfig(DecomposableElementConfig decomposableElementConfig) {
		this._decomposableElementConfig = decomposableElementConfig;
	}
}
