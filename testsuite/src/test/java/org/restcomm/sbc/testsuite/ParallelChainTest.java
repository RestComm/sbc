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
package org.restcomm.sbc.testsuite;

import org.junit.Test;
import org.restcomm.chain.impl.DefaultParallelProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.ProcessorParsingException;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    19/5/2016 6:04:43
 * @class   ParallelChainTest.java
 *
 */
public class ParallelChainTest extends DefaultParallelProcessorChain implements ProcessorListener, ProcessorCallBack {
private static transient Logger LOG = Logger.getLogger(ParallelChainTest.class);
	
	protected Object preMessage;
	protected Object postMessage;
	
	
	
	
	
	
	
	@Test//(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionHole() {
		Throwable e = null; 
    	// initialize the chain
 		// works with original message
		Processor       c1=  new SimpleDPIProcessor(this);
		Processor       c2=  new SimpleDPIProcessor(this);
		   
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1);
				link(c2);
				link(null);
				
	 			
			} catch (MalformedProcessorChainException e1) {
				e=e1;
			}
 			assertTrue(e instanceof MalformedProcessorChainException);
    }
	
	@Test//(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionJustInChain() {
    	 
    	// initialize the chain
 		// works with original message
		Processor       c1=  new SimpleDPIProcessor(this);
		Processor       c3=  new SimpleDPIProcessor(this); 	
 		
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1);
				link(c1);
	 			link(c3);
	 			
	 			
			} catch (MalformedProcessorChainException e1) {
				LOG.error(e1.getMessage());
			}
     	
    }
	
	@Test
    public void processShouldTraverseChain() {
    	 
    	// initialize the chain
 		// works with original message
		Processor       c1=  new SimpleDPIProcessor(this);
		Processor       c2=  new SimpleDPIProcessor(this);
		Processor       c3=  new SimpleDPIProcessor(this); 
 		
 		// set the chain of responsibility
 		   this.addProcessorListener(this);
 		
 			try {
				link(c1);
				link(c2);
	 			link(c3);	
	 			
			} catch (MalformedProcessorChainException e1) {
				fail(e1.getMessage());
			}
 			
     	
 			StringBufferMessage message=new StringBufferMessage("Mary has a little lamb");
    	
    	
		try {
			process(message);
		} catch (ProcessorParsingException e) {
			fail(e.getMessage());
		}
		
    	
        assertSame(preMessage, postMessage);
     	
    }
	
	
	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		LOG.debug(">> onProcessorProcessing():"+message);
		
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		LOG.debug(">> onProcessorEnd():"+processor.getName()+":"+message);
		
	}

	@Override
	public void onProcessorAbort(Processor processor) {
		LOG.debug(">> onProcessorAbort():"+processor.getName());
		
	}
	
	public static void main(String argv[]) {
		ParallelChainTest test=new ParallelChainTest();
		test.processShouldTraverseChain();
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		
	}

	@Override
	public void setName(String name) {
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

	
}