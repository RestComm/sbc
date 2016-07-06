/*******************************************************************************
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc, Eolos IT Corp and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 *******************************************************************************/
package org.restcomm.sbc.notification;
import javax.servlet.sip.SipServletMessage;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    14/5/2016 16:52:21
 * @class   SuspectActivityElectable.java
 *
 */
public interface SuspectActivityElectable {
	
	/**
	 * Gets Record Unique ID
	 * @return
	 */
	long getUid();
	
	/**
	 * Record must be dismissed due to time-to-live
	 * expiration in cache without becoming a real
	 * threat.
	 * @return boolean
	 */
	boolean isExpired();
	
	/**
	 * Record is marked as processed
	 * by the Monitor thread
	 * 
	 */
	void markAsProcessed();
	
	/**
	 * Check if record has been processed
	 * @return
	 */
	public boolean isProcessed();
	
	/**
	 * Gets Suspect host
	 * @return String
	 */
	String getHost();
	
	/**
	 * Gets message status
	 * @return message
	 */
	int getStatus();
	
	/**
	 * Gets message status
	 * @return message
	 */
	SipServletMessage getMessage();

}
