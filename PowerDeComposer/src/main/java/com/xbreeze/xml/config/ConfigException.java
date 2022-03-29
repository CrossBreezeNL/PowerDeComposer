/*******************************************************************************
 * Copyright (c) 2022 CrossBreeze
 *
 * This file is part of PowerDeComposer.
 *
 * PowerDeComposer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your option) any 
 * later version.
 *
 * PowerDeComposer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerDeComposer.  If not, see <https://www.gnu.org/licenses/>.
 *      
 * Contributors:
 *   Harmen Wessels - CrossBreeze
 *   Willem Otten - CrossBreeze
 *******************************************************************************/
package com.xbreeze.xml.config;

public class ConfigException extends Exception {
	private static final long serialVersionUID = -4716484260267428508L;
	
	/**
	 * Constructor.
	 * @param message The message of the exception.
	 */
	public ConfigException(String message) {
		super(message);
	}
	
	/**
	 * Constructor.
	 * @param throwable Throwable of the exception.
	 */
	public ConfigException(Throwable throwable) {
		super(throwable);
	}
	
	/**
	 * Constructor.
	 * @param message The message of the exception.
	 * @param throwable The throwable.
	 */
	public ConfigException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
