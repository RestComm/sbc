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
package org.restcomm.chain;
import org.restcomm.chain.impl.MalformedProcessorChainException;

import org.restcomm.chain.processor.Processor;




/**
 * A ProcessorChain is a component of a chain of responsibility
 * responsible in the construction of the chain of Processors
 * that will interact in the transformation of the message.
 * it enqueues processors and controls the chain life-cycle.
 * 
 * 
 */
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    18/5/2016 12:12:24
 * @class   ParallelProcessorChain.java
 *
 */
public interface ParallelProcessorChain extends ProcessorChain {
		
	
	/**
	 * adds Processor to the chain
	 * 
	 * @param processor
	 */
	void link(Processor processor) throws MalformedProcessorChainException ;
	
	/**
	 * adds next hop Processor to the chain
	 * 
	 * @param processor
	 */
	void setNextLink(Processor processor) throws MalformedProcessorChainException ;
	
}