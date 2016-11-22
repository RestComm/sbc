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
package org.restcomm.chain.processor;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:34:57
 * @class   Message.java
 *
 */
public interface Message {
	
	static final int SOURCE_DMZ = 0;
	static final int SOURCE_MZ  = 1;
	
	static final int TARGET_DMZ = 0;
	static final int TARGET_MZ  = 1;
	static final int TARGET_B2BUA=2;
	
	static final String[] TARGET={"DMZ", "MZ", "B2BUA"};
	
	
	/**
	 * Message Flow direction.
	 * @return direction
	 */
	int getDirection();
	
	/**
	 * Message Flow target.
	 * @return target
	 */
	int getTarget();
	
	/**
	 * Get the IP to where
	 * the Message initially came
	 * @return String.
	 */
	String getSourceLocalAddress();
	
	/**
	 * Get the IP from where
	 * the Message initially came
	 * @return String.
	 */
	String getSourceRemoteAddress();
	
	/**
	 * Get the IP to where
	 * the Message goes in SBC domain
	 * @return String.
	 */
	String getTargetLocalAddress();
	
	/**
	 * Get the IP to where
	 * the Message goes to reach its
	 * final destination.
	 * @return String
	 */
	String getTargetRemoteAddress();
	
	/**
	 * Get the target Transport
	 * @return String
	 */
	String getTargetTransport();
	
	/**
	 * Get the source Transport
	 * @return String
	 */
	String getSourceTransport();
	
	/**
	 * Get the attached metadata
	 * @return Object
	 */
	Object getMetadata();
	
	
	Object getContent() ;
	
	/**
	 * unlinks itself from its processor container
	 * disabling any further processing of the message,
	 * producing immediate dispatch. 
	 */
	
	void unlink();
	
	/**
	 * aborts delivery of the message
	 * interrupting the process chain. 
	 */
	
	void abort();
	
	/**
	 * Gets link message state
	 */
	
	boolean isLinked();
	
	/**
	 * Gets aborted message state
	 */
	
	boolean isAborted();
}
