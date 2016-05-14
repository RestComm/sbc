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
package org.restcomm.sbc.chain;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;

import org.restcomm.sbc.processor.Processor;


/**
 * 
 * @author  Oscar Andres Carriles <ocarriles@eolos.la>
 * @date    24/4/2016 22:41:10
 * @class   MessageProcessor.java
 * @project Servlet2.5SBC
 *
 */

/**
 * A ProcessorChain is a component of a chain of responsibility
 * responsible in the construction of the chain of Processors
 * that will interact in the transformation of the message.
 * it enqueues processors and controls the chain life-cycle.
 * 
 * 
 */
public interface ProcessorChain {
	
	
	/**
	 * Gets next Message processor in the chain
	 * 
	 * @return nextInChain
	 */
	Processor getNextLink(Processor processor);
	
	/**
	 * Sets next Processor in the chain
	 * 
	 * @param nextInChain
	 */
	void link(Processor processor, Processor nextInChain);
	
	
	/**
	 * unlinks from the chain
	 * breaking its prefixed sequence
	 * causing immediate message process
	 * termination. 
	 */
	
	void unlink(Processor processor);
	
	/**
	 * Processes the Chain
	 * 
	 * @param processor
	 * @param message
	 * @throws IOException
	 */
	void processChain(Processor processor, SipServletMessage message) throws IOException;
	
	
	
}