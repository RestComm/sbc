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
package org.restcomm.sbc.chain.impl;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.ConfigurationCache;


/**
 * @author ocarriles@eolos.la (Oscar Andres Carriles)
 * @date 13 sept. 2016 18:10:42
 * @class NATHelperProcessor.java
 *
 */
public class NATHelperProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(NATHelperProcessor.class);

	public NATHelperProcessor(ProcessorChain callback) {
		super(callback);
	}

	public NATHelperProcessor(String name, ProcessorChain callback) {
		super(name, callback);
	}

	public String getName() {
		return "NAT Helper Processor";
	}

	public int getId() {
		return this.hashCode();
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	private void processResponse(SIPMutableMessage message) {
		SipServletResponse response=(SipServletResponse) message.getContent();
		SipServletRequest request=response.getRequest();
		//SipServletRequest orequest=(SipServletRequest) request.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processResponse()");
			LOG.trace(">> request Coming from host: "+request.getRemoteHost());
			LOG.trace(">> request Coming from port: "+request.getRemotePort());		
			
		}
		
		SipURI fromURI 	= (SipURI) request.getFrom().getURI();
		SipURI contactURI = null;
		
		contactURI = ConfigurationCache.getSipFactory().createSipURI(fromURI.getUser(), request.getRemoteHost());
		contactURI.setPort(request.getRemotePort());
		
		if(LOG.isTraceEnabled()){ 
			LOG.trace("Patching NATed Contact Address to: "+contactURI.toString());
		}
		
		/*
		 * Replace Contact address from IP/Port the message is coming from
		 */

		response.setAddressHeader("Contact", ConfigurationCache.getSipFactory().createAddress(contactURI));
		
		message.setContent(response);
		
	}

	private void processRequest(SIPMutableMessage message) {
		
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		
		if( sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if (sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}

}
