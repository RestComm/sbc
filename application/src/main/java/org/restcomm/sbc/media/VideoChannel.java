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

package org.restcomm.sbc.media;

import org.apache.log4j.Logger;


/**
 * Media channel responsible for audio processing.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class VideoChannel extends MediaChannel {
	private static final Logger logger = Logger.getLogger(VideoChannel.class);
	
	public static final String MEDIA_TYPE = "video";

	public VideoChannel(String originalHost, int originalPort) {
		super(MEDIA_TYPE, originalHost, originalPort);
		if(logger.isTraceEnabled()) {
			logger.trace("Creating VideoChannel on "+originalHost+":"+originalPort);
		}
		
	}

	
	
}
