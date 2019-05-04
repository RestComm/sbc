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
import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Processor;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 may. 2019 8:49:09
 * @class   ProcessorService.java
 *
 */
public class ProcessorService {

    private static ProcessorService service;
	private static transient Logger LOG = Logger.getLogger(ProcessorService.class);
	private HashMap<String, String> processors = new HashMap<String, String>();
	private ProcessorRepositoryWatcher watcher;

    private ProcessorService(ProcessorRepositoryWatcher watcher) throws IOException {
    	this.watcher = watcher;
    	
    }

    public static synchronized ProcessorService getInstance(ProcessorRepositoryWatcher watcher) throws IOException {
        if (service == null) {
            service = new ProcessorService(watcher);
        }
        return service;
    }


    public Constructor getProcessorConstructor(String name) throws ProcessorLoadException { 
    	ClassLoader parentClassLoader = ProcessorLoader.class.getClassLoader();
    	ProcessorLoader loader = new ProcessorLoader(parentClassLoader, watcher);
        Class<Processor> clazz;
        Constructor processorConstructor;
		try {
			clazz = loader.loadClass(normalize(name));
			
			processorConstructor = clazz
			          .getConstructor(ProcessorChain.class);
			      
		} catch (NullPointerException | StringIndexOutOfBoundsException | ClassNotFoundException  | NoSuchMethodException | SecurityException e) {
			// now try fully qualified class name
			try {
				clazz = loader.loadClass(name);
				
				processorConstructor = clazz
				          .getConstructor(ProcessorChain.class);
				      
			} catch (NullPointerException | ClassNotFoundException | NoSuchMethodException | SecurityException e1) {
				throw new ProcessorLoadException("Cannot load class "+name);
			}
		}
		
		return processorConstructor;
            
    }
    
    private String normalize(String fullClassName) throws StringIndexOutOfBoundsException {
  	  String className = fullClassName.substring(fullClassName.lastIndexOf('.'));
  	  return "chain"+className;
  	  
    }
    
    public void registerProcessor(String fullClassName) {
    	processors.put(normalize(fullClassName),  fullClassName);
    }
    
    public void unregisterProcessor(String normalizedClassName) {
    	processors.remove(normalizedClassName);
    }
    
    public String getProcessorFullClassName(String normalizedClassName) {
    	return processors.get(normalizedClassName);
    }
}
