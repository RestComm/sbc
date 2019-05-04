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
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;


import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3 may. 2019 8:49:30
 * @class   ProcessorRepositoryWatcher.java
 *
 */
public class ProcessorRepositoryWatcher implements Runnable {
	private EventListenerList listenerList = new EventListenerList();
	private static transient Logger LOG = Logger.getLogger(ProcessorRepositoryWatcher.class);
	private WatchService watcher;
	private Path dir;
	
	public ProcessorRepositoryWatcher(String path) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		
		
		dir = FileSystems.getDefault().getPath(path+"/processors/chain");
		
		try {
		    WatchKey key = dir.register(watcher,
		                           ENTRY_CREATE,
		                           ENTRY_DELETE,
		                           ENTRY_MODIFY);
		} catch (IOException x) {
		    LOG.error("ERR:", x);
		}
		
		
	}
	
	public URI getURI() {
		return dir.toUri();
	}
	
	public void addProcessorRepositoryListener(ProcessorRepositoryListener listener) {
		 if(LOG.isDebugEnabled())
			 LOG.debug("Adding listener "+listener+" to "+this);
	     listenerList.add(ProcessorRepositoryListener.class, listener);
	}
	
	public void removeProcessorRepositoryListener(ProcessorRepositoryListener listener) {
		 if(LOG.isDebugEnabled())
			 LOG.debug("Removing listener "+listener+" from "+this);
	     listenerList.remove(ProcessorRepositoryListener.class, listener);
	}
	
	
	
	protected void fireProcessorCreateEvent(String simpleClassName) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	  
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ProcessorRepositoryListener.class) {        	 
	             ((ProcessorRepositoryListener)listeners[i+1]).onProcessorCreated(simpleClassName);
	         }
	         
	     }
	 }
	
	protected void fireProcessorRemoveEvent(String simpleClassName) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ProcessorRepositoryListener.class) {             
	             ((ProcessorRepositoryListener)listeners[i+1]).onProcessorRemoved(simpleClassName);
	         }
	         
	     }
	 }
	
	
	@Override
	public void run() {
		for (;;) {
			
		    // wait for key to be signaled
		    WatchKey key;
		    try {
		        key = watcher.take();
		    } catch (InterruptedException x) {
		        return;
		    }

		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();

		        // This key is registered only
		        // for ENTRY_CREATE events,
		        // but an OVERFLOW event can
		        // occur regardless if events
		        // are lost or discarded.
		        if (kind == OVERFLOW) {
		            continue;
		        }

		        // The filename is the
		        // context of the event.
		        WatchEvent<Path> ev = (WatchEvent<Path>)event;
		        Path filename = ev.context();

		        
		        
	            // Resolve the filename against the directory.
	            // If the filename is "test" and the directory is "foo",
	            // the resolved name is "test/foo".
	            Path child = dir.resolve(filename);
	            
	            if (filename.toString().endsWith(".class")) {
	            	if(kind == ENTRY_DELETE) {
			        	// unload this class
	    	            // System.out.println("Class "+ev.kind().name()+":"+filename);
	    	            this.fireProcessorRemoveEvent(filename.toString().replaceAll(".class", ""));
			        }
			        else if(kind == ENTRY_CREATE) {
			        	// load class
			            // System.out.println("Class "+ev.kind().name()+":"+filename);	
			        	try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
			            this.fireProcessorCreateEvent(filename.toString().replaceAll(".class", ""));
			        }
	                continue;
	            }
		        
		       
		    }

		    // Reset the key -- this step is critical if you want to
		    // receive further watch events.  If the key is no longer valid,
		    // the directory is inaccessible so exit the loop.
		    boolean valid = key.reset();
		    if (!valid) {
		        break;
		    }
		}
		
	}

}
