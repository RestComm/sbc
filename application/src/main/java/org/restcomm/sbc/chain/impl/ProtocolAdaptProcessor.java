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
 */

package org.restcomm.sbc.chain.impl;

import java.net.NoRouteToHostException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.sbc.adapter.ProtocolAdapter;
import org.restcomm.sbc.adapter.UnavailableProtocolAdapterException;
import org.restcomm.sbc.managers.ProtocolAdapterFactory;
import org.restcomm.sbc.managers.RouteManager;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:49:35
 * @class   ProtocolAdaptProcessor.java
 *
 */
public class ProtocolAdaptProcessor extends DefaultProcessor
	implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(ProtocolAdaptProcessor.class);
	private String name="Protocol Adapt Processor";

	protected ProtocolAdapterFactory protocolAdapterFactory;
	protected RouteManager routeManager;
	
	public ProtocolAdaptProcessor(ProcessorChain processorChain) {
		super(processorChain);
		protocolAdapterFactory=ProtocolAdapterFactory.getProtocolAdapterFactory();	
		
	}
	
	public ProtocolAdaptProcessor(String name, ProcessorChain processorChain) {
		this(processorChain);
		setName(name);
			
	}
	
	
	private void processResponse(Message message) {
		//SipServletResponse response=(SipServletResponse) message.getContent();
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processResponse()");
	          LOG.trace(message.toString());
	          LOG.trace(message.getMetadata());
	    }
		ProtocolAdapter adapter = null;
		try {
			adapter = protocolAdapterFactory.getAdapter(message.getTargetTransport());//response.getTransport());
			
			message=adapter.adapt(message);
		} catch (UnavailableProtocolAdapterException e) {
			LOG.error("ERROR",e);
		} catch (NoRouteToHostException e) {
			LOG.error("ERROR",e);
		}
	
	}

	private void processRequest(Message message) {
		//SipServletRequest request=(SipServletRequest) message.getContent();
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processRequest()");
	          LOG.trace(message.toString());
	    }
		ProtocolAdapter adapter = null;
		try {
			
			adapter = protocolAdapterFactory.getAdapter(message.getTargetTransport());//request.getTransport());
			
			message=adapter.adapt(message);
		} catch (UnavailableProtocolAdapterException e) {
			LOG.error("ERROR",e);
		} catch (NoRouteToHostException e) {
			LOG.error("ERROR",e);
		}
			
	}

	
	public String getName() {
		return name;
	}



	public int getId() {
		return this.hashCode();
	}


	@Override
	public void setName(String name) {
		this.name=name;
		
	}



	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		
		SipServletMessage sm=(SipServletMessage) message.getContent();
		
		if(sm instanceof SipServletRequest) {		
			processRequest(message);
		}
		if(sm instanceof SipServletResponse) {		
			processResponse(message);
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}


}
