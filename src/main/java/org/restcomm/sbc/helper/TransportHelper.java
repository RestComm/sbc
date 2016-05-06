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

package org.restcomm.sbc.helper;

import java.util.Hashtable;

import org.restcomm.sbc.adapter.TransportAdapter;
import org.restcomm.sbc.adapter.UnavailableTransportAdapterException;
import org.restcomm.sbc.adapter.impl.TCPTransportAdapter;
import org.restcomm.sbc.adapter.impl.UDPTransportAdapter;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:47:54
 * @class   TransportHelper.java
 * @project Servlet2.5SBC
 *
 */
public class TransportHelper {
	
	private Hashtable<String, TransportAdapter> transports=new Hashtable<String, TransportAdapter>();
	
	private static TransportHelper helper;
	
	private TransportHelper() {
		
		registerRouter("udp", new UDPTransportAdapter());
		registerRouter("tcp", new TCPTransportAdapter());
		
		
	}
	
	public static TransportHelper getHelper() {
		if(helper==null) {
			helper=new TransportHelper();
		}
		return helper;
	}
	
	
	public void registerRouter(String toTransport, TransportAdapter adapter) {
		
		transports.put(toTransport, adapter);
		
			
	}
	
	public TransportAdapter getAdapter(String transport) throws UnavailableTransportAdapterException {
		TransportAdapter adapter=transports.get(transport);
		if(adapter==null) {
			throw new UnavailableTransportAdapterException(transport+" transport adapter unavailable");
		}
		else
			return adapter;
	}
	
	
	
	

}
