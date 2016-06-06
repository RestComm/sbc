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
package org.restcomm.chain.processor.impl;

import org.restcomm.chain.processor.Message;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:38:16
 * @class   MutableMessage.java
 *
 */
public class MutableMessage implements Message {
	
	private Object wrappedObject;
	
	public MutableMessage() {
		
	}
	public MutableMessage(Object wrappedObject) {
		this.wrappedObject=wrappedObject;
	}
	public Object getWrappedObject() {
		return wrappedObject;
	}
	public void setWrappedObject(Object wrappedObject) {
		this.wrappedObject=wrappedObject;
	}
	public final ImmutableMessage getImmutable() {
		return new ImmutableMessage(wrappedObject);
	}
}
