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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 may. 2019 8:50:16
 * @class   ProcessorLoader.java
 *
 */
public class ProcessorLoader extends ClassLoader{
	private URI uri;
	private static transient Logger LOG = Logger.getLogger(ProcessorLoader.class);

    public ProcessorLoader(ClassLoader parent, ProcessorRepositoryWatcher watcher)  {
        super(parent);
        
  	  	uri = watcher.getURI();
    }

    public Class loadClass(String name) throws ClassNotFoundException {
    	
    	if(!name.startsWith("chain."))
            return super.loadClass(name);
    	
        try {
        	name = name.substring(6);
            String url = uri.toString()+"/"+name+".class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();
            //LOG.info("defining class chain."+name);
            return defineClass("chain."+name,
                    classData, 0, classData.length);

        } catch (IOException e) {
        	if(LOG.isDebugEnabled()) {
        		LOG.debug("Cannot define class "+name+" "+e.getMessage());
        		LOG.debug("jumping to default class "+name);
        	}
            return super.loadClass(name, true);
        } 

       
    }
    
    /*
    
	public static void main(String[] args)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {

		ProcessorRepositoryWatcher watcher = null;
		try {
			watcher = new ProcessorRepositoryWatcher();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ClassLoader parentClassLoader = ProcessorLoader.class.getClassLoader();
		ProcessorLoader classLoader = new ProcessorLoader(parentClassLoader, watcher);

		Class myObjectClass = classLoader.loadClass("chain.NATHelperProcessor");

		Processor object1 = (Processor) myObjectClass.newInstance();

		// create new class loader so classes can be reloaded.
		classLoader = new ProcessorLoader(parentClassLoader, watcher);
		myObjectClass = classLoader.loadClass("chain.B2BUABuilderProcessor0101");

		object1 = (Processor) myObjectClass.newInstance();

	}
*/
}