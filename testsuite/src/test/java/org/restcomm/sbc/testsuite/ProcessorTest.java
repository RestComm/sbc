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
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorListener;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.apache.log4j.Logger;

import static org.junit.Assert.*;

public class ProcessorTest implements ProcessorListener {
	
	private static transient Logger LOG = Logger.getLogger(ProcessorTest.class);
	
	protected Object preObject;
	protected Object postObject;
	
	
    @Test 
    public void messageInstanceShouldBePreserved() {
    	
    	Processor c1 = new SimpleProcessor("c1", null);
    	
    	c1.addProcessorListener(this);
    	//chain.addProcessorListener(this);
    	
    	StringBufferMessage message=new StringBufferMessage("Mary has a little lamb");
    	
    	try {
			c1.process(message);
		} catch (ProcessorParsingException e) {
			LOG.error(e.getMessage());
		}
    	
        assertEquals(preObject, postObject);

    }
    @Test//(expected = ProcessorParsingException.class)
    public void processShouldThrowProcessorParsingException() {
    	Throwable e = null;
     	Processor       c1 = new SimpleProcessor("c1", null);
     	
     	MutableMessage message=null;
    	
    	try {
			c1.process(message);
		} catch (ProcessorParsingException e1) {
			e=e1;
		}
    	
    	assertTrue("UnExpected exception thrown "+e.getMessage(), e instanceof ProcessorParsingException);
     	
    }

	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		
		//if(LOG.isTraceEnabled()){
	          LOG.debug(">> onProcessorProcessing():"+message);
	    //}
		this.preObject=message;
		
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		
		//if(LOG.isTraceEnabled()){
	          LOG.debug(">> onProcessorEnd():"+processor.getName()+":"+message);
	    //}
		this.postObject=message;
		
	}

	@Override
	public void onProcessorAbort(Processor processor) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProcessorUnlink(Processor processor) {
		if(LOG.isDebugEnabled())
			LOG.debug(">>onProcessorUnlink() "+processor.getType()+"("+processor.getName()+")");
		
	}
	
	public static void main(String argv[]) {
		ProcessorTest test=new ProcessorTest();
		test.messageInstanceShouldBePreserved();
		test.processShouldThrowProcessorParsingException();
		
	}
}