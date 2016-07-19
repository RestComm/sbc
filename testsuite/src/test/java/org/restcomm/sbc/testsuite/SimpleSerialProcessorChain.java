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

package org.restcomm.sbc.testsuite;

import org.apache.log4j.Logger;
import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.ProcessorParsingException;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    21/5/2016 12:23:37
 * @class   SimpleSerialProcessChain.java
 *
 */
public class SimpleSerialProcessorChain extends DefaultSerialProcessorChain implements ProcessorListener, ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(SimpleSerialProcessorChain.class);
	private String name="Simple Serial Process Chain";
	
	public SimpleSerialProcessorChain() {
		setName("SSPC");
		// initialize the chain
		// works with original message
		Processor c1 = new SimpleProcessor("c1", this);
		c1.addProcessorListener(this);
		Processor c2 = new SimpleProcessor("c2", this);
		c2.addProcessorListener(this);
		Processor c3 = new SimpleProcessor("c3", this);
		c3.addProcessorListener(this);
		Processor c4 = new SimpleProcessor("c4", this);
		c4.addProcessorListener(this);
		
		
		// set the chain of responsibility
		
		try {
			link(c1, c2);
			link(c2, c3);
			link(c3, c4);
			
		} catch (MalformedProcessorChainException e) {
			LOG.error("ERROR",e);
		}
		
		//this.addProcessorListener(this);
		
	}
	
	public void doProcess(Message message) {
		LOG.debug(">> doProcess() Callback from chain: "+getName());
		if(message!=null) {
			LOG.debug(">> doProcess() message ["+message+"]");
		}
		
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		LOG.info(">>onProcessorProcessing() "+processor.getType()+"("+processor.getName()+")["+message.getContent()+"]-"+message);
		
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		LOG.info(">>onProcessorEnd() "+processor.getType()+"("+processor.getName()+")["+message.getContent()+"]-"+message);
		
	}

	@Override
	public void onProcessorAbort(Processor processor) {
		LOG.info(">>onProcessorAbort() "+processor.getType()+"("+processor.getName()+")");
		
	}
	
	public static void main(String argv[]) throws ProcessorParsingException {
		new SimpleSerialProcessorChain().process(new StringBufferMessage("Mary has a little lamb"));
		
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
	public String getVersion() {
		return "1.0.0";
	}

	

}