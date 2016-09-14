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



import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
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

 /**
 * Specialized Message Processor responsible to hide topology
 * MZ Data. 
 *
 */

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    13 sept. 2016 18:10:42
 * @class   NATHelperProcessor.java
 *
 */
public class NATHelperProcessor extends DefaultProcessor
	implements ProcessorCallBack {


	private static transient Logger LOG = Logger.getLogger(NATHelperProcessor.class);
	private String name;
	private Address contactAddress;
	
	public NATHelperProcessor(ProcessorChain callback) {
		super(callback);
	}
	public NATHelperProcessor(String name,ProcessorChain callback) {
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
		this.name=name;
		
	}

	private SipServletResponse processResponse(SipServletResponse message) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processResponse()");
	    }
	
		return message;
	}

	private SipServletRequest processRequest(SipServletRequest dmzRequest) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processRequest()");
	    }
		
		try {
			contactAddress = dmzRequest.getAddressHeader("Contact");
			
		} catch (ServletParseException e) {
			LOG.error("Cannot get Contact Address!");
			
		}
		/*
		 * Replace Contact address from IP/Port the message
		 * is coming from
		 */
		SipURI uri = (SipURI) contactAddress.getURI();
		uri.setHost(dmzRequest.getRemoteAddr());
		uri.setPort(dmzRequest.getRemotePort());
		contactAddress.setURI(uri);
		dmzRequest.setAddressHeader("Contact", contactAddress);
		
		return dmzRequest;
			
	}


	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}
	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
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
	
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m=(SIPMutableMessage) message;
		m.setContent(doProcess(m.getContent()));
	}

	
}