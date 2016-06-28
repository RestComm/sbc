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
package org.restcomm.sbc.notification.impl;

import javax.servlet.sip.SipServletMessage;

import org.restcomm.sbc.notification.SuspectActivityElectable;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    4/6/2016 13:12:18
 * @class   SuspectActivity.java
 *
 */
public class SuspectActivity implements SuspectActivityElectable {
	
	private long uid;
	private boolean processed = false;
	private int status;
	private long timestamp;
	private String host;
	private SipServletMessage message;


	public SuspectActivity (long uid) {
		this.uid=uid;
		this.timestamp = System.currentTimeMillis();

	}
	
	public SuspectActivity (long uid, String host) {
		this(uid);
		this.host = host;
	}
	

	public boolean isExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public String getHost() {
		return host;
	}
	
	public SipServletMessage getLastMessage() {
		return message;
	}
	
	public void setLastMessage(SipServletMessage message) {
		this.message=message;
	}


	public long getTimestamp() {
		return timestamp;
	}

	
	public SipServletMessage getMessage() {
		return message;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public long getUid() {
		return uid;
	}

	@Override
	public void markAsProcessed() {
		processed= true;
		
	}

	@Override
	public int getStatus() {
		return status;
	}

	public boolean isProcessed() {
		return processed;
	}

}
