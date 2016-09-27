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

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.Message;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:36:08
 * @class   DefaultDPIProcessor.java
 *
 */
public abstract class DefaultDPIProcessor extends DefaultProcessor   {

	private static transient Logger LOG = Logger.getLogger(DefaultDPIProcessor.class);

	public DefaultDPIProcessor() {
		super();
		type=Type.CHAIN;
		
	}
	
	public DefaultDPIProcessor(String name) {
		super(name);
		type=Type.CHAIN;
		
	}
	
	public DefaultDPIProcessor(ProcessorChain chain) {
		super(chain);
		type=Type.SINGLE_PROCESSOR;
		
	}
	
	
	public DefaultDPIProcessor(String name, ProcessorChain chain) {
		super(name, chain);
		
	}

	@Override
	public void process(MutableMessage message) throws ProcessorParsingException {
		if(LOG.isDebugEnabled())
			LOG.debug(">> process() message ["+message+"]");
		
		ImmutableMessage immutableMessage=(ImmutableMessage)message;
		
	
		fireProcessingEvent(immutableMessage, (Processor) getCallback());
		
		getCallback().doProcess((Message)immutableMessage);
		
		fireEndEvent(immutableMessage, (Processor) getCallback());
		
		
		Processor nextLink = null;
		if(chain!=null) {
			if(LOG.isDebugEnabled())
				LOG.debug("DPI "+type+" from callback "+((Processor)getCallback()).getName()+" chain "+chain.getName());
			nextLink=chain.getNextLink(this);
		}		
		
		if(nextLink!=null) {
			if(LOG.isDebugEnabled())
				LOG.debug("DPI "+type+" from callback "+((Processor)getCallback()).getName()+" nextlink "+nextLink.getName());
			nextLink.process(message);
		}
		
	}
	
}
