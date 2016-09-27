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

package org.restcomm.sbc.managers;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.restcomm.sbc.adapter.ProtocolAdapter;
import org.restcomm.sbc.adapter.UnavailableProtocolAdapterException;
import org.restcomm.sbc.adapter.impl.TCPProtocolAdapter;
import org.restcomm.sbc.adapter.impl.UDPProtocolAdapter;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 sept. 2016 17:42:50
 * @class   ProtocolAdapterFactory.java
 *
 */
public class ProtocolAdapterFactory {
	
	public static String PROTOCOL_UDP 	="UDP";
	public static String PROTOCOL_TCP 	="TCP";
	public static String PROTOCOL_SCTP 	="SCTP";
	public static String PROTOCOL_TLS 	="TLS";
	public static String PROTOCOL_WS 	="WS";
	public static String PROTOCOL_WSS 	="WSS";
	
	private static transient Logger LOG = Logger.getLogger(ProtocolAdapterFactory.class);
	private HashMap<String, ProtocolAdapter> protocols=new HashMap<String, ProtocolAdapter>();
	
	private static ProtocolAdapterFactory protocolAdapterFactory;
	
	private ProtocolAdapterFactory() {
		
		registerRouter("UDP", new UDPProtocolAdapter());
		registerRouter("TCP", new TCPProtocolAdapter());
		
		
	}
	
	public static ProtocolAdapterFactory getProtocolAdapterFactory() {
		if(protocolAdapterFactory==null) {
			protocolAdapterFactory=new ProtocolAdapterFactory();
		}
		return protocolAdapterFactory;
	}
	
	
	private void registerRouter(String toProtocol, ProtocolAdapter adapter) {
		
		protocols.put(toProtocol, adapter);
		
			
	}
	
	public ProtocolAdapter getAdapter(String protocol) throws UnavailableProtocolAdapterException {
		if(protocol==null) {
			// implicit protocol
			protocol="UDP";
		}
		ProtocolAdapter adapter=protocols.get(protocol.toUpperCase());
		if(adapter==null) {
			throw new UnavailableProtocolAdapterException(protocol+" protocol adapter unavailable");
		}
		else {
			if(LOG.isTraceEnabled()){
		          LOG.trace(">> Factoring ProtocolAdapter ["+adapter.getProtocol()+"]"+adapter);
		    }
			return adapter;
		}
	}
	
	
	
	

}
