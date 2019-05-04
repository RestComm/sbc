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

import java.util.ArrayList;

import java.util.List;

import org.mobicents.media.server.io.sdp.SdpField;
import org.mobicents.media.server.io.sdp.SessionLevelAccessor;
import org.mobicents.media.server.io.sdp.attributes.ConnectionModeAttribute;
import org.mobicents.media.server.io.sdp.attributes.MaxPacketTimeAttribute;
import org.mobicents.media.server.io.sdp.attributes.PacketTimeAttribute;
import org.mobicents.media.server.io.sdp.attributes.RtpMapAttribute;
import org.mobicents.media.server.io.sdp.attributes.SsrcAttribute;
import org.mobicents.media.server.io.sdp.dtls.attributes.FingerprintAttribute;
import org.mobicents.media.server.io.sdp.dtls.attributes.SetupAttribute;
import org.mobicents.media.server.io.sdp.fields.ConnectionField;
import org.mobicents.media.server.io.sdp.fields.MediaDescriptionField;
import org.mobicents.media.server.io.sdp.ice.attributes.CandidateAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IcePwdAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IceUfragAttribute;
import org.mobicents.media.server.io.sdp.rtcp.attributes.RtcpAttribute;
import org.mobicents.media.server.io.sdp.rtcp.attributes.RtcpMuxAttribute;


/**
 * m=[media] [port] [proto] [fmt]
 * 
 * <p>
 * A session description may contain a number of media descriptions.<br>
 * Each media description starts with an "m=" field and is terminated by either
 * the next "m=" field or by the end of the session description.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class ExtendedMediaDescriptionField  {
	private static final String NEWLINE = "\n";
	public static final char FIELD_TYPE = 'm';
	private StringBuilder builder;
	
	//Crypto attributes
	private List<CryptoAttribute> cryptos;
	private MediaDescriptionField field;

	
	public ExtendedMediaDescriptionField(final SessionLevelAccessor field) {
		
			this.field = new MediaDescriptionField(field);
		
	}
	public ExtendedMediaDescriptionField(MediaDescriptionField media) {
		this.field = media;
	}
	
	
	public ExtendedMediaDescriptionField() {
		this.field = new MediaDescriptionField();
	}



	public CryptoAttribute[] getCryptos() {
		if(this.cryptos == null || this.cryptos.isEmpty()) {
			return null;
		}
		return cryptos.toArray(new CryptoAttribute[this.cryptos.size()]);
	}
	
	public MediaDescriptionField getMediaDescriptionField() {
		return field;
	}
	
	public boolean containsCryptos() {
		return this.cryptos != null && !this.cryptos.isEmpty();
	}
	
	
	public void addCrypto(CryptoAttribute crypto) {
		if(this.cryptos == null) {
			this.cryptos = new ArrayList<CryptoAttribute>(8);
			this.cryptos.add(crypto);
		} else if(!this.cryptos.contains(crypto)) {
			this.cryptos.add(crypto);
		}
	}
	
	
	
	public void removeCrypto(CryptoAttribute crypto) {
		if(this.cryptos != null) {
			this.cryptos.remove(crypto);
		}
	}
	
	
	
	public void removeAllCryptos() {
		if(this.cryptos != null) {
			this.cryptos.clear();
		}
	}
	
	

	@Override
	public String toString() {
		// Clean builder
		this.builder=new StringBuilder(this.field.toString());
		
		
		if (this.cryptos != null && !this.cryptos.isEmpty()) {
			for (CryptoAttribute crypto : this.cryptos) {
				appendField(crypto);
			}
		}

		return this.builder.toString();
	}
	
	private void appendField(SdpField field) {
		if(field != null) {
			this.builder.append(NEWLINE).append(field.toString());
		}
	}



	public void setProtocol(String protocol) {
		this.field.setProtocol(protocol);
		
	}



	public void setConnection(ConnectionField connection) {
		this.field.setConnection(connection);
		
	}



	public int getPort() {
		return this.field.getPort();
	}



	public void setSsrc(SsrcAttribute ssrc) {
		this.field.setSsrc(ssrc);
		// TODO Auto-generated method stub
		
	}



	public void setRtcp(RtcpAttribute rtcp) {
		this.field.setRtcp(rtcp);
		
	}



	public void setRtcpMux(RtcpMuxAttribute rtcpMux) {
		this.field.setRtcpMux(rtcpMux);
		
	}



	public void addCandidate(CandidateAttribute candidate) {
		this.field.addCandidate(candidate);
		
	}



	public int getRtcpPort() {
		return field.getRtcpPort();
	}



	public void setIceUfrag(IceUfragAttribute ufrag) {
		this.field.setIceUfrag(ufrag);
		
	}



	public void setIcePwd(IcePwdAttribute pwd) {
		this.field.setIcePwd(pwd);
		
	}



	public void setFingerprint(FingerprintAttribute fprint) {
		this.field.setFingerprint(fprint);
		
	}



	public void setSetup(SetupAttribute setup) {
		this.field.setSetup(setup);
		
	}



	public RtpMapAttribute[] getFormats() {
		return this.field.getFormats();
	}



	public void removeAllCandidates() {
		if(this.field!=null && this.field.containsCandidates())
			this.field.removeAllCandidates();
		
	}



	public RtcpAttribute getRtcp() {
		return this.field.getRtcp();
	}



	public void setPort(int port) {
		this.field.setPort(port);
		
	}



	public String getMedia() {
		return field.getMedia();
	}



	public SsrcAttribute getSsrc() {
		return field.getSsrc();
	}



	public void setConnectionMode(ConnectionModeAttribute connectionMode) {
		field.setConnectionMode(connectionMode);
		
	}



	public void setMaxptime(MaxPacketTimeAttribute maxptime) {
		this.field.setMaxptime(maxptime);
		
	}



	public void setPtime(PacketTimeAttribute ptime) {
		this.field.setPtime(ptime);
		
	}



	public void addFormat(RtpMapAttribute format) {
		this.field.addFormat(format);
		
	}



	public void setSession(ExtendedSessionDescription sdp) {
		this.field.setSession(sdp);
		
	}



	public boolean isRtcpMux() {
		return field.isRtcpMux();
	}



	public ConnectionField getConnection() {
		return field.getConnection();
	}



	public String getProtocol() {
		return field.getProtocol();
	}



	public boolean containsIce() {
		return field.containsIce();
	}



	public void setMedia(String media) {
		field.setMedia(media);
		
	}



	public int[] getPayloadTypes() {
		return field.getPayloadTypes();
	}



	public void setPayloadTypes(int[] payloadTypes) {
		field.setPayloadTypes(payloadTypes);
		
	}



	public void addPayloadType(int payloadType) {
		field.addPayloadType(payloadType);
		
	}



	public FingerprintAttribute getFingerprint() {
		return field.getFingerprint();
	}
	

}
