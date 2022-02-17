/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.api.v1.log;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * abstract implementation for logger
 * @author russdanner
 */
public abstract class AbstractLogger implements Logger {

	/**
	 * expand a message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public String expandMessage(String msg, Object ... args) {
		String retMessage = "";

		ResourceBundle resources = null;
		String pattern = null;

		try {
			resources = ResourceBundle.getBundle("/org/craftercms/cstudio/studio-message-resources"); // locale
			pattern = resources.getString(msg);
		} catch(Exception e) {
			// no resource for msg ID (caller just sent string)
		}
		
		if(pattern == null) {
			pattern = msg;
		}
		
        
        try {
            MessageFormat msgFormat = new MessageFormat(pattern); //use locale from some external source
            retMessage = msgFormat.format(args);
        } catch(Exception e) {
            retMessage = pattern;
        }
		
		return retMessage;
	}
}
