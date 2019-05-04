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
package org.restcomm.chain.processor;

import java.io.IOException;

import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.impl.ProcessorParsingException;


/**
 * A Message Processor is a component of a chain of responsibility
 * that acts as a pipe, receives a message, process it and feeds
 * the processed message to the next Message Processor component 
 * in the chain. It's up the MessageProcessorChain to concatenate
 * MessageProcessors in the chain.
 * 
 */
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    13/6/2016 14:32:22
 * @class   Processor.java
 *
 */
public interface Processor {
	
	
	enum Type {
		CHAIN,
		SINGLE_PROCESSOR
	}
	/**
	 * Gets Processor version
	 * 
	 * @return version
	 */
	double getVersion();
	
	/**
	 * Gets Processor vendor
	 * 
	 * @return vendor
	 */
	String getVendor();
	
	/**
	 * Gets type
	 * 
	 * @return type
	 */
	Type getType();
	
	/**
	 * Gets name
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Sets name
	 * 
	 */
	void setName(String name);
	
	
	/**
	 * Gets id
	 * 
	 * @return id
	 */
	int getId();
	
	/**
	 * EventListeners
	 * @param listener
	 */
	void addProcessorListener(ProcessorListener listener);
	
	/**
	 * EventListeners
	 * @param listener
	 */
	void removeProcessorListener(ProcessorListener listener);
	
	/**
	 * Processes the message
	 * 
	 * @param message
	 * @throws IOException
	 * @return message
	 */
	
	void process(Message message) throws ProcessorParsingException;
	
	/**
	 * Gets Callback
	 * to calling processor
	 * 
	 */
	ProcessorCallBack getCallback();	
	
}