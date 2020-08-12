package com.xbreeze.xml.config;

import java.util.logging.Logger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class UnmarshallValidationEventHandler implements ValidationEventHandler {
	// The logger for the XGenConfig class.
	private static final Logger logger = Logger.getLogger(UnmarshallValidationEventHandler.class.getName());
	
	@Override
	public boolean handleEvent(ValidationEvent event) {
		if (event.getLocator() != null)
			logger.warning(String.format("Error in config file on line %d, column %d, node '%s':\n%s", event.getLocator().getLineNumber(), event.getLocator().getOffset(), event.getLocator().getNode(), event.getMessage()));
		else
			logger.warning(String.format("Error in config file:\n%s", event.getMessage()));
		return false;
	}
}
