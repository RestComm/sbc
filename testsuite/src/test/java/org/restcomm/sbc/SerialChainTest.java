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
package org.restcomm.sbc;

import org.junit.Test;
import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.DispatchProcessor;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    19/5/2016 2:52:40
 * @class   SerialChainTest.java
 *
 */
public class SerialChainTest extends DefaultSerialProcessorChain implements ProcessorListener, ProcessorCallBack {
private static transient Logger LOG = Logger.getLogger(SerialChainTest.class);
	
	protected Object preMessage;
	protected Object postMessage;
	
	
	
	@Test(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionLoop() {
    	 
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor("c1", this);
 		   Processor       c2=  new SimpleProcessor("c2", this);
 		   Processor       c3=  new SimpleProcessor("c3", this); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1, c1);
				link(c2, c3);
	 			link(c3, new DispatchProcessor("Dispatch", this));	
	 			
			} catch (MalformedProcessorChainException e1) {
				LOG.error(e1.getMessage());
			}
     	
    }
	
	@Test(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionDPI() {
    	 
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(this);
 		   Processor       c2=  new SimpleProcessor(this);
 		   Processor       c3=  new SimpleProcessor(this); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1, c1);
				link(c2, c3);
	 			link(c3, new DispatchProcessor("Dispatch", this));
	 			
			} catch (MalformedProcessorChainException e1) {
				LOG.error(e1.getMessage());
			}
     	
    }
	
	@Test(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionHole() {
    	 
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(this);
 		   Processor       c2=  new SimpleProcessor(this);
 		   Processor       c3=  new SimpleProcessor(this); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1, c2);
				link(c2, null);
	 			link(c3, new DispatchProcessor("Dispatch", this));	
	 			
			} catch (MalformedProcessorChainException e1) {
				LOG.error(e1.getMessage());
			}
     	
    }
	
	@Test(expected = MalformedProcessorChainException.class)
    public void processShouldThrowMalformedProcessorChainExceptionJustInChain() {
    	 
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(this);
 		   Processor       c2=  new SimpleProcessor(this);
 		   Processor       c3=  new SimpleProcessor(this); 		
 		
 		// set the chain of responsibility
 		
 		
 			try {
				link(c1, c2);
				link(c1, c2);
	 			link(c3, new DispatchProcessor("Dispatch", this));	
	 			
			} catch (MalformedProcessorChainException e1) {
				LOG.error(e1.getMessage());
			}
     	
    }
	
	@Test
    public void processShouldTraverseChain() {
    	 
    	// initialize the chain
 		// works with original message
 		   Processor       c1=  new SimpleProcessor(this);
 		   Processor       c2=  new SimpleProcessor(this);
 		   Processor       c3=  new SimpleProcessor(this); 		
 		
 		// set the chain of responsibility
 		   this.addProcessorListener(this);
 		
 			try {
				link(c1, c2);
				link(c2, c3);
	 			link(c3, new DispatchProcessor("Dispatch", this));	
	 			
			} catch (MalformedProcessorChainException e1) {
				fail(e1.getMessage());
			}
 			
     	
     	MutableMessage message=new MutableMessage("Mary has a little lamb");
    	
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
	public void onProcessorAbort(Message message, Processor processor) {
		LOG.debug(">> onProcessorAbort():"+processor.getName());
		
	}
	
	public static void main(String argv[]) {
		SerialChainTest test=new SerialChainTest();
		test.processShouldTraverseChain();
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		
		message=new MutableMessage("Mutate "+message.getWrappedObject());
		
	}

	@Override
	public String getName() {
		return "Test Serial Chain";
	}

	@Override
	public void setName(String name) {
		
		
	}

	
}