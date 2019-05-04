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

package org.restcomm.chain.testsuite;

import org.apache.log4j.Logger;
import org.restcomm.chain.impl.DefaultParallelProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.ProcessorParsingException;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    19/5/2016 5:25:18
 * @class   SimpleParallelProcessChain.java
 *
 */
public class SimpleParallelProcessorChain extends DefaultParallelProcessorChain 
	implements ProcessorListener, ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(SimpleParallelProcessorChain.class);
	private String name="Simple Parallel Processor Chain";
	
	public SimpleParallelProcessorChain() {
		   super();
		   setName("Parallel");
		// initialize the chain
		// works with original message
		   Processor       c5=  new SimpleDPIProcessor("DPI-c5", this);
		   c5.addProcessorListener(this);
		   Processor       c6=  new SimpleDPIProcessor("DPI-c6", this);
		   c6.addProcessorListener(this);
		   Processor       c7=  new SimpleDPIProcessor("DPI-c7", this);
		   c7.addProcessorListener(this);
		  		
		
		// set the chain of responsibility
		
		try {
			link(c5);
			link(c6);
			link(c7);
			
			
		} catch (MalformedProcessorChainException e) {
			LOG.error("ERROR",e);
		}
		//this.addProcessorListener(this);	
		
	}
	
	
	public static void main(String argv[]) throws ProcessorParsingException {
		StringBufferMutableMessage message=new StringBufferMutableMessage("Immutable Mary has a little lamb");
		new SimpleParallelProcessorChain().process(message);
	}

	public void doProcess(Message message) throws ProcessorParsingException {
		LOG.debug(">> doProcess() Callback from chain: "+getName());
		

	}

	public String getName() {
		return name;
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

	@Override
	public void onProcessorUnlink(Processor processor) {
		if(LOG.isDebugEnabled())
			LOG.debug(">>onProcessorUnlink() "+processor.getType()+"("+processor.getName()+")");
		
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
	public double getVersion() {
		return 1.0;
	}

	

}