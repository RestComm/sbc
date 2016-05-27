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

import org.restcomm.sbc.threat.Threat;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    14/5/2016 16:52:21
 * @class   SuspectActivityElectable.java
 *
 */
public interface SuspectActivityElectable {
	
	/**
	 * Record must be dismissed due to time-to-live
	 * expiration in cache without becoming a real
	 * threat.
	 * @return boolean
	 */
	boolean isExpired();
	
	/**
	 * A real typified threat
	 * has been detected
	 * @return Threat
	 */
	Threat becomesThreatCandidate();
	
	/**
	 * Gets Suspect host
	 * @return String
	 */
	String getHost();
	
	/**
	 * Gets authorization denial count
	 * @return count
	 */
	int getUnauthorizedAccessCount();
	
	/**
	 * Gets last message
	 * @return message
	 */
	SipServletMessage getLastMessage();

}
