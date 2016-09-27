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
 */

package org.restcomm.chain.impl;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.restcomm.chain.ParallelProcessorChain;
import org.restcomm.chain.SerialProcessorChain;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    21/5/2016 12:19:59
 * @class   DefaultSerialProcessorChain.java
 *
 */
public abstract class DefaultSerialProcessorChain extends DefaultProcessor 
	implements SerialProcessorChain {
	
	private static transient Logger LOG = Logger.getLogger(DefaultSerialProcessorChain.class);
	
	
	private Processor startPoint;

	
	private HashMap<Integer, Processor> processors=new HashMap<Integer, Processor>();
	
	public DefaultSerialProcessorChain() {
		super();
		
	}
	
	public DefaultSerialProcessorChain(String name) {
		super(name);
		
		
	}
	
	public void link(Processor processor, Processor nextInChain) throws MalformedProcessorChainException {
		
		if(processor==null||nextInChain==null) {
			throw new MalformedProcessorChainException("Processors could not be null");
			
		}
		else if(processors.containsValue((nextInChain))) {
			throw new MalformedProcessorChainException("Processor "+nextInChain.getName()+" already in chain");
		}
		else if(processor.getId()==nextInChain.getId()) {
			throw new MalformedProcessorChainException("Loop detected "+nextInChain.getName()+" illegal recursion");
		}
		
		else if(startPoint!=null && processor.getId()==startPoint.getId()) {
			throw new MalformedProcessorChainException("Loop detected "+nextInChain.getName()+" illegal recursion, Check double start point");
		}
		
		if(processors.isEmpty()) {
			startPoint=processor;
		}
		
		if(processor instanceof ParallelProcessorChain) {
			ParallelProcessorChain ppc=(ParallelProcessorChain) processor;
			ppc.setNextLink(nextInChain);
			
		}
		processors.put(processor.getId(), nextInChain);
		
		
	}

	public Processor getNextLink(Processor processor) {
		return processors.get(processor.getId());
	}
	
	
	@Override
	public int getId() {
		return this.hashCode();
	}
	
	@Override
	public void process(MutableMessage message) throws ProcessorParsingException {
		if(LOG.isDebugEnabled())
			LOG.debug(">> DSC "+getType()+" input message ["+message+"]");
		
		
		fireProcessingEvent(message, (Processor) getCallback());
		
		getCallback().doProcess(message);
		startPoint.process(message);
		
		
		if(chain!=null) {
			if(LOG.isDebugEnabled())
				LOG.debug("DSC "+getType()+" from callback "+getCallback()+" chain "+chain);
		}	
		Processor nextLink=getNextLink((DefaultProcessor) getCallback());
			
		if(nextLink!=null) {
			if(LOG.isDebugEnabled())
				LOG.debug("DSC "+getType()+" from callback "+getCallback()+" nextlink "+nextLink);
			nextLink.process(message);
		}
		
		
		
		fireEndEvent(message, (Processor) getCallback());

		
	}
	
}
