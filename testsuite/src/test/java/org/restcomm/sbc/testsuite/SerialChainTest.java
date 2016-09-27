/*******************************************************************************
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc, Eolos IT Corp and individual contributors
 * by the @authors tag.
 *
 * chain program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * chain program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with chain program.  If not, see <http://www.gnu.org/licenses/>
 *
 *******************************************************************************/
package org.restcomm.sbc.testsuite;

import org.junit.Test;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.sbc.chain.impl.DispatchDPIProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    19/5/2016 2:52:40
 * @class   SerialChainTest.java
 *
 */
public class SerialChainTest implements ProcessorListener {
private static transient Logger LOG = Logger.getLogger(SerialChainTest.class);
	
	protected Object preMessage;
	protected Object postMessage;
	
	public SerialChainTest() {
		
	}
	
	
	@Test//(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionLoop() {
		Throwable e = null;
		EmptySerialProcessorChain chain=new EmptySerialProcessorChain();
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor("c1", chain);
 		   Processor       c2=  new SimpleProcessor("c2", chain);
 		   Processor       c3=  new SimpleProcessor("c3", chain); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
 				chain.link(c1, c1);
 				chain.link(c2, c3);
 				chain.link(c3, new DispatchDPIProcessor("Dispatch", chain));	
	 			
			} catch (MalformedProcessorChainException e1) {
				e=e1;
			}
 			assertTrue(e instanceof MalformedProcessorChainException);
     	
    }
	
	
	
	@Test//(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionHole() {
		 Throwable e = null;
		 EmptySerialProcessorChain chain=new EmptySerialProcessorChain();
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(chain);
 		   Processor       c2=  new SimpleProcessor(chain);
 		   Processor       c3=  new SimpleProcessor(chain); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
				chain.link(c1, c2);
				chain.link(c2, null);
	 			chain.link(c3, new DispatchDPIProcessor("Dispatch", chain));	
	 			
			} catch (MalformedProcessorChainException e1) {
				e=e1;
			}
 			assertTrue(e instanceof MalformedProcessorChainException);
    }
	
	@Test//(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionJustInChain() {
		Throwable e = null;
		 EmptySerialProcessorChain chain=new EmptySerialProcessorChain();
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(chain);
 		   Processor       c2=  new SimpleProcessor(chain);
 		   Processor       c3=  new SimpleProcessor(chain); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
 				chain.link(c1, c2);
 				chain.link(c1, c2);
 				chain.link(c3, new DispatchDPIProcessor("Dispatch", chain));	
	 			
			} catch (MalformedProcessorChainException e1) {
				e=e1;
			}
 			assertTrue(e instanceof MalformedProcessorChainException);
    }
	
	@Test
    public void processShouldTraverseChain() {
    	 
    	// initialize the chain
 		// works with original message
		 EmptySerialProcessorChain chain=new EmptySerialProcessorChain();
 		   Processor       c1=  new SimpleProcessor("c1-simple", chain);
 		   Processor       c2=  new SimpleProcessor("c1-simple", chain);
 		   Processor       c3=  new SimpleProcessor("c1-simple", chain); 		
 		
 		// set the chain of responsibility
 		   chain.addProcessorListener(this);
		   c1.addProcessorListener(this);
		   c2.addProcessorListener(this);
		   c3.addProcessorListener(this);
 		
 			try {
 				chain.link(c1, c2);
 				chain.link(c2, c3);
 				//chain.link(c3, new DispatchProcessor("Dispatch", chain));	
	 			
			} catch (MalformedProcessorChainException e1) {
				fail(e1.getMessage());
			}
 			
     	
     	StringBufferMessage message=new StringBufferMessage("Mary has a little lamb");
    	
    	try {
    		chain.process(message);
		} catch (ProcessorParsingException e) {
			fail(e.getMessage());
		}
    	
        assertSame(preMessage, postMessage);
     	
    }
	
	@Test
    public void processShouldUnlinkChain() {
    	 
    	// initialize the chain
 		// works with original message
		 EmptySerialProcessorChain chain=new EmptySerialProcessorChain();
 		   Processor       c1=  new SimpleProcessor("c1-simple", chain);
 		   Processor       c2=  new UnlinkProcessor("c2-unlinker", chain);
 		   Processor       c3=  new SimpleProcessor("c3-simple", chain); 	
 		   Processor       c4=  new SimpleProcessor("c4-simple", chain);
		   Processor       c5=  new SimpleProcessor("c5-simple", chain);
		   Processor       c6=  new LastProcessor  ("Last-Processor", chain); 	
 		
 		// set the chain of responsibility
 		   chain.addProcessorListener(this);
 		   c1.addProcessorListener(this);
 		   c2.addProcessorListener(this);
 		   c3.addProcessorListener(this);
 		   c4.addProcessorListener(this);
		   c5.addProcessorListener(this);
		   c6.addProcessorListener(this);
 		 
 		
 			try {
 				chain.link(c1, c2);
 				chain.link(c2, c3);
 				chain.link(c3, c4);
 				chain.link(c4, c5);
 				chain.link(c5, c6);
 				//chain.link(c6, new DispatchProcessor("Dispatch", chain));	
	 			
			} catch (MalformedProcessorChainException e1) {
				fail(e1.getMessage());
			}
 			
     	
     	StringBufferMessage message=new StringBufferMessage("Mary has a little lamb");
    	
    	try {
    		chain.process(message);
		} catch (ProcessorParsingException e) {
			fail(e.getMessage());
		}
    	
        assertSame(preMessage, postMessage);
     	
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
	
	public static void main(String argv[]) {
		SerialChainTest test=new SerialChainTest();
		test.processShouldTraverseChain();
		test.processShouldUnlinkChain();
	}

	

	
}