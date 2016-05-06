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

package org.restcomm.sbc.processor.impl;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.adapter.TransportAdapter;
import org.restcomm.sbc.adapter.UnavailableTransportAdapterException;
import org.restcomm.sbc.chain.ProcessorChain;
import org.restcomm.sbc.helper.TransportHelper;
import org.restcomm.sbc.processor.Processor;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:49:35
 * @class   TransportAdaptProcessor.java
 * @project Servlet2.5SBC
 *
 */
public class TransportAdaptProcessor implements Processor {
	
	private static transient Logger LOG = Logger.getLogger(TransportAdaptProcessor.class);


	protected TransportHelper helper;
	
	public TransportAdaptProcessor(ProcessorChain processorChain) {
		helper=TransportHelper.getHelper();	
	}
	
	

	public SipServletMessage process(SipServletMessage message)  {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> process() "+getName());
	    }
		
		
		if(message instanceof SipServletRequest) {		
			message=processRequest((SipServletRequest) message);
		}
		if(message instanceof SipServletResponse) {		
			message=processResponse((SipServletResponse) message);
		}
		
		return message;
		
	}
	
	private SipServletResponse processResponse(SipServletResponse message) {
		TransportAdapter adapter = null;
		try {
			adapter = helper.getAdapter(ConfigurationCache.getMzTransport());
		} catch (UnavailableTransportAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (SipServletResponse) adapter.adapt(message);
	}

	private SipServletRequest processRequest(SipServletRequest mzRequest) {
		TransportAdapter adapter = null;
		try {
			adapter = helper.getAdapter(ConfigurationCache.getMzTransport());
		} catch (UnavailableTransportAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (SipServletRequest) adapter.adapt(mzRequest);
			
	}

	
	public String getName() {
		return "Transport Adapt Processor";
	}



	public int getId() {
		return this.hashCode();
	}


}
