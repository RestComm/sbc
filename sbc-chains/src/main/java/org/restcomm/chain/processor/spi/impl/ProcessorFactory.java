/*
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
 */

package org.restcomm.chain.processor.spi.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Processor;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 may. 2019 8:50:35
 * @class   ProcessorFactory.java
 *
 */
public class ProcessorFactory  {
  private static transient Logger LOG = Logger.getLogger(ProcessorFactory.class);
  private static ProcessorRepositoryWatcher watcher;
  private static ProcessorFactory factory;
  
  public ProcessorFactory(String path) throws ProcessorLoadException  {
	  try {
		watcher = new ProcessorRepositoryWatcher(path);
	} catch (IOException e) {
		throw new ProcessorLoadException("Cannot create watcher "+e.getMessage());
	}
	  new Thread(watcher).start();
	  
  }
  
  public static ProcessorFactory getInstance(String path) throws ProcessorLoadException {
	  if(factory == null) {
		  factory = new ProcessorFactory(path);
	  }
	  return factory;
  }
  
  public static ProcessorFactory getInstance() throws ProcessorLoadException {
	  return factory;
  }
	
  public ProcessorRepositoryWatcher getWatcher() {
	  return watcher;
  }
  
  public Processor recoverDefault(String name, ProcessorChain chain) throws ProcessorLoadException {
		ProcessorService service = null;
	
		try {
			service = ProcessorService.getInstance(watcher);
			
		} catch (IOException e) {
			throw new ProcessorLoadException("Service cannot be created " + e.getMessage());
		}
		try {
			name = service.getProcessorFullClassName(name);
			return (Processor) service.getProcessorConstructor(name).newInstance(chain);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ProcessorLoadException("Processor cannot be created " + e.getMessage());
		}
	
	}
  
	public Processor lookup(String name, ProcessorChain chain) throws ProcessorLoadException {
		ProcessorService service = null;
	
		try {
			service = ProcessorService.getInstance(watcher);
			service.registerProcessor(name);
			
		} catch (IOException e) {
			throw new ProcessorLoadException("Service cannot be created " + e.getMessage());
		}
		try {
			return (Processor) service.getProcessorConstructor(name).newInstance(chain);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ProcessorLoadException("Processor cannot be created " + e.getMessage());
		}
	
	}
  
  
/*
@Override
public void onProcessorRemoved(String simpleClassName) {
	LOG.info("Processor "+simpleClassName+" removed.");
}

@Override
public void onProcessorCreated(String simpleClassName) {
	Processor processor = null;	
	try {
		processor = pf.lookup("chain."+simpleClassName, null);
	} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		LOG.error(simpleClassName+" cannot be updated! "+e.getMessage());
	}	
	LOG.info("Processor "+processor+" created/updated.");

	
}*/
}
