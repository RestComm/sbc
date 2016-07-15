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

package org.restcomm.sbc.chain.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TooManyHopsException;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.managers.MessageUtil;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:49:01
 * @class   DownStreamProcessor.java
 *
 */
public class DownStreamProcessor extends DefaultProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(DownStreamProcessor.class);

	private String name="MZ fork B2BUA leg processor";
	
	public DownStreamProcessor(ProcessorChain callback) {
		super(callback);
	}
	
	public DownStreamProcessor(String name, ProcessorChain callback) {
		super(name, callback);
	}

	

	public SipServletMessage process(SipServletMessage message)  {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> process()");
	    }
		
		if(message instanceof SipServletRequest) {
			message=processRequest((SipServletRequest) message);
		}
		
		return message;
		
		
	}
	
	private SipServletRequest processRequest(SipServletRequest mzRequest) {
		MessageUtil.tracer(mzRequest);
		
		
		Map<String, List<String>> headers=new HashMap<String, List<String>>();

		B2buaHelper helper = mzRequest.getB2buaHelper();
		SipServletRequest forkedRequest = null;
		try {
			forkedRequest = helper.createRequest(mzRequest, true,	headers);
		} catch (IllegalArgumentException e) {
			LOG.error("ERROR",e);
		} catch (TooManyHopsException e) {
			LOG.error("ERROR",e);
		}
		forkedRequest.setHeader("X-SBC", "true");
		forkedRequest.setHeader("X-Orig-Contact",forkedRequest.getHeader("Contact"));
		
		
		
		return forkedRequest;
		
		
	}

	
	public String getName() {
		return name;
	}



	public int getId() {
		return this.hashCode();
	}



	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> doProcess()");
	    }
		
		if(message instanceof SipServletRequest) {
			message=processRequest((SipServletRequest) message);
		}
		
		return message;
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
		SIPMutableMessage m=(SIPMutableMessage) message;
		m.setContent(doProcess(m.getContent()));
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

	
}
