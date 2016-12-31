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

package org.restcomm.sbc.media;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.io.sdp.SdpException;
import org.mobicents.media.server.io.sdp.SessionDescription;
import org.mobicents.media.server.io.sdp.SessionDescriptionParser;
import org.mobicents.media.server.io.sdp.attributes.RtpMapAttribute;
import org.mobicents.media.server.io.sdp.fields.ConnectionField;
import org.mobicents.media.server.io.sdp.fields.MediaDescriptionField;
import org.mobicents.media.server.io.sdp.fields.OriginField;
import org.mobicents.media.server.io.sdp.fields.SessionNameField;
import org.mobicents.media.server.io.sdp.format.AVProfile;
import org.mobicents.media.server.io.sdp.format.RTPFormat;
import org.mobicents.media.server.io.sdp.format.RTPFormats;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.media.MediaZone.Direction;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    2 dic. 2016 9:22:42
 * @class   MediaController.java
 *
 */
public class MediaController {
	
	private static transient Logger LOG = Logger.getLogger(MediaController.class);
	
	
	public static final String MEDIATYPE_AUDIO   = "audio";
	public static final String MEDIATYPE_VIDEO   = "video";
	public static final String MEDIATYPE_MESSAGE = "message";
	
	static String[] supportedMediaTypes = { 
			MEDIATYPE_AUDIO,
			MEDIATYPE_VIDEO,
			MEDIATYPE_MESSAGE
	};
	
	private SessionDescription sdp ;
	private MediaZone.Direction direction;
	private MediaSession mediaSession;
	
	private HashMap<String, MediaZone> mediaZones=new HashMap<String, MediaZone>();


	private SessionDescription secureSdp;
	
	public SessionDescription getSdp() {
		return sdp;
	}

    public MediaController(MediaSession session, MediaZone.Direction  direction, String sdpText) throws SdpException, UnknownHostException {
    	this.mediaSession=session;
    	this.sdp=SessionDescriptionParser.parse(sdpText);
    	this.direction=direction;
    	buildMediaZones();
		
    }
    
    public String toPrint() {
    	return "[MediaController MS:"+mediaSession.getSessionId()+" ("+direction+")]";
    }
    
    
    private void buildMediaZones ()
            throws UnknownHostException, SdpException {
    	
    	for(int type=0;type<supportedMediaTypes.length;type++) {
    			MediaZone mediaZone;
    			String ip;
    			int rtpPort;
    			int rtcpPort;
    			boolean canMux;
    			
    			MediaDescriptionField mediaDescription = sdp.getMediaDescription(supportedMediaTypes[type]);
    			
    			if(mediaDescription==null) {
    				LOG.warn("No media type "+supportedMediaTypes[type]);
    				continue;
    			}
    			
    			canMux=mediaDescription.isRtcpMux();
    			rtpPort=mediaDescription.getPort();
    			rtcpPort=mediaDescription.getRtcpPort();
    			
    			// Do not create disabled Mediazones
    			if(rtpPort==0) {
    				continue;
    			}
    			
    			if(sdp.getConnection()!=null) {
    				ip=sdp.getConnection().getAddress();
    			}
    			else {
    				ip=mediaDescription.getConnection().getAddress();
    			}
    			if (mediaDescription.getProtocol().equals("RTP/AVP")) {
    				mediaZone=new MediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);	
    			}
    			else {
    				if(supportedMediaTypes[type].equals("audio")) {
    					mediaZone=new CryptoMediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);
    				}
    				else {
    					continue;
    				}
    			}
    	   		
    	   		mediaZones.put(supportedMediaTypes[type], mediaZone);
    	   		if(LOG.isTraceEnabled()) {
    	   			LOG.trace("Adding MediaZone "+mediaZone.toPrint());
    	   		}
    	}
    		
    }
    
    public boolean isOffer() {
    	return direction==MediaZone.Direction.OFFER;
    }
    
    public boolean isSecure(String mediaType) { 
    	if(sdp.getMediaDescription(mediaType).getProtocol().equals("RTP/AVP")){
    		return false;
    	}
    	return true;
    	
    }
    
    public boolean isSecure() { 
    	return isSecure(MEDIATYPE_AUDIO);
    	
    }
    /**
     * Attach peers of same mediatype and different direction
     * @param peerController
     */
    public void attach(MediaController peerController) {
    	
    	for(MediaZone zone:mediaZones.values()) {
    		MediaZone peerZone=peerController.getMediaZone(zone.getMediaType());
    			if(LOG.isTraceEnabled()) {
    				LOG.trace("Attaching "+zone.toPrint());
    				LOG.trace("with      "+peerZone.toPrint());
    			}
    			zone.attach(peerZone);
    	}   	
    }
    /*
    public void start() throws UnknownHostException {
    	if(LOG.isInfoEnabled()) {
			LOG.info("Starting "+this.toPrint());	
		}
    	for(MediaZone zone:mediaZones.values()) {
    			zone.start();
    	}
    	
    }
    */
    public MediaZone checkStreaming()  {	
    	for(MediaZone zone:mediaZones.values()) {
    		if(!zone.isStreaming())
    			return zone;
    	}
    	return null;
    	
    }
    
    public void finalize() throws IOException {
    	if(LOG.isInfoEnabled()) {
			LOG.info("Finalizing "+this.toPrint());	
		}
    	for(MediaZone zone:mediaZones.values()) {
    		zone.finalize();
    	}
    	
    }
    
    public MediaZone getMediaZone(String mediaType) {
    	return mediaZones.get(mediaType);
    }
    
    public void setLocalProxy(String proxyHost) throws UnknownHostException, SocketException {
    	for(MediaZone zone:mediaZones.values()) {
    		zone.setLocalProxy(proxyHost);
    	}
    	
    }
    
    public String getProxySdp(String proxyHost) throws SdpException {
    	return patchIPAddressAndPort(false, sdp.toString(), proxyHost);
    }
    
    public String getUnsecureProxySdp(String proxyHost) throws SdpException {
    	if(ConfigurationCache.isMediaDecodingEnabled())
    		return patchIPAddressAndPort(true, sdp.toString(), proxyHost);
    	return patchIPAddressAndPort(false, sdp.toString(), proxyHost);
    }
    
    public String getSecureProxySdp(String proxyHost) throws SdpException {
    	return this.getSecureSdp().toString().trim().concat("\n");
    }
    
    public String getUnsecureSdp() throws SdpException {
    	if(ConfigurationCache.isMediaDecodingEnabled())
    		return patchIPAddressAndPort(true, sdp.toString(), null);
    	return patchIPAddressAndPort(false, sdp.toString(), null);
    }
    
    
    public MediaController getOtherParty() {
    	if(this.direction==Direction.ANSWER) {
    		return mediaSession.getOffer();
    	}
    	else {
    		return mediaSession.getAnswer();
    	}
    }
    
    private String patchIPAddressAndPort(boolean unsecure, String sdp2Patch, String ip) throws SdpException  {
   		SessionDescription psdp;
		
		psdp = SessionDescriptionParser.parse(sdp2Patch);
	
   		OriginField origin = psdp.getOrigin();
   		
   		if(ip!=null)
   			origin.setAddress(ip);
   		psdp.setOrigin(origin);
   		
   		SessionNameField sessionName=new SessionNameField("SBC Call "+(unsecure?"unsecure ":"secure "));
		psdp.setSessionName(sessionName);
   		
   		ConnectionField connection = new ConnectionField();
   		if(ip!=null)
   			connection.setAddress(ip);
   		
   		if(psdp.getConnection()!=null && ip!=null)
   			psdp.setConnection(connection);
   		
   		for(int type=0;type<supportedMediaTypes.length;type++) {
   			MediaZone zone=mediaZones.get(supportedMediaTypes[type]);
   			if(zone==null) {
   				LOG.warn("skipping MediaZone "+supportedMediaTypes[type]);
   				continue;
   			}
	   		MediaDescriptionField mediaDescription = psdp.getMediaDescription(supportedMediaTypes[type]);
	   		
	   		if(mediaDescription!=null) {
	   			if(unsecure) {
		   			mediaDescription.removeAllCandidates();
		   			mediaDescription.setProtocol("RTP/AVP");
		   		}
	   			if(ip!=null)
	   				mediaDescription.setConnection(connection);
	   			if(zone.getProxyPort()==0){
	   				LOG.warn("Cannot Patch ProxyPort == 0 ");
	   			}
	   			// Offered mediaPorts == 0 MUST not be patched
	   			if(mediaDescription.getPort()!=0) {
	   				mediaDescription.setPort(zone.getProxyPort());
	   			}
	   			
	   			
	   		}
   		}
   		return psdp.toString().trim().concat("\n");
    	
    }
   
   public RTPFormats getNegociatedFormats() {
	// Media formats
	   RTPFormats rtpFormats=new RTPFormats();
	   
	   MediaDescriptionField description=sdp.getMediaDescription("audio");
	   RtpMapAttribute[] formats = description.getFormats();
		for (int index = 0; index < formats.length; index++) {
			RTPFormat f = AVProfile.audio.find(formats[index].getPayloadType());
			if(f!=null) {
				if(LOG.isTraceEnabled()) {
					LOG.trace("Adding mediaFormat "+f.toString());	
				}
				rtpFormats.add(f);
			}
		}
		/*
		description=sdp.getMediaDescription("video");
		formats = description.getFormats();
		for (int index = 0; index < formats.length; index++) {
			RTPFormat f = AVProfile.video.find(formats[index].getPayloadType());
			if(f!=null)
				rtpFormats.add(f);		
		}
		*/
		return rtpFormats;
	   
   }
  
   public void setSecureSdp(SessionDescription sd) {
		this.secureSdp=sd;
		
   }
   
   public SessionDescription getSecureSdp() {
		return this.secureSdp;
		
   }
   
   @Override
	public boolean equals(Object controller) {
		MediaController otherController=(MediaController) controller;
		if (!(controller instanceof MediaController)) {
			return false;
		}
		
		if (otherController.getSdp().equals(this.getSdp()) &&
			otherController.getMediaSession().equals(this.getMediaSession()) &&
			otherController.direction.equals(direction)) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((mediaSession == null) ? 0 : mediaSession.hashCode());
		result = prime * result + ((sdp == null) ? 0 : sdp.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return result;

	}
   
   class Crypto {
		
		private int tag;
		private CipherSuite cryptoSuite;
		private String keyParams;
		
		Crypto(String line) {
			String fields[]=line.split("inline:");
			keyParams=fields[1];
			String crypto[]=fields[0].split(" ");
			tag=Integer.parseInt(crypto[0]);
			//cryptoSuite=CipherSuite.valueOf(crypto[1]);
			cryptoSuite=CipherSuite.TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA;
		}
		
		public int getTag() {
			return tag;
		}
		public void setTag(int tag) {
			this.tag = tag;
		}
		public CipherSuite getCryptoSuite() {
			return cryptoSuite;
		}
		public void setCryptoSuite(CipherSuite cryptoSuite) {
			this.cryptoSuite = cryptoSuite;
		}
		public String getKeyParams() {
			return keyParams;
		}
		public void setKeyParams(String keyParams) {
			this.keyParams = keyParams;
		}
		
		public String toString() {
			return "Crypto [tag="+tag+", crypto-suite="+cryptoSuite+", key="+keyParams+"]";
		}
		
   }
   
   public static void main(String argv[]) {
	   
		String sdpText="v=0\n"+
					"o=12-jitsi.org 0 0 IN IP4 192.168.88.3\n"+
					"s=SBC Call\n"+
					"c=IN IP4 192.168.88.3\n"+
					"t=0 0\n"+
					"m=audio 5020 UDP/TLS/RTP/SAVP 96 97 98 9 100 102 0 8 103 3 104 4 101\n"+
					"a=rtpmap:96 opus/48000/2\n"+
					"a=fmtp:96 usedtx=1\n"+
					"a=rtpmap:97 SILK/24000\n"+
					"a=rtpmap:98 SILK/16000\n"+
					"a=rtpmap:9 G722/8000\n"+
					"a=rtpmap:100 speex/32000\n"+
					"a=rtpmap:102 speex/16000\n"+
					"a=rtpmap:0 PCMU/8000\n"+
					"a=rtpmap:8 PCMA/8000\n"+
					"a=rtpmap:103 iLBC/8000\n"+
					"a=rtpmap:3 GSM/8000\n"+
					"a=rtpmap:104 speex/8000\n"+
					"a=rtpmap:4 G723/8000\n"+
					"a=fmtp:4 annexa=no;bitrate=6.3\n"+
					"a=rtpmap:101 telephone-event/8000\n"+
					"a=extmap:1 urn:ietf:params:rtp-hdrext:csrc-audio-level\n"+
					"a=extmap:2 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n"+
					"a=rtcp-xr:voip-metrics\n"+
					"a=setup:actpass\n"+
					"a=fingerprint:sha-1 31:1A:85:0F:02:85:58:C4:1B:35:E9:BB:E2:D8:79:27:18:AD:E0:5F\n"+
					"m=audio 5020 RTP/SAVPF 96 97 98 9 100 102 0 8 103 3 104 4 101\n"+
					"a=rtpmap:96 opus/48000/2\n"+
					"a=fmtp:96 usedtx=1\n"+
					"a=rtpmap:97 SILK/24000\n"+
					"a=rtpmap:98 SILK/16000\n"+
					"a=rtpmap:9 G722/8000\n"+
					"a=rtpmap:100 speex/32000\n"+
					"a=rtpmap:102 speex/16000\n"+
					"a=rtpmap:0 PCMU/8000\n"+
					"a=rtpmap:8 PCMA/8000\n"+
					"a=rtpmap:103 iLBC/8000\n"+
					"a=rtpmap:3 GSM/8000\n"+
					"a=rtpmap:104 speex/8000\n"+
					"a=rtpmap:4 G723/8000\n"+
					"a=fmtp:4 annexa=no;bitrate=6.3\n"+
					"a=rtpmap:101 telephone-event/8000\n"+
					"a=extmap:1 urn:ietf:params:rtp-hdrext:csrc-audio-level\n"+
					"a=extmap:2 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n"+
					"a=rtcp-xr:voip-metrics\n"+
					"a=setup:actpass\n"+
					"a=fingerprint:sha-1 31:1A:85:0F:02:85:58:C4:1B:35:E9:BB:E2:D8:79:27:18:AD:E0:5F\n"+
					"m=video 5022 UDP/TLS/RTP/SAVP 105 99 106 107 108 109\n"+
					"a=recvonly\n"+
					"a=rtpmap:105 H264/90000\n"+
					"a=fmtp:105 profile-level-id=4DE01f;packetization-mode=1\n"+
					"a=imageattr:105 send * recv [x=[0-1680],y=[0-1050]]\n"+
					"a=rtpmap:99 H264/90000\n"+
					"a=fmtp:99 profile-level-id=4DE01f\n"+
					"a=imageattr:99 send * recv [x=[0-1680],y=[0-1050]]\n"+
					"a=rtpmap:106 H263-1998/90000\n"+
					"a=fmtp:106 CUSTOM=1680,1050,2;VGA=2;CIF=1;QCIF=1\n"+
					"a=rtpmap:107 red/90000\n"+
					"a=rtpmap:108 ulpfec/90000\n"+
					"a=rtpmap:109 VP8/90000\n"+
					"a=setup:actpass\n"+
					"a=fingerprint:sha-1 E6:CE:47:0E:64:5D:EF:9B:08:B3:34:D1:72:3E:46:48:BD:6E:62:47\n"+
					"m=video 5022 RTP/SAVPF 105 99 106 107 108 109\n"+
					"a=recvonly\n"+
					"a=rtpmap:105 H264/90000\n"+
					"a=fmtp:105 profile-level-id=4DE01f;packetization-mode=1\n"+
					"a=imageattr:105 send * recv [x=[0-1680],y=[0-1050]]\n"+
					"a=rtpmap:99 H264/90000\n"+
					"a=fmtp:99 profile-level-id=4DE01f\n"+
					"a=imageattr:99 send * recv [x=[0-1680],y=[0-1050]]\n"+
					"a=rtpmap:106 H263-1998/90000\n"+
					"a=fmtp:106 CUSTOM=1680,1050,2;VGA=2;CIF=1;QCIF=1\n"+
					"a=rtpmap:107 red/90000\n"+
					"a=rtpmap:108 ulpfec/90000\n"+
					"a=rtpmap:109 VP8/90000\n"+
					"a=setup:actpass\n"+
					// just for test
					//"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:XK2+f3dMxqq9yfhYNSO3cwSnFACD+/h5xnXG15iQ\n"+
					//"a=crypto:2 AES_CM_128_HMAC_SHA1_32 inline:Fqpm95oH83bu61+saLnKi4NY0kzJ1fhwQS/DfxCz\n"+
					//
					"a=fingerprint:sha-1 E6:CE:47:0E:64:5D:EF:9B:08:B3:34:D1:72:3E:46:48:BD:6E:62:47";
					/*
	   String sdpText="v=0\n"+
			   "o=- 188809950206000236 2 IN IP4 127.0.0.1\n"+
			   "s=-\n"+
			   "t=0 0\n"+
			   "a=group:BUNDLE audio\n"+
			   "a=msid-semantic: WMS QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN\n"+
		
			   "m=audio 54011 UDP/TLS/RTP/SAVPF 111 103 104 9 0 8 106 105 13 126\n"+
			   
			   "c=IN IP4 181.165.120.41\n\r"+
			   "a=rtcp:54013 IN IP4 181.165.120.41\n"+
			   "a=candidate:2162125114 1 udp 2122260223 10.0.0.10 54010 typ host generation 0 network-id 2\n"+
			   "a=candidate:4221882981 1 udp 2122194687 192.168.88.2 54011 typ host generation 0 network-id 1\n"+
			   "a=candidate:2162125114 2 udp 2122260222 10.0.0.10 54012 typ host generation 0 network-id 2\n"+
			   "a=candidate:4221882981 2 udp 2122194686 192.168.88.2 54013 typ host generation 0 network-id 1\n"+
			   "a=candidate:3462174154 1 tcp 1518280447 10.0.0.10 9 typ host tcptype active generation 0 network-id 2\n"+
			   "a=candidate:3039243925 1 tcp 1518214911 192.168.88.2 9 typ host tcptype active generation 0 network-id 1\n"+
			   "a=candidate:3462174154 2 tcp 1518280446 10.0.0.10 9 typ host tcptype active generation 0 network-id 2\n"+
			   "a=candidate:3039243925 2 tcp 1518214910 192.168.88.2 9 typ host tcptype active generation 0 network-id 1\n"+
			   "a=candidate:229944241 2 udp 1685987070 181.165.120.41 54013 typ srflx raddr 192.168.88.2 rport 54013 generation 0 network-id 1\n"+
			   "a=candidate:229944241 1 udp 1685987071 181.165.120.41 54011 typ srflx raddr 192.168.88.2 rport 54011 generation 0 network-id 1\n"+
			   "a=ice-ufrag:lPqG\n"+
			   "a=ice-pwd:1iy3iKIrMn88BMA+abWT8hbZ\n"+
			   "a=fingerprint:sha-256 32:B6:92:32:22:5D:A4:0F:C4:B3:18:B7:23:FB:DF:82:6A:0C:13:82:AA:3E:89:6F:C7:83:1F:60:C2:9B:5D:68\n"+
			   "a=setup:actpass\n"+
			   "a=mid:audio\n"+
			   "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n"+
			   "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n"+
			   "a=sendrecv\n"+
			   "a=rtcp-mux\n"+
			   "a=rtpmap:111 opus/48000/2\n"+
			   "a=rtcp-fb:111 transport-cc\n"+
			   "a=fmtp:111 minptime=10;useinbandfec=1\n"+
			   "a=rtpmap:103 ISAC/16000\n"+
			   "a=rtpmap:104 ISAC/32000\n"+
			   "a=rtpmap:9 G722/8000\n"+
			   "a=rtpmap:0 PCMU/8000\n"+
			   "a=rtpmap:8 PCMA/8000\n"+
			   "a=rtpmap:106 CN/32000\n"+
			   "a=rtpmap:105 CN/16000\n"+
			   "a=rtpmap:13 CN/8000\n"+
			   "a=rtpmap:126 telephone-event/8000\n"+
			   "a=ssrc:819597054 cname:bHyZrjk3sM4Psqgb\n"+
			   "a=ssrc:819597054 msid:QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN 3a3383c1-c87e-4739-9e94-19992cc433ff\n"+
			   "a=ssrc:819597054 mslabel:QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN\n"+
			   "a=ssrc:819597054 label:3a3383c1-c87e-4739-9e94-19992cc433ff";
	  /*
		String sdpText="v=0\n"+
				"o=root 10050031 10050031 IN IP4 192.168.0.2\n"+
				"s=eolosCM-2.6\n"+
				"c=IN IP4 192.168.0.2\n"+
				"t=0 0\n"+
				"m=audio 17398 RTP/AVP 8\n"+
				"a=rtpmap:8 PCMA/8000\n"+
				"a=ptime:20\n"+
				"a=sendrecv\n"+
				"m=video 0 RTP/AVP 105 99 106 107 108 109\n";	
				
	/*	String sdpText="v=0\n"+
				"o=11 8000 8000 IN IP4 192.168.88.3\n"+
				"s=SBC Call\n"+
				"c=IN IP4 192.168.88.3\n"+
				"t=0 0\n"+
				"m=audio 10002 RTP/SAVP 18 4 3 0 8\n"+
				"a=sendrecv\n"+
				"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:XK2+f3dMxqq9yfhYNSO3cwSnFACD+/h5xnXG15iQ\n"+
				"a=crypto:2 AES_CM_128_HMAC_SHA1_32 inline:Fqpm95oH83bu61+saLnKi4NY0kzJ1fhwQS/DfxCz\n"+
				"a=rtpmap:18 G729/8000\n"+
				"a=rtpmap:4 G723/8000\n"+
				"a=rtpmap:3 GSM/8000\n"+
				"a=rtpmap:0 PCMU/8000\n"+
				"a=rtpmap:8 PCMA/8000\n"+
				"a=ptime:20";
				*/
		
		try {
			MediaSession session=new MediaSession("ID");
			String sdpOffer=
"v=0\n"+ 
"o=22-jitsi.org 0 0 IN IP4 10.0.0.10\n"+ 
"s=- \n"+
"c=IN IP4 10.0.0.10\n"+
"t=0 0\n"+
"m=audio 5082 RTP/AVP 8 96 97 98 9 100 102 103 3 104 101\n"+
"a=rtpmap:8 PCMA/8000\n"+
"a=rtpmap:96 opus/48000/2\n"+
"a=fmtp:96 usedtx=1\n"+
"a=rtpmap:97 SILK/24000\n"+
"a=rtpmap:98 SILK/16000\n"+
"a=rtpmap:9 G722/8000\n"+
"a=rtpmap:100 speex/32000\n"+
"a=rtpmap:102 speex/16000\n"+
"a=rtpmap:103 iLBC/8000\n"+
"a=rtpmap:3 GSM/8000\n"+
"a=rtpmap:104 speex/8000\n"+
"a=rtpmap:101 telephone-event/8000\n"+
"a=extmap:1 urn:ietf:params:rtp-hdrext:csrc-audio-level\n"+
"a=extmap:2 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n"+
"a=rtcp-xr:voip-metrics\n"+
"m=video 5084 RTP/AVP 105 99 106\n"+
"a=rtpmap:105 H264/90000\n"+
"a=fmtp:105 profile-level-id=4DE01f;packetization-mode=1\n"+
"a=imageattr:105 send * recv [x=[0-1680],y=[0-1050]]\n"+
"a=rtpmap:99 H264/90000\n"+
"a=fmtp:99 profile-level-id=4DE01f\n"+
"a=imageattr:99 send * recv [x=[0-1680],y=[0-1050]]\n"+
"a=rtpmap:106 H263-1998/90000\n"+
"a=fmtp:106 CUSTOM=1680,1050,2;VGA=2;CIF=1;QCIF=1\n";
			MediaController offer=session.buildOffer(sdpOffer);
			String sdpAnswer=

"v=0\n"+
"o=root 1373078659 1373078659 IN IP4 192.168.120.96\n"+
"s=eolosCM-2.6\n"+
"c=IN IP4 192.168.120.96\n"+
"b=CT:384\n"+
"t=0 0\n"+
"m=audio 17252 RTP/AVP 8 3\n"+
"a=rtpmap:8 PCMA/8000\n"+
"a=rtpmap:3 GSM/8000\n"+
"a=ptime:20\n"+
"a=sendrecv\n"+
"m=video 14906 RTP/AVP 106 99\n"+
"a=rtpmap:106 h263-1998/90000\n"+
"a=rtpmap:99 H264/90000\n"+
"a=sendrecv\n";
			MediaController answer = session.buildAnswer(sdpAnswer);
			
			offer.setLocalProxy("127.0.0.1");
			
			String sdpContent=offer.getProxySdp("1.1.1.1");		
			
			session.attach();
			answer.setLocalProxy("192.168.88.3");	
			
			sdpContent=answer.getProxySdp("2.2.2.2");		
			
			System.out.println(answer.getMediaZone("audio").toPrint());
			System.out.println(answer.getMediaZone("video").toPrint());
			System.out.println(offer.getMediaZone("audio").toPrint());
			System.out.println(offer.getMediaZone("video").toPrint());
			
			System.out.println("---------------original-------------------");
			//System.out.println(metadata.getSdp());
			//System.out.println(metadata.mediaType+", "+metadata.getProtocol()+", "+metadata.getIp()+":"+metadata.getRtpPort());
			System.out.println(offer.getSdp().toString());
			
			String unsecure = offer.getUnsecureSdp();
			
			System.out.println("---------------unsecure-------------------");
			System.out.println(unsecure);
			
			String upatched   = offer.getUnsecureProxySdp("201.216.233.187");
			System.out.println("---------------unsecure patched---------------------");
			System.out.println(upatched);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

public MediaSession getMediaSession() {
	return mediaSession;
}


   
  
}
