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
package org.restcomm.sbc.media;

import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    2 dic. 2016 10:44:53
 * @class   PortManager.java
 *
 */
public class PortManager {
	
	private static PortManager portManager;
	
	private static final Logger LOG = Logger.getLogger(PortManager.class);
	
	
	private static int startPort=ConfigurationCache.getMediaStartPort();
	private static int endPort	=ConfigurationCache.getMediaEndPort();
	
	private int currentPort;
	
	private PortManager() {
		if(startPort==0) {
			startPort=10000;
		}
		if(endPort<startPort) {
			startPort=10000;
			endPort=20000;
		}
		if(startPort%2!=0)
			startPort++;
		
		currentPort=startPort;
		
		
	}

	public static PortManager getPortManager() {
		if(portManager==null)
			portManager=new PortManager();
		return portManager;
	}
	
	public synchronized int getNextAvailablePort()  {	
		currentPort+=2;
		
		if(currentPort>endPort)
			currentPort=startPort;
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Assigning Media Port "+currentPort);
		}
		return currentPort;
		

	}
	
	public synchronized int getCurrentPort()  {	
		return currentPort;
		
	}
	
	
	
}
