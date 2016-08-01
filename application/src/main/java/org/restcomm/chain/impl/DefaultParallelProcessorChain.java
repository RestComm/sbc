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
package org.restcomm.chain.impl;

import java.util.HashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.restcomm.chain.ParallelProcessorChain;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.impl.DefaultDPIProcessor;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ImmutableMessage;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    8/6/2016 18:02:24
 * @class   DefaultParallelProcessorChain.java
 *
 */
public abstract class DefaultParallelProcessorChain extends DefaultDPIProcessor  
	implements ParallelProcessorChain  {
	
	private static transient Logger LOG = Logger.getLogger(DefaultParallelProcessorChain.class);
	
	private Processor nextLink;
	
	
	private HashMap<Integer, Processor> processors=new HashMap<Integer, Processor>();
	
	public DefaultParallelProcessorChain() {
		super();
		
	}
	
	public DefaultParallelProcessorChain(String name) {
		super(name);
		
		
	}
	
	@Override
	public void process(MutableMessage message) throws ProcessorParsingException  {
		
			
			final ImmutableMessage immutableMessage=(ImmutableMessage)message;
		    
			
			fireProcessingEvent(immutableMessage, (Processor) getCallback());
			
			ExecutorService taskExecutor = Executors.newFixedThreadPool(processors.size());
			
			for(final Processor processor:processors.values()) {
				taskExecutor.execute(new ProcessorTask(immutableMessage, processor));
			}
			
			taskExecutor.shutdown();
			
			try {
			  taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
			 
			}
			
			if(message!=null) {
				if(LOG.isDebugEnabled())
					LOG.debug("<< DPC "+type+" output message ["+message+"]");
			}
			
			fireEndEvent(message, (Processor) getCallback());
			Processor nextLink = null;
			
			if(LOG.isDebugEnabled())
				LOG.debug("DPC "+type+" from callback "+getName()+" chain "+getCallback());
			nextLink=getNextLink((DefaultProcessor) getCallback());
				
			if(nextLink!=null) {
				if(LOG.isDebugEnabled())
					LOG.debug("DPC "+type+" from callback "+getName()+" nextlink "+nextLink);
				nextLink.process(message);
			}
			
		
	}
	

	@Override
	public String getName() {
		return "Raw Default Parallel Chain Implementation";
	}

	@Override
	public int getId() {
		return this.hashCode();
	}


	@Override
	public void link(Processor processor) throws MalformedProcessorChainException {
		if(processor==null) {
			throw new MalformedProcessorChainException("Processors could not be null");
			
		}
		processors.put(processor.getId(), processor);
		
	}	
	
	@Override
	public Processor getNextLink(Processor processor) {
		return nextLink;
	}

	@Override
	public void setNextLink(Processor processor)
			throws MalformedProcessorChainException {
		this.nextLink=processor;
		
	}

	class ProcessorTask implements Runnable {
		
		private ImmutableMessage message;
		private Processor processor;

		ProcessorTask(ImmutableMessage message, Processor processor) {
			this.message=message;
			this.processor=processor;
		}

		@Override
		public void run() {
			try {			
				fireProcessingEvent(message, processor);
				processor.getCallback().doProcess(message);
				if(chain!=null) {
					if(LOG.isDebugEnabled())
						LOG.debug("DPC "+type+" from callback "+getCallback()+" chain "+chain);			
				}
				fireEndEvent(message, processor);
				
			} catch (ProcessorParsingException e) {
				LOG.error(e.getMessage());
				
			}	
			
		}
		
	}

}
