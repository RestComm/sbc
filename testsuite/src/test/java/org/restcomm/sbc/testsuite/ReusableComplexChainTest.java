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
import org.restcomm.chain.processor.impl.ProcessorParsingException;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    8/6/2016 17:07:11
 * @class   ReusableComplexChainTest.java
 *
 */
public class ReusableComplexChainTest implements Runnable, ProcessorListener {
private static transient Logger LOG = Logger.getLogger(ReusableComplexChainTest.class);
	
private ComplexProcessorChain cpc;
private final int CONCURRENT_MESSAGES = 100;
private int threadsInTerminationState = 0;	
private boolean ok=true;

	@Test
    public void complexChainInstanceShouldBeReusable() {
		cpc=new ComplexProcessorChain();
		cpc.addProcessorListener(this);
		for(int i=0;i<CONCURRENT_MESSAGES;i++) {
			new Thread(this).start();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("Not Passed ok="+ok+" threads="+threadsInTerminationState, ok && threadsInTerminationState == CONCURRENT_MESSAGES);
		
	}

	@Override
	public void run() {
		try {
			cpc.process(new StringBufferMessage(Thread.currentThread().getName()));
		} catch (ProcessorParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String argv[]) {
		ReusableComplexChainTest test=new ReusableComplexChainTest();
		test.complexChainInstanceShouldBeReusable();
	}

	@Override
	public void onProcessorProcessing(Message message, Processor processor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessorEnd(Message message, Processor processor) {
		String content=(String) message.getProperty("content").toString();
		LOG.info(">>>>onChainEnd() "+processor.getType()+"("+processor.getName()+")["+message.getProperty("content")+"]-"+message);	
		// 4 processors traversed is ok
		ok&=content.startsWith("***");
		
		threadsInTerminationState++;
	}

	@Override
	public void onProcessorAbort(Processor processor) {
		// TODO Auto-generated method stub
		
	}
}