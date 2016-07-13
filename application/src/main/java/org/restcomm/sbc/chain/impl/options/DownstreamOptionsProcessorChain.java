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

package org.restcomm.sbc.chain.impl.options;

import javax.servlet.sip.SipServletMessage;

import org.apache.log4j.Logger;
import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.DispatchDPIProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.chain.impl.B2BUABuilderProcessor;
import org.restcomm.sbc.chain.impl.TopologyHideProcessor;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    16/6/2016 14:33:42
 * @class   DownstreamInviteProcessorChain.java
 *
 */
public class DownstreamOptionsProcessorChain extends DefaultSerialProcessorChain implements ProcessorCallBack, ProcessorListener  {
	private static transient Logger LOG = Logger.getLogger(DownstreamOptionsProcessorChain.class);
	private String name="Downstream OPTIONS Processor Chain";
	
	public DownstreamOptionsProcessorChain() {
		// initialize the chain
		// works with original message
		Processor c1 = new OptionsProcessor(this);
		c1.addProcessorListener(this);
		Processor c2 = new B2BUABuilderProcessor(this);
		c2.addProcessorListener(this);
		// works with B2BUA Leg message
		Processor c3 = new TopologyHideProcessor(this);
		c3.addProcessorListener(this);
		Processor c4 = new DispatchDPIProcessor("Dispatch", this);
		c4.addProcessorListener(this);
		
		// set the chain of responsibility
		try {
			link(c1, c2);
			link(c2, c3);
			link(c3, c4);
		} catch (MalformedProcessorChainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.addProcessorListener(this);
		
			
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name=name;
		
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
		return message;
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
	
	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		SipServletMessage m = (SipServletMessage) message.getContent();
		LOG.debug(">>onProcessorProcessing() "+processor.getType()+"("+processor.getName()+")[<-"+m.getRemoteAddr()+"][To:"+m.getTo()+"]");	
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		SipServletMessage m = (SipServletMessage) message.getContent();
		LOG.debug(">>onProcessorEnd() "+processor.getType()+"("+processor.getName()+")[<-"+m.getRemoteAddr()+"][To:"+m.getTo()+"]");	
		
	}

	@Override
	public void onProcessorAbort(Processor processor) {	
		LOG.debug(">>onProcessorAbort() "+processor.getType()+"("+processor.getName()+")");
	}
	
}