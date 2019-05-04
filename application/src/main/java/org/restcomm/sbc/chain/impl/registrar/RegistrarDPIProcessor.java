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

package org.restcomm.sbc.chain.impl.registrar;


import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultDPIProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.chain.impl.registrar.RegistrarDPIProcessor;
import org.restcomm.sbc.managers.RouteManager;




/**
 * Specialized Registrar Processor. 
 *
 */
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    5 oct. 2016 10:02:15
 * @class   PreRegistrarDPIProcessor.java
 *
 */
public class RegistrarDPIProcessor extends DefaultDPIProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(RegistrarDPIProcessor.class);
	private String name="PRE-REGISTRAR DPI Processor";
	
	public RegistrarDPIProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
		
	}
	
	public RegistrarDPIProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	/**
	 * Simple DPI message rejector
	 * SBC contract does not allow REGISTER request
	 * coming from MZ
	 */
	
	private void processRequest(SIPMutableMessage message) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processRequest()");
	    }
		SipServletRequest dmzRequest=(SipServletRequest) message.getContent();
		
		// Register requests only accepted from DMZ
		
		if(!RouteManager.isFromDMZ(dmzRequest)) {
			if(LOG.isDebugEnabled()){
		          LOG.debug("Rejecting REGISTER from unauthorized NetworkPoint!");
		    }
			SipServletResponse dmzResponse =
					dmzRequest.createResponse(SipServletResponse.SC_FORBIDDEN);
							
			message.setContent(dmzResponse);
			message.unlink();
			return;
			
		}
		
		
		
	}
	
	private void processResponse(SIPMutableMessage message) {
		
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
		SIPMutableMessage m  =(SIPMutableMessage) message;
		
		SipServletMessage sm = m.getContent();
		
		m.setSourceLocalAddress(sm.getLocalAddr());
		m.setSourceRemoteAddress(sm.getRemoteAddr());
		
		
		
		if(sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}
	
	@Override
	public double getVersion() {
		return 1.0;
	}

}
