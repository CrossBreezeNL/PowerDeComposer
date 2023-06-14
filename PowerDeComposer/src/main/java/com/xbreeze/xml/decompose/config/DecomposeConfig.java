package com.xbreeze.xml.decompose.config;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"changeDetectionConfig", "nodeRemovalConfigs", "identifierReplacementConfigs", "decomposableElementConfig"})
public class DecomposeConfig {
	// The default value for the file removal strategy is includes (since this is the safest option, choose files for speed).
	private String _fileRemovalStrategy = "includes";
	private ChangeDetectionConfig _changeDetectionConfig;
	private List<IdentifierReplacementConfig> _identifierReplacementConfigs;
	private List<NodeRemovalConfig> _nodeRemovalConfigs;
	private DecomposableElementConfig _decomposableElementConfig;
	
	public DecomposeConfig() {
		super();
	}
	
	@XmlAttribute(name = "fileRemovalStrategy")
	public String getFileRemovalStrategy() {
		return _fileRemovalStrategy;
	}

	public void setFileRemovalStrategy(String fileRemovalStrategy) {
		this._fileRemovalStrategy = fileRemovalStrategy;
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
