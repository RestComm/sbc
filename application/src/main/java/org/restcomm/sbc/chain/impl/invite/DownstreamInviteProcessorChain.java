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

package org.restcomm.sbc.chain.impl.invite;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.chain.processor.spi.impl.ProcessorFactory;
import org.restcomm.chain.processor.spi.impl.ProcessorLoadException;
import org.restcomm.chain.processor.spi.impl.ProcessorRepositoryListener;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.chain.impl.B2BUABuilderProcessor;
import org.restcomm.sbc.chain.impl.DispatchDPIProcessor;
import org.restcomm.sbc.chain.impl.IncomingDPIProcessor;
import org.restcomm.sbc.chain.impl.NATHelperProcessor;
import org.restcomm.sbc.chain.impl.ProtocolAdaptProcessor;
import org.restcomm.sbc.chain.impl.TopologyHideProcessor;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    16/6/2016 14:33:42
 * @class   DownstreamInviteProcessorChain.java
 *
 */
public class DownstreamInviteProcessorChain extends DefaultSerialProcessorChain implements 
	ProcessorCallBack, 
	ProcessorListener,
	ProcessorRepositoryListener {
	
	private static transient Logger LOG = Logger.getLogger(DownstreamInviteProcessorChain.class);
	private String name="Downstream INVITE Processor Chain";
	private Processor c1, c2, c3, c4, c5, c6, c7, c8;
	ProcessorFactory processorFactory;
	
	public DownstreamInviteProcessorChain() {
		processorFactory = (ProcessorFactory) ShiroResources.getInstance().get(ProcessorFactory.class);		
		processorFactory.getWatcher().addProcessorRepositoryListener(this);
		
		build();
			
	}
	
	private void build() {
		// initialize the chain
		try {
			c1 = processorFactory.lookup(IncomingDPIProcessor.class.getName(), this);
			c2 = processorFactory.lookup(InviteDPIProcessor.class.getName(),this);			
			c3 = processorFactory.lookup(B2BUABuilderProcessor.class.getName(),this);
			c4 = processorFactory.lookup(NATHelperProcessor.class.getName(),this);
			c5 = processorFactory.lookup(InviteProcessor.class.getName(),this);			
			c6 = processorFactory.lookup(ProtocolAdaptProcessor.class.getName(),this);
			c7 = processorFactory.lookup(TopologyHideProcessor.class.getName(),this);
			c8 = processorFactory.lookup(DispatchDPIProcessor.class.getName(), this);
		} catch (ProcessorLoadException e1) {
			LOG.error("Cannot create chain!", e1);
		}
		
		
		// set the chain of responsibility
		try {
			relink(c1, c2);
			link(c2, c3);
			link(c3, c4);
			link(c4, c5);
			link(c5, c6);
			link(c6, c7);
			link(c7, c8);
			
			c1.addProcessorListener(this);
			c2.addProcessorListener(this);
			c3.addProcessorListener(this);
			c4.addProcessorListener(this);
			c5.addProcessorListener(this);
			c6.addProcessorListener(this);
			c7.addProcessorListener(this);
			c8.addProcessorListener(this);
			
		} catch (MalformedProcessorChainException e) {
			LOG.error("ERROR",e);
		}
		
		this.addProcessorListener(this);
		LOG.info("Loaded (v. "+getVersion()+") "+getName());
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
		m.setContent( doProcess(m.getContent()));
	}
	
	@Override
	public double getVersion() {
		return 1.0;
	}
	
	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		SipServletMessage m = (SipServletMessage) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug(">>onProcessorProcessing() "+processor.getType()+"("+processor.getName()+")");
			LOG.debug(">>onProcessorProcessing() "+m.getMethod()      +"[From:"+m.getFrom()+"][To:"+m.getTo()+"]");
			if(m instanceof SipServletResponse) {
				SipServletResponse r = (SipServletResponse) m;
				LOG.debug(">>onProcessorProcessing() "+r.getStatus()+":"+r.getReasonPhrase());
			}
		}
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		SipServletMessage m = (SipServletMessage) message.getContent();
		if(LOG.isDebugEnabled()) {
			LOG.debug(">>onProcessorEnd() "+processor.getType()+"("+processor.getName()+")");
			LOG.debug(">>onProcessorEnd() "+m.getMethod()      +"[From:"+m.getFrom()+"][To:"+m.getTo()+"]");
			if(m instanceof SipServletResponse) {
				SipServletResponse r = (SipServletResponse) m;
				LOG.debug(">>onProcessorEnd() "+r.getStatus()+":"+r.getReasonPhrase());
			}
		}
		
	}

	@Override
	public void onProcessorAbort(Processor processor) {
		if(LOG.isDebugEnabled())
			LOG.debug(">>onProcessorAbort() "+processor.getType()+"("+processor.getName()+")");
	}

	@Override
	public void onProcessorUnlink(Processor processor) {
		if(LOG.isDebugEnabled())
			LOG.debug(">>onProcessorUnlink() "+processor.getType()+"("+processor.getName()+")");
		
	}

	
	@Override
	public void onProcessorRemoved(String simpleClassName) {
		Processor processor = null;	
		
		try {			
			processor = processorFactory.recoverDefault("chain."+simpleClassName, this);
		} catch (IOException  | IllegalArgumentException  e) {
			LOG.error(simpleClassName+" cannot be updated! "+e.getMessage());
		}
		build();
		LOG.info("Processor "+simpleClassName+" removed.");
	}

	@Override
	public void onProcessorCreated(String simpleClassName) {
		
		Processor processor = null;	
		
		try {			
			processor = processorFactory.lookup("chain."+simpleClassName, this);
		} catch (IOException  | IllegalArgumentException  e) {
			LOG.error(simpleClassName+" cannot be updated! "+e.getMessage());
		}
		
		if(simpleClassName.equals("IncomingDPIProcessor")) {
			c1.removeProcessorListener(this);
			c1 = processor;
		}
		else if(simpleClassName.equals("InviteDPIProcessor")) {
			c2.removeProcessorListener(this);
			c2 = processor;
		}
		else if(simpleClassName.equals("B2BUABuilderProcessor")) {
			c3.removeProcessorListener(this);
			c3 = processor;
		}
		else if(simpleClassName.equals("NATHelperProcessor")) {
			c4.removeProcessorListener(this);
			c4 = processor;
		}
		else if(simpleClassName.equals("InviteProcessor")) {
			c5.removeProcessorListener(this);
			c5 = processor;
		}
		else if(simpleClassName.equals("ProtocolAdaptProcessor")) {
			c6.removeProcessorListener(this);
			c6 = processor;
		}
		else if(simpleClassName.equals("TopologyHideProcessor")) {
			c7.removeProcessorListener(this);
			c7 = processor;
		}
		
		else if(simpleClassName.equals("DispatchDPIProcessor")) {
			c8.removeProcessorListener(this);
			c8 = processor;
			c8.setName("Dispatch");
		}
		else {
			LOG.debug("Processor "+processor+" not in chain "+this);
		}
		
		// set the chain of responsibility
		try {
			relink(c1, c2);
			link(c2, c3);
			link(c3, c4);
			link(c4, c5);
			link(c5, c6);
			link(c6, c7);
			link(c7, c8);
			c1.addProcessorListener(this);
			c2.addProcessorListener(this);
			c3.addProcessorListener(this);
			c4.addProcessorListener(this);
			c5.addProcessorListener(this);
			c6.addProcessorListener(this);
			c7.addProcessorListener(this);
			c8.addProcessorListener(this);
			
		} catch (MalformedProcessorChainException e) {
			LOG.error("ERROR",e);
		}
				
		LOG.info("Processor "+processor+" updated for chain "+this);
		
		
	}
	
}