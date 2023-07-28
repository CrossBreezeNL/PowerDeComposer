package com.xbreeze.xml.config;

import java.util.logging.Logger;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

public class UnmarshallValidationEventHandler implements ValidationEventHandler {
	// The logger for the XGenConfig class.
	private static final Logger logger = Logger.getLogger(UnmarshallValidationEventHandler.class.getName());
	
	@Override
	public boolean handleEvent(ValidationEvent event) {
		if (event.getLocator() != null)
			logger.warning(String.format("Error in config file on line %d, column %d, node '%s':%s%s", event.getLocator().getLineNumber(), event.getLocator().getOffset(), event.getLocator().getNode(), System.lineSeparator(), event.getMessage()));
		else
			logger.warning(String.format("Error in config file:%s%s", System.lineSeparator(), event.getMessage()));
		return false;
	}
}
