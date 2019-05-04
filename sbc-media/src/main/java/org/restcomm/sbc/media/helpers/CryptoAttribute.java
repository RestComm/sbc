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

import org.mobicents.media.server.io.sdp.fields.AttributeField;

/**
 * a=crypto:tagId cryptoSuite inline: masterKey[|mkLifetime][|mkId]<br>
 * 
 * 
 * <p>
 * a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:lCsm/zoxoNH1N8OAXdStXIH2edrVE8JNQMZEPxhT
 * a=crypto:2 AES_CM_128_HMAC_SHA1_32 inline:x6BPdOlszfQGtUAmrDcyC0Y9rN0BQ6SAdeC5lI0C
 * </p>
 * 
 * @author Oscar Carriles (ocarriles@eolos.la)
 * @see <a href="http://tools.ietf.org/html/rfc4568">RFC4568</a> 
 */
public class CryptoAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "crypto";
	
	public static final String INLINE = "inline";
	
	
	
	private short  tagId;
	private String cryptoSuite;
	private String masterKey;
	private String mkLifetime;
	private String mkId;
	

	public CryptoAttribute() {
		super(ATTRIBUTE_TYPE);
	}
	public CryptoAttribute(short tagId, String cryptoSuite, String masterKey) {
		super(ATTRIBUTE_TYPE);
		this.tagId = tagId;
		this.cryptoSuite = cryptoSuite;
		this.masterKey = masterKey;
		
	}

	@Override
	public String toString() {
		// Clear the builder
		super.builder.setLength(0);
		
		// Build the candidate string - mandatory fields first
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR)
				.append(this.tagId).append(" ")
				.append(this.cryptoSuite).append(" ")
				.append(INLINE).append(":")
				.append(this.masterKey);
		if(this.mkLifetime!=null) {
			super.builder.append("|")
			.append(this.mkLifetime);
		}
		if(this.mkId!=null) {
			super.builder.append("|")
			.append(this.mkId);
		}
		
		return super.builder.toString();
	}

	public short getTagId() {
		return tagId;
	}

	public void setTagId(short tagId) {
		this.tagId = tagId;
	}


	public String getCryptoSuite() {
		return cryptoSuite;
	}


	public void setCryptoSuite(String cryptoSuite) {
		this.cryptoSuite = cryptoSuite;
	}


	public String getMasterKey() {
		return masterKey;
	}


	public void setMasterKey(String masterKey) {
		this.masterKey = masterKey;
	}


	public String getMkLifetime() {
		return mkLifetime;
	}


	public void setMkLifetime(String mkLifetime) {
		this.mkLifetime = mkLifetime;
	}


	public String getMkId() {
		return mkId;
	}


	public void setMkId(String mkId) {
		this.mkId = mkId;
	}
	
}
