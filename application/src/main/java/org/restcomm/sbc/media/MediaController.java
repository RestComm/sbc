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

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.mobicents.media.io.ice.IceAuthenticatorImpl;
import org.mobicents.media.io.ice.IceComponent;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.io.sdp.SdpException;
import org.mobicents.media.server.io.sdp.SessionDescription;
import org.mobicents.media.server.io.sdp.SessionDescriptionParser;
import org.mobicents.media.server.io.sdp.attributes.RtpMapAttribute;
import org.mobicents.media.server.io.sdp.attributes.SsrcAttribute;
import org.mobicents.media.server.io.sdp.dtls.attributes.FingerprintAttribute;
import org.mobicents.media.server.io.sdp.dtls.attributes.SetupAttribute;
import org.mobicents.media.server.io.sdp.fields.ConnectionField;
import org.mobicents.media.server.io.sdp.fields.MediaDescriptionField;
import org.mobicents.media.server.io.sdp.fields.OriginField;
import org.mobicents.media.server.io.sdp.fields.SessionNameField;
import org.mobicents.media.server.io.sdp.format.AVProfile;
import org.mobicents.media.server.io.sdp.format.RTPFormat;
import org.mobicents.media.server.io.sdp.format.RTPFormats;
import org.mobicents.media.server.io.sdp.ice.attributes.CandidateAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IcePwdAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IceLiteAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IceUfragAttribute;
import org.mobicents.media.server.io.sdp.rtcp.attributes.RtcpAttribute;
import org.mobicents.media.server.io.sdp.rtcp.attributes.RtcpMuxAttribute;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.media.MediaZone.Direction;
import org.restcomm.sbc.media.dtls.DtlsConfiguration;
import org.restcomm.sbc.media.dtls.DtlsSrtpServer;
import org.restcomm.sbc.media.dtls.DtlsSrtpServerProvider;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    2 dic. 2016 9:22:42
 * @class   MediaController.java
 *
 */
public class MediaController  {
	
	private static transient Logger LOG = Logger.getLogger(MediaController.class);
	
	
	public static final String MEDIATYPE_AUDIO   = "audio";
	public static final String MEDIATYPE_VIDEO   = "video";
	
	static String[] supportedMediaTypes = { 
			MEDIATYPE_AUDIO,
			MEDIATYPE_VIDEO		
	};
	
	private SessionDescription sdp ;
	private SessionDescription webrtcSdp ;
	private MediaZone.Direction direction;
	private MediaSession mediaSession;
	private StreamProfile streamProfile;
	private IceAuthenticatorImpl iceAuthenticator;
	private static DtlsSrtpServer server;
	
	
	private ConcurrentHashMap<String, MediaZone> mediaZones=new ConcurrentHashMap<String, MediaZone>();

	public SessionDescription getSdp() {
		return sdp;
	}

    public MediaController(MediaSession session, StreamProfile streamProfile, MediaZone.Direction  direction, String sdpText, String targetProxyAddress) throws SdpException, UnknownHostException {
    	this.mediaSession=session;
    	this.sdp=SessionDescriptionParser.parse(sdpText);
    	this.direction=direction;
    	this.streamProfile=streamProfile;
    	buildMediaZones(targetProxyAddress);
		
    }
    
    public static DtlsSrtpServer getDTLSServer() {
    	if(server==null) {
    		server=createDtlServer();
    	}
    	return server;
    }
    
    public String toPrint() {
    	return "[MediaController MS:"+mediaSession.getSessionId()+" ("+direction+")]";
    }
    
    
    private void buildMediaZones (String targetProxyAddress)
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
    			
    			/* Do not create disabled Mediazones */
    			if(rtpPort==0) {
    				LOG.warn("Disabled media type "+supportedMediaTypes[type]);
    				continue;
    			}
    			
    			
    			if(sdp.getConnection()!=null) {
    				ip=sdp.getConnection().getAddress();
    			}
    			else {
    				ip=mediaDescription.getConnection().getAddress();
    			}
    			
    			switch(streamProfile) {
	    			case WEBRTC:
	    				if(ConfigurationCache.isMediaDecryptionEnabled()) {
	    					// DTLS termination
		    				if(supportedMediaTypes[type].equals(MEDIATYPE_AUDIO)) {
		    					mediaZone=new CryptoMediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);
		    				}
		    				else {
		    					// Not supported yet
		    					LOG.warn("Unsupported media type "+supportedMediaTypes[type]);
		    					continue;
		    				}
	    				}
	    				else {
	    					// DTLS pass thru
	    					mediaZone=new MediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);	
	    				}
	    				break;
	    			case AVP:
	    			case SAVP:
	    				mediaZone=new MediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);	
	    				break;
	    			default:
	    				mediaZone=new MediaZone(this, direction, supportedMediaTypes[type], ip, rtpPort, rtcpPort, canMux, mediaSession.proxyPorts[type]);	
	    				break;
    			}
    			
    			if(LOG.isTraceEnabled()) {
        			LOG.trace("Setting proxy on MediaZone target "+targetProxyAddress);	
        		}
    			
    			
    			try {
					mediaZone.setLocalProxy(targetProxyAddress);
				} catch (SocketException e) {
					LOG.error("Setting proxy on MediaZone target "+targetProxyAddress, e);	
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
    		if(peerZone==null) {
        		LOG.error("PeerZone not found for "+zone.toPrint());
        		continue;
    		}
    		if(LOG.isTraceEnabled()) {
    			LOG.trace("Attaching "+zone.toPrint());
    			LOG.trace("with      "+peerZone.toPrint());
    		}
    		zone.attach(peerZone);
    	}   	
    }
   
    public MediaZone checkStreaming()  {	
    	for(MediaZone zone:mediaZones.values()) {
    		if(!zone.isStreaming())
    			return zone;
    	}
    	return null;
    	
    }
    
    public synchronized void finalize()  {
    	if(LOG.isTraceEnabled()) {
			LOG.trace("Finalizing "+this.toPrint());	
		}
    	for(MediaZone zone:mediaZones.values()) {
    		finalize(zone);
    	}
    	mediaZones.clear();
    	
    }
    
    public synchronized void finalize(MediaZone zone)  {
    	zone.finalize();	
    	mediaZones.remove(zone.getMediaType());
    	
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
    	return patchIPAddressAndPort(StreamProfile.AVP, sdp.toString(), proxyHost);
    }
    
    public String getAVPProxySdp(String proxyHost) throws SdpException {  	
    	return patchIPAddressAndPort(StreamProfile.AVP, sdp.toString(), proxyHost);
    }
    
    public String getWebrtcSdp(String proxyHost) throws SdpException {
    	if(getWebrtcSdp()!=null)
    		return this.getWebrtcSdp().toString().trim().concat("\n");
    	String wrtcSdp=patchIPAddressAndPort(StreamProfile.AVP, sdp.toString(), proxyHost);
    	wrtcSdp = this.buildWebRtcFromSdp(wrtcSdp, proxyHost);
    	return wrtcSdp;
    }
    
    public String getAVPSdp() throws SdpException { 	
    	return patchIPAddressAndPort(StreamProfile.AVP, sdp.toString(), null);
    }
    
    
    public MediaController getOtherParty() {
    	if(this.direction==Direction.ANSWER) {
    		return mediaSession.getOffer();
    	}
    	else {
    		return mediaSession.getAnswer();
    	}
    }
    
    
    
    private String buildWebRtcFromSdp(String wsdp, String proxyHost) throws SdpException {	
    	SessionDescription psdp = SessionDescriptionParser.parse(wsdp);
   		OriginField origin = psdp.getOrigin();
   	
   		SessionNameField sessionName=new SessionNameField("SBC Call WebRTC");
   		
		psdp.setSessionName(sessionName);
   		
   		ConnectionField connection = new ConnectionField();
   		IceLiteAttribute iceLite = new IceLiteAttribute();
   		psdp.setIceLite(iceLite);
   		
   		
   		
   			
	   		MediaDescriptionField mediaDescription = psdp.getMediaDescription(MediaController.MEDIATYPE_AUDIO);
	   		
	   		
	   		if(mediaDescription!=null) {
	   			MediaZone zone=this.getMediaZone(MediaController.MEDIATYPE_AUDIO);
	   			if(zone instanceof MediaZone) {
	   				MediaChannel audioChannel = zone.getRtpConnection().getAudioChannel();
	   				if (!audioChannel.isOpen()) {
	   					// setup audio channel
	   					audioChannel.open();
	   				}
	   				String hashFunction="sha-256";
	   				if(LOG.isTraceEnabled()) {
	   					LOG.trace("> generateFingerPrint("+hashFunction+")");
	   				}
	   				try {
	   				 

	   		        DtlsSrtpServer server = getDTLSServer();
	   		        
	   				String fingerprint=server.generateFingerprint(hashFunction);
	   				iceAuthenticator = (IceAuthenticatorImpl) audioChannel.getIceAuthenticator();
	   				iceAuthenticator.generateIceCredentials();
	   				
		   			mediaDescription.setProtocol("UDP/TLS/RTP/SAVPF");
		   			mediaDescription.setConnection(psdp.getConnection());
		   			SsrcAttribute ssrc = new SsrcAttribute(Long.toString(audioChannel.getSsrc()));
		   			ssrc.addAttribute("cname", audioChannel.getCname());
		   			
		   			IceUfragAttribute ufrag = new IceUfragAttribute();
		   			IcePwdAttribute pwd = new IcePwdAttribute();
		   			
		   			FingerprintAttribute fprint = new FingerprintAttribute();
		   			SetupAttribute setup = new SetupAttribute("actpass");
		   			RtcpMuxAttribute rtcpMux = new RtcpMuxAttribute();
		   			RtcpAttribute rtcp = new RtcpAttribute();
		   			rtcp.setNetworkType("IN");
		   			rtcp.setAddressType("IP4");
		   			rtcp.setAddress(proxyHost);
		   			rtcp.setPort(mediaDescription.getPort());
		   			
		   			CandidateAttribute rtpCandidate = new CandidateAttribute();
		   			rtpCandidate.setFoundation("11111111");
		   			rtpCandidate.setComponentId(IceComponent.RTP_ID);
		   			rtpCandidate.setAddress(proxyHost);
		   			rtpCandidate.setPort(mediaDescription.getPort());
		   			rtpCandidate.setProtocol("udp");
		   			rtpCandidate.setPriority(1L);
		   			rtpCandidate.setCandidateType("host");
		   			mediaDescription.addCandidate(rtpCandidate);
		   			
		   			CandidateAttribute rtcpCandidate = new CandidateAttribute();
		   			rtcpCandidate.setFoundation("11111111");
		   			rtcpCandidate.setComponentId(IceComponent.RTP_ID);
			   
		   			rtcpCandidate.setAddress(proxyHost);
		   			rtcpCandidate.setRelatedAddress(proxyHost);
		   			rtcpCandidate.setPort(9);
		   			rtcpCandidate.setRelatedPort(mediaDescription.getRtcpPort());
		   			rtcpCandidate.setProtocol("tcp");
		   			rtcpCandidate.setPriority(1L);
		   			rtcpCandidate.setCandidateType("host");
		   			rtcpCandidate.setTcpType("active");
		   			mediaDescription.addCandidate(rtcpCandidate);
		   			
		   			fprint.setFingerprint(fingerprint.split(" ")[1]);
		   			fprint.setHashFunction(hashFunction);
		   			
		   			ufrag.setUfrag(iceAuthenticator.getUfrag());
		   			pwd.setPassword(iceAuthenticator.getPassword());
		   			
		   			mediaDescription.setSsrc(ssrc);
		   			mediaDescription.setRtcp(rtcp);
		   			mediaDescription.setRtcpMux(rtcpMux);
		   			mediaDescription.setIceUfrag(ufrag);
		   			mediaDescription.setIcePwd(pwd); 
					mediaDescription.setFingerprint(fprint);  
					mediaDescription.setSetup(setup); 
					
					mediaDescription = psdp.getMediaDescription(MediaController.MEDIATYPE_VIDEO);
					if(mediaDescription!=null) {
						mediaDescription.setProtocol("UDP/TLS/RTP/SAVPF");
						mediaDescription.setConnection(psdp.getConnection());
						mediaDescription.setIceUfrag(ufrag);
			   			mediaDescription.setIcePwd(pwd); 
						mediaDescription.setFingerprint(fprint);  
						mediaDescription.setSetup(setup); 
					}
					
	   			} catch(Exception e) {
	   				LOG.error(e.getMessage());
	   				zone.fireProxyFailedEvent();
	   			}
	   			}
	   			else {
	   				zone.fireProxyFailedEvent();
	   			}
	   			
	   		
   		}
   		return psdp.toString().trim().concat("\n");
    	
    	
    }
    
    private String patchIPAddressAndPort(StreamProfile streamProfile, String sdp2Patch, String ip) throws SdpException  {
   		SessionDescription psdp;
		
		psdp = SessionDescriptionParser.parse(sdp2Patch);
	
   		OriginField origin = psdp.getOrigin();
   		
   		if(ip!=null)
   			origin.setAddress(ip);
   		psdp.setOrigin(origin);
   		
   		SessionNameField sessionName=new SessionNameField("SBC Call "+streamProfile.text);
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
	   			switch(streamProfile) {
	   			case AVP:
	   			case SAVP:
	   				mediaDescription.removeAllCandidates();
		   			mediaDescription.setProtocol("RTP/AVP");
		   			break;
	   			case WEBRTC:
	   				mediaDescription.setProtocol("RTP/SAVPF");
	   				break;
	   			default:
	   				mediaDescription.removeAllCandidates();
		   			mediaDescription.setProtocol("RTP/AVP");
		   			break;
	   				
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
  
   
   
   public SessionDescription getWebrtcSdp() {
		return webrtcSdp;
		
   }
   
   private static  DtlsSrtpServer createDtlServer()  {
	    
   	//Dtls Server Provider
		   
	    DtlsConfiguration configuration = new DtlsConfiguration();
	    
	    DtlsSrtpServerProvider dtlsServerProvider = null;
	    
	    
	        dtlsServerProvider = 
	        		new DtlsSrtpServerProvider(	configuration.getMinVersion(),
	        									configuration.getMaxVersion(),
	        									configuration.getCipherSuites(),
	        									configuration.getCertificatePath(), //System.getProperty("user.home")+"/certs/x509-server-ecdsa.cert.pem",
	        									configuration.getKeyPath(), //System.getProperty("user.home")+"/certs/x509-server-ecdsa.private.pem",
	        									configuration.getAlgorithmCertificate());
	        
	       
	        DtlsSrtpServer server = dtlsServerProvider.provide();
	   
   	return server;  
   	
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
	
	public enum StreamProfile {
	        WEBRTC("WEBRTC"), AVP("AVP"), SAVP("SAVP");

	        private final String text;

	        private StreamProfile(final String text) {
	            this.text = text;
	        }

	        public static StreamProfile getValueOf(final String text) {
	        	StreamProfile[] values = values();
	            for (final StreamProfile value : values) {
	                if (value.toString().equals(text)) {
	                    return value;
	                }
	            }
	            throw new IllegalArgumentException(text + " is not a valid StreamProfile.");
	        }

	        @Override
	        public String toString() {
	            return text;
	        }
	};
   
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
	   /* webrtc offer 
	    * 
-------------------------------------
INVITE sip:21@10.0.0.10:56494;transport=WSS SIP/2.0
Max-Forwards: 69
From: "Oscar Carriles" <sip:2002@201.216.233.187>;tag=66002690_fbe427c7_8d4b6fda_abcbd919
CSeq: 102 INVITE
User-Agent: eomCM-2.6
Date: Thu, 02 Mar 2017 22:33:21 GMT
Allow: INVITE,ACK,CANCEL,OPTIONS,BYE,REFER,SUBSCRIBE,NOTIFY,INFO
Supported: replaces,timer
Call-ID: 382e21ee565c731389414d3b0ada363a@10.0.0.10
Via: SIP/2.0/UDP 10.0.0.10:5060;branch=z9hG4bKabcbd919_8d4b6fda_5728237f-dbfa-4a28-bfd7-4bfbf236725a
Contact: <sip:2002@10.0.0.10:5060;transport=wss>;transport=WSS
To: <sip:21@10.0.0.10:56494>;transport=wss
Content-Type: application/sdp
Content-Length: 571

v=0
o=root 601762045 601762045 IN IP4 201.216.233.187
s=SBC Call WebRTC
c=IN IP4 201.216.233.187
t=0 0
m=audio 17648 UDP/TLS/RTP/SAVPF 8 9 0 101
c=IN IP4 201.216.233.187
a=sendrecv
a=ptime:20
a=ice-ufrag:6fpiv
a=ice-pwd:468l6hhon24rqlsm18l7kc3hd
a=rtpmap:0 PCMU/8000
a=rtpmap:101 telephone-event/8000
a=rtpmap:8 PCMA/8000
a=rtpmap:9 G722/8000
a=setup:passive
a=fingerprint:sha-256 82:1E:5E:EB:B5:0D:F8:CF:7A:72:43:FE:91:3A:CE:DC:20:D1:4E:F4:69:4B:06:B4:AA:03:41:67:19:F1:E5:24
m=video 14522 UDP/TLS/RTP/SAVPF 99
c=IN IP4 201.216.233.187
a=sendrecv
a=rtpmap:99 H264/90000
-------------------------------------------------------------------------------
INVITE sip:2002@10.0.0.10 SIP/2.0
CSeq: 2 INVITE
User-Agent: TelScale RestComm SBC Web Client 1.0.0 BETA4
Allow: INVITE,ACK,CANCEL,BYE
Contact: <sip:21@05EbNlrXVEvC.invalid;transport=wss>
Call-ID: 1488493839075
Via: SIP/2.0/WSS 05EbNlrXVEvC.invalid;branch=z9hG4bK-333330-98f6239a22124baa7aa9a5a604a57752;rport
From: "21" <sip:21@10.0.0.10>;tag=1488493839324
To: <sip:2002@10.0.0.10>
Max-Forwards: 70
Content-Type: application/sdp
Authorization: Digest username="21",realm="telecom",nonce="7a06b367",response="6ca41502fcd4ea29bef0a2e7c8c93329",uri="sip:2002@10.0.0.10",algorithm=MD5
Content-Length: 3874

v=0
o=- 679921508637561316 2 IN IP4 127.0.0.1
s=-
t=0 0
a=group:BUNDLE audio video
a=msid-semantic: WMS RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz
m=audio 61222 UDP/TLS/RTP/SAVPF 111 103 104 9 0 8 106 105 13 126
c=IN IP4 10.0.0.10
a=rtcp:61224 IN IP4 10.0.0.10
a=candidate:2162125114 1 udp 2122260223 10.0.0.10 61222 typ host generation 0 network-id 2
a=candidate:2162125114 2 udp 2122260222 10.0.0.10 61224 typ host generation 0 network-id 2
a=candidate:3462174154 1 tcp 1518280447 10.0.0.10 9 typ host tcptype active generation 0 network-id 2
a=candidate:3462174154 2 tcp 1518280446 10.0.0.10 9 typ host tcptype active generation 0 network-id 2
a=ice-ufrag:+sbR
a=ice-pwd:5uD9jGHkHohSpFAve3b89tzp
a=fingerprint:sha-256 6B:3F:BC:31:C6:04:32:4B:8A:04:E1:D9:88:E1:2E:D9:63:90:F2:5B:73:75:B0:77:88:47:E4:8E:C9:D9:F6:2C
a=setup:actpass
a=mid:audio
a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level
a=sendrecv
a=rtcp-mux
a=rtpmap:111 opus/48000/2
a=rtcp-fb:111 transport-cc
a=fmtp:111 minptime=10;useinbandfec=1
a=rtpmap:103 ISAC/16000
a=rtpmap:104 ISAC/32000
a=rtpmap:9 G722/8000
a=rtpmap:0 PCMU/8000
a=rtpmap:8 PCMA/8000
a=rtpmap:106 CN/32000
a=rtpmap:105 CN/16000
a=rtpmap:13 CN/8000
a=rtpmap:126 telephone-event/8000
a=ssrc:370255539 cname:+5+1CAuLB07Avifx
a=ssrc:370255539 msid:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz eee089cb-fbd8-4add-80c2-8c32b7582265
a=ssrc:370255539 mslabel:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz
a=ssrc:370255539 label:eee089cb-fbd8-4add-80c2-8c32b7582265
m=video 61226 UDP/TLS/RTP/SAVPF 100 101 107 116 117 96 97 99 98
c=IN IP4 10.0.0.10
a=rtcp:61228 IN IP4 10.0.0.10
a=candidate:2162125114 1 udp 2122260223 10.0.0.10 61226 typ host generation 0 network-id 2
a=candidate:2162125114 2 udp 2122260222 10.0.0.10 61228 typ host generation 0 network-id 2
a=candidate:3462174154 1 tcp 1518280447 10.0.0.10 9 typ host tcptype active generation 0 network-id 2
a=candidate:3462174154 2 tcp 1518280446 10.0.0.10 9 typ host tcptype active generation 0 network-id 2
a=ice-ufrag:+sbR
a=ice-pwd:5uD9jGHkHohSpFAve3b89tzp
a=fingerprint:sha-256 6B:3F:BC:31:C6:04:32:4B:8A:04:E1:D9:88:E1:2E:D9:63:90:F2:5B:73:75:B0:77:88:47:E4:8E:C9:D9:F6:2C
a=setup:actpass
a=mid:video
a=extmap:2 urn:ietf:params:rtp-hdrext:toffset
a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time
a=extmap:4 urn:3gpp:video-orientation
a=extmap:5 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01
a=extmap:6 http://www.webrtc.org/experiments/rtp-hdrext/playout-delay
a=sendrecv
a=rtcp-mux
a=rtcp-rsize
a=rtpmap:100 VP8/90000
a=rtcp-fb:100 ccm fir
a=rtcp-fb:100 nack
a=rtcp-fb:100 nack pli
a=rtcp-fb:100 goog-remb
a=rtcp-fb:100 transport-cc
a=rtpmap:101 VP9/90000
a=rtcp-fb:101 ccm fir
a=rtcp-fb:101 nack
a=rtcp-fb:101 nack pli
a=rtcp-fb:101 goog-remb
a=rtcp-fb:101 transport-cc
a=rtpmap:107 H264/90000
a=rtcp-fb:107 ccm fir
a=rtcp-fb:107 nack
a=rtcp-fb:107 nack pli
a=rtcp-fb:107 goog-remb
a=rtcp-fb:107 transport-cc
a=fmtp:107 level-asymmetry-allowed=1;packetization-mode=1;profile-level-id=42e01f
a=rtpmap:116 red/90000
a=rtpmap:117 ulpfec/90000
a=rtpmap:96 rtx/90000
a=fmtp:96 apt=100
a=rtpmap:97 rtx/90000
a=fmtp:97 apt=101
a=rtpmap:99 rtx/90000
a=fmtp:99 apt=107
a=rtpmap:98 rtx/90000
a=fmtp:98 apt=116
a=ssrc-group:FID 1212614237 709767935
a=ssrc:1212614237 cname:+5+1CAuLB07Avifx
a=ssrc:1212614237 msid:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz 37205f02-f32f-4625-be06-061c2c837db7
a=ssrc:1212614237 mslabel:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz
a=ssrc:1212614237 label:37205f02-f32f-4625-be06-061c2c837db7
a=ssrc:709767935 cname:+5+1CAuLB07Avifx
a=ssrc:709767935 msid:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz 37205f02-f32f-4625-be06-061c2c837db7
a=ssrc:709767935 mslabel:RMRG2tPidvcXFdrV38z0EpLJOACVgOWdBpdz
a=ssrc:709767935 label:37205f02-f32f-4625-be06-061c2c837db7

	    */
	   
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
			MediaController offer=session.buildOffer(StreamProfile.AVP, sdpOffer,"192.168.12.10");
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
			MediaController answer = session.buildAnswer(StreamProfile.AVP,sdpAnswer,"192.168.12.10");
			
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
			
			String unsecure = offer.getAVPSdp();
			
			System.out.println("---------------unsecure-------------------");
			System.out.println(unsecure);
			
			String upatched   = offer.getAVPProxySdp("201.216.233.187");
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

public void setWebrtcSdp(SessionDescription secureSdp) {
	this.webrtcSdp = secureSdp;
}


 
}
