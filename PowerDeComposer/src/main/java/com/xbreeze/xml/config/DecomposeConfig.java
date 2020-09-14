package com.xbreeze.xml.config;

import javax.xml.bind.annotation.XmlElement;

public class DecomposeConfig {
	private DecomposableElementConfig _decomposableElementConfig;

	@XmlElement(name = "DecomposableElement")
	public DecomposableElementConfig getDecomposableElementConfig() {
		return _decomposableElementConfig;
	}

	public void setDecomposableElementConfig(DecomposableElementConfig decomposableElementConfig) {
		this._decomposableElementConfig = decomposableElementConfig;
	}
	
	public DecomposeConfig() {
		super();
	}
}
