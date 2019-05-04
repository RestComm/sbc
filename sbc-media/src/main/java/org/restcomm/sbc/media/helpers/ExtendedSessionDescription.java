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

import java.util.HashMap;
import java.util.Map;

import org.mobicents.media.server.io.sdp.SdpField;
import org.mobicents.media.server.io.sdp.SessionDescription;


/**
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class ExtendedSessionDescription extends SessionDescription {
	
	private static final String NEWLINE = "\n";
	private final StringBuilder builder;
	
	

	// Media Descriptions
	private final Map<String, ExtendedMediaDescriptionField> mediaMap;
	   
	public ExtendedSessionDescription() {
		super();
		this.builder = new StringBuilder();
		this.mediaMap = new HashMap<String, ExtendedMediaDescriptionField>(5);
	}
	
	

	public ExtendedMediaDescriptionField getExtendedMediaDescription(String mediaType) {
		return this.mediaMap.get(mediaType);
	}
	
	public boolean containsMediaDescription(String mediaType) {
		return this.mediaMap.containsKey(mediaType);
	}
	
	public void addMediaDescription(ExtendedMediaDescriptionField media) {
		this.mediaMap.put(media.getMedia(), media);
	}
	
	
	
	@Override
	public String toString() {
		this.builder.setLength(0);
		append(super.getVersion());
		append(super.getOrigin());
		append(super.getSessionName());
		append(super.getConnection());
		append(super.getTiming());
		append(super.getIceLite());
		append(super.getIceUfrag());
		append(super.getIcePwd());
		append(super.getFingerprint());
		append(super.getSetup());
		
		for (ExtendedMediaDescriptionField media : this.mediaMap.values()) {
			this.builder.append(media.toString()).append(NEWLINE);
		}
		this.builder.deleteCharAt(this.builder.length() - 1);
		return this.builder.toString();
	}
	
	private void append(SdpField field) {
		if(field != null) {
			this.builder.append(field.toString()).append(NEWLINE);
		}
	}
}
