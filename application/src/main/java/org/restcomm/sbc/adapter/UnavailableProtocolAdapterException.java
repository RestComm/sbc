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

package org.restcomm.sbc.adapter;	

import java.io.IOException;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 13:28:26
 * @class   UnavailableProtocolAdapterException.java
 *
 */
public class UnavailableProtocolAdapterException extends IOException { 
	
	private static final long serialVersionUID = 3026030994173806039L;

	/**
	 * Transport is not available for adaptation
	 * 
	 * @param transport
	 */
	public UnavailableProtocolAdapterException(String transport) {
		super(transport);
	}

}
