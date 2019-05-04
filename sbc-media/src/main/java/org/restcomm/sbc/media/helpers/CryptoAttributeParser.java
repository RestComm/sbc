/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
 */

package org.restcomm.sbc.media.helpers;

import java.util.regex.Pattern;

import org.mobicents.media.server.io.sdp.SdpException;
import org.mobicents.media.server.io.sdp.SdpParser;


/**
 * Parses SDP text to construct {@link CryptoAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class CryptoAttributeParser implements SdpParser<CryptoAttribute> {
	
	// TODO use proper IP address regex instead of [0-9\\.]+
	// private static final String REGEX = "^a=crypto:\\w+\\s\\d\\s\\w+\\s\\d+\\s[0-9\\.]+\\s\\d+\\s(typ)\\s\\w+(\\stcptype\\s\\w+)?(\\s(raddr)\\s[0-9\\.]+\\s(rport)\\s\\d+)?\\s(generation)\\s\\d+$";
	// private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return true;
		//return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public CryptoAttribute parse(String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(9).split(" ");
			int index = 0;

			// extract data from SDP
			
			short tagId = Short.parseShort(values[index++]);
			String cryptoSuite = values[index++];
			
			String masterKey = values[index++];
			String[] mk = masterKey.trim().split(":");
			masterKey=mk[1];
			
			// Create object from extracted data
			CryptoAttribute crypto = new CryptoAttribute(tagId, cryptoSuite, masterKey);
			
			return crypto;
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(CryptoAttribute field, String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(9).split(" ");
			int index = 0;

			// extract data from SDP
			
			short tagId = Short.parseShort(values[index++]);
			String cryptoSuite = values[index++];
			
			
			String masterKey = values[index++];
			String[] mk = masterKey.trim().split(":");
			masterKey=mk[1];
			
			// Create object from extracted data
			field.setTagId(tagId);
			field.setCryptoSuite(cryptoSuite);
			field.setMasterKey(masterKey);
			
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
