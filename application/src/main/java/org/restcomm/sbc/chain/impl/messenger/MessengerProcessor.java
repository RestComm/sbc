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

package org.restcomm.sbc.chain.impl.messenger;



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
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    17 feb. 2017 18:37:22
 * @class   MessengerProcessor.java
 *
 */
public class MessengerProcessor extends DefaultProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(MessengerProcessor.class);
	private String name="MESSAGE Processor";
	
	
	public MessengerProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
		
	}
	
	public MessengerProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	/**
	 * Throttling management 
	 * @param dmzRequest
	 * @return message
	 */
	
	private void processRequest(SIPMutableMessage message) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processRequest()");
	    }
		
		
	}
	
	private void processResponse(SIPMutableMessage message) {
		
		SipServletResponse mzResponse=(SipServletResponse) message.getContent();
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processResponse()");
	          LOG.trace(">> response received: \n"+mzResponse.toString());
	         
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
		SIPMutableMessage m  =(SIPMutableMessage) message;
		
		SipServletMessage sm = m.getContent();
		
		if(sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
