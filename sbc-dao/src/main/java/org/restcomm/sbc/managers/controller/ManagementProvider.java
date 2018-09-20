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
 */
package org.restcomm.sbc.managers.controller;

import java.io.IOException;
import java.util.List;
import org.restcomm.sbc.bo.Connector;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 nov. 2016 3:24:55
 * @class   ManagementProvider.java
 *
 */
public interface ManagementProvider  {	
	
	public final static int TOMCAT		 = 0; 
	public final static int TOMCAT_EMBED = 1; 
	public final static int JBOSS		 = 2; 
	
	public void init();
	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws IOException;
	
	public boolean addSipConnector(String ipAddress, int port, String transport, String interfaceName) throws IOException;
	
	public boolean addInterface(String name, String ipAddress) throws IOException;
	
	public List<Connector> getConnectors();
		
	public void close() throws IOException;	
	
	public void reload();
	
	public int getCPULoadAverage();
	
	public int getMemoryUsage();
	
	public int getContext();
	
	
}
