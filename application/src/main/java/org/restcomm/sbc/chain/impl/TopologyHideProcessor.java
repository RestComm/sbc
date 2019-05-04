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
 * @date    28 sept. 2016 17:58:44
 * @class   TopologyHideProcessor.java
 *
 */
public class TopologyHideProcessor extends DefaultProcessor
	implements ProcessorCallBack {


	private static transient Logger LOG = Logger.getLogger(TopologyHideProcessor.class);

	
	public TopologyHideProcessor(ProcessorChain callback) {
		super(callback);
	}
	public TopologyHideProcessor(String name,ProcessorChain callback) {
		super(name, callback);
	}
	
	
	public String getName() {
		return "Topology Hide Processor";
	}



	public int getId() {
		return this.hashCode();
	}


	@Override
	public void setName(String name) {
		this.name=name;
		
	}

	private SipServletResponse processResponse(SipServletResponse message) {
		
		//SipServletRequest oRequest=(SipServletRequest) message.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
		if(LOG.isTraceEnabled()) {
			//LOG.trace("o          Contact "+message.getHeader("Contact"));
			//LOG.trace("o Orig Req Contact "+oRequest.getHeader("Contact"));
		//	LOG.trace("o Message follows:\n"+message.toString());
			
		}
		/*
		 * Replace Contact address from original Message
		 * is coming from
		 */
		
		
		//message.setHeader("Contact", oContact);
		
		return message;
	}

	private SipServletRequest processRequest(SipServletRequest mzRequest) {
		
		//SipServletRequest oRequest=(SipServletRequest) mzRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
	    if(LOG.isTraceEnabled()) {
	  			LOG.trace("o          Contact "+mzRequest.getHeader("Contact"));
	  		//	LOG.trace("o Orig Req Contact "+oRequest.getHeader("Contact"));
	  		//	LOG.trace("o Message follows:\n"+message.toString());
	  		
	    }
		return mzRequest;
			
	}


	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}
	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
		
		if(message instanceof SipServletRequest) {		
			message=processRequest((SipServletRequest) message);
		}
		if(message instanceof SipServletResponse) {		
			message=processResponse((SipServletResponse) message);
		}
		
		return message;
	}
	
	
	@Override
	public double getVersion() {
		return 1.0;
	}
	
	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m=(SIPMutableMessage) message;
		m.setContent(doProcess(m.getContent()));
	}

	
}