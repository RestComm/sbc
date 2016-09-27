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
package org.restcomm.chain.processor.impl;



import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.EndpointProcessor;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:36:25
 * @class   DefaultProcessor.java
 *
 */
public abstract class DefaultProcessor implements Processor {
	
	private static transient Logger LOG = Logger.getLogger(DefaultProcessor.class);
	
	private EventListenerList listenerList = new EventListenerList();
	
	protected ProcessorChain chain;
	protected String name;
	protected Type type;
	
	
	public DefaultProcessor() {
		type=Type.CHAIN;
		
	}
	
	public DefaultProcessor(String name) {
		this();
		setName(name);
		
	}
	
	public DefaultProcessor(ProcessorChain chain) {
		this.chain=chain;
		type=Type.SINGLE_PROCESSOR;
		
	}
	
	
	public DefaultProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}

	public void addProcessorListener(ProcessorListener listener) {
		 if(LOG.isDebugEnabled())
			 LOG.debug("Adding listener "+listener+" to "+this);
	     listenerList.add(ProcessorListener.class, listener);
	}
	
	
	/*
	 * void onProcessorProcessing (Object message);
	   void onProcessorEnd        (Processor processor);
	   void onProcessorAbort      (Processor processor);
	 */
	protected void fireProcessingEvent(Message message, Processor processor) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ProcessorListener.class) {             
	             ((ProcessorListener)listeners[i+1]).onProcessorProcessing(message, processor);
	         }
	         
	     }
	 }
	
	protected void fireEndEvent(Message message, Processor processor) {
		
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ProcessorListener.class) {             
	             ((ProcessorListener)listeners[i+1]).onProcessorEnd(message, processor);
	         }
	         
	     }
	 }
	
	protected void fireAbortEvent(Processor processor) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ProcessorListener.class) {             
	             ((ProcessorListener)listeners[i+1]).onProcessorAbort(processor);
	         }
	         
	     }
	 }
	
	
	
	public void process(MutableMessage message) throws ProcessorParsingException {
		if(message==null) {
			throw new ProcessorParsingException("null Messages not allowed");
		}
		if(LOG.isDebugEnabled())
			LOG.debug(">> process() message ["+message+"]");
		
		if(!message.isLinked()&& !(this instanceof EndpointProcessor )) {
			if(LOG.isDebugEnabled())
				LOG.debug("ABORT message ["+message+"] on chain "+chain.getName()+" on processor "+this);
			fireAbortEvent((Processor) getCallback());
		}
		else {
			fireProcessingEvent(message, (Processor) getCallback());
			getCallback().doProcess(message);
			fireEndEvent(message, (Processor) getCallback());
		}
		
		Processor nextLink = null;
		if(chain!=null) {
			if(LOG.isDebugEnabled()) {
				LOG.debug("DP "+type+" from callback "+((Processor)getCallback()).getName()+" chain "+chain.getName());
			}
			nextLink=chain.getNextLink(this);
		}
				
		
		if(nextLink!=null) {
			if(LOG.isDebugEnabled())
				LOG.debug("DP "+type+" from callback "+((Processor)getCallback()).getName()+" nextlink "+nextLink.getName());
			nextLink.process(message);
			
		}
		
	}
	
	
	@Override
	public Type getType() {
		return type;
	}
	

	@Override
	public abstract String getVersion();

	@Override
	public abstract String getName();

	@Override
	public abstract void setName(String name);

	@Override
	public abstract int getId();

	@Override
	public abstract ProcessorCallBack getCallback();
	
}
