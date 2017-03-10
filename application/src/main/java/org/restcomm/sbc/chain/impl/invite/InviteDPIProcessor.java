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

package org.restcomm.sbc.chain.impl.invite;


import java.io.IOException;

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
import org.restcomm.sbc.managers.RouteManager;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 oct. 2016 12:44:29
 * @class   InviteDPIProcessor.java
 *
 */
public class InviteDPIProcessor extends DefaultDPIProcessor implements ProcessorCallBack {
	/**
	 * 		
	 */
	private static transient Logger LOG = Logger.getLogger(InviteDPIProcessor.class);
	private String name="INVITE DPI Processor";
	
	
	
	public InviteDPIProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
	}
	
	public InviteDPIProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	private void processInviteRequest(SIPMutableMessage message) {
		SipServletRequest request=(SipServletRequest) message.getContent();
		
		if(LOG.isTraceEnabled()) {	
			LOG.trace("INVITE REQUEST DMZ:"+RouteManager.isFromDMZ(request));
			LOG.trace("From/To "+request.getFrom()+"/"+request.getTo());
		}
		/*
		  SipServletResponse trying = request.createResponse(SipServletResponse.SC_TRYING);
		 
		
		try {
			trying.send();
			
		} catch (IOException e) {
			LOG.error("TRYING ERROR",e);
		}
		
		*/		
	}
	
	private void processInviteResponse(SIPMutableMessage message) {
		SipServletResponse response=(SipServletResponse) message.getContent();
		
		if(LOG.isTraceEnabled()) {	
			LOG.trace("INVITE RESPONSE DMZ:"+RouteManager.isFromDMZ(response));
			
			LOG.trace("From/To "+response.getFrom()+"/"+response.getTo());
		}
		if (response.getStatus() == SipServletResponse.SC_OK) {			
			
				SipServletRequest ackRequest = response.createAck();
				try {
					ackRequest.send();
				} catch (IOException e) {
					LOG.error("Cannot send ACK!", e);
				}
				
		}
		
	}
	
	private void processByeRequest(SIPMutableMessage message)  {
		SipServletRequest request=(SipServletRequest) message.getContent();
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Request BYE: "	+ request.getMethod());	
		}
		/*
		SipServletResponse response = request.createResponse(200);
		try {
			response.send();
		} catch (IOException e) {
			LOG.error("",e);
		}
		*/
		
	}
	
	private void processAckRequest(SIPMutableMessage message)  {
		
		
	}
	
	private void processPrackRequest(SIPMutableMessage message)  {	
		SipServletRequest request=(SipServletRequest) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Request PRACK: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());	
		}
				
		SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
		message.setContent(ok);
		message.unlink();
		/*
		try {
			ok.send();
		} catch (IOException e) {
			LOG.error("",e);
		}	
		*/
		
		
		
	}
	
	private void processInfoRequest(SIPMutableMessage message)  {
		
	}
	
	private void processCancelRequest(SIPMutableMessage message)  {	
		SipServletRequest request=(SipServletRequest) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Request CANCEL: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());	
		}
				
		SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
		SipServletResponse terminated = request.createResponse(SipServletResponse.SC_REQUEST_TERMINATED);
		
		try {
			ok.send();
			terminated.send();
		} catch (IOException e) {
			LOG.error("",e);
		}	
		
		
	}
	
	private void processByeResponse(SIPMutableMessage message)  {
		SipServletResponse response=(SipServletResponse) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Response BYE: "	+ response.getMethod());	
		}
		
		
	}
	
	private void processInfoResponse(SIPMutableMessage message)  {
		SipServletResponse response=(SipServletResponse) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Response INFO: "	+ response.getMethod());	
		}
		
		
	}
	
	
	private void processCancelResponse(SIPMutableMessage message)  {	
		SipServletResponse response=(SipServletResponse) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Got Response CANCEL: "	+ response.getMethod());	
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
		
		m.setSourceLocalAddress(sm.getLocalAddr());
		m.setSourceRemoteAddress(sm.getRemoteAddr());
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> doProcess()");
	    }
		
		if(sm instanceof SipServletRequest) {
			if(sm.getMethod().equals("INVITE"))
				processInviteRequest(m);
			else if(sm.getMethod().equals("BYE"))
				processByeRequest(m);
			else if(sm.getMethod().equals("ACK"))
				processAckRequest(m);
			else if(sm.getMethod().equals("PRACK"))
				processPrackRequest(m);
			else if(sm.getMethod().equals("CANCEL"))
				processCancelRequest(m);
			else if(sm.getMethod().equals("INFO"))
				processInfoRequest(m);
			else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Request METHOD "+sm.getMethod()+" not supported!");
				}
			}
		}
		if(sm instanceof SipServletResponse) {
			if(sm.getMethod().equals("INVITE"))
				processInviteResponse(m);
			else if(sm.getMethod().equals("BYE"))
				processByeResponse(m);
			else if(sm.getMethod().equals("CANCEL"))
				processCancelResponse(m);
			else if(sm.getMethod().equals("INFO"))
				processInfoResponse(m);
			else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Response METHOD "+sm.getMethod()+" not supported!");
				}
			}
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
