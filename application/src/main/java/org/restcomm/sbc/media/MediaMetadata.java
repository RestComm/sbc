package org.restcomm.sbc.media;

import java.net.UnknownHostException;
import java.util.ArrayList;
import org.mobicents.media.io.ice.IceAuthenticatorImpl;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.io.sdp.SdpException;
import org.mobicents.media.server.io.sdp.SessionDescription;
import org.mobicents.media.server.io.sdp.SessionDescriptionParser;
import org.mobicents.media.server.io.sdp.dtls.attributes.FingerprintAttribute;
import org.mobicents.media.server.io.sdp.dtls.attributes.SetupAttribute;
import org.mobicents.media.server.io.sdp.fields.ConnectionField;
import org.mobicents.media.server.io.sdp.fields.MediaDescriptionField;
import org.mobicents.media.server.io.sdp.fields.OriginField;
import org.mobicents.media.server.io.sdp.fields.SessionNameField;
import org.mobicents.media.server.io.sdp.ice.attributes.IcePwdAttribute;
import org.mobicents.media.server.io.sdp.ice.attributes.IceUfragAttribute;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    31 oct. 2016 13:48:51
 * @class   MediaMetadata.java
 *
 */
public class MediaMetadata {
	
	private SessionDescription sdp ;
	
	private String ip;
	private String mediaType;
	private String protocol;
	private int rtpPort;
	private int rtcpPort;
	private String fingerprint;
	private String fingerAlgorithm;
	private boolean canMux;
	
	public static final String MEDIATYPE_AUDIO   = "audio";
	public static final String MEDIATYPE_VIDEO   = "video";
	public static final String MEDIATYPE_MESSAGE = "message";
	
	
	// a map for each Mediatype (audio, video, etc)
   
	private ArrayList<Crypto> cryptos=new ArrayList<Crypto>();

	
	private MediaMetadata(String mediaType, String text) throws SdpException  {
		this.mediaType=mediaType;
		this.sdp=SessionDescriptionParser.parse(text);
		MediaDescriptionField mediaDescription = sdp.getMediaDescription(mediaType);
		this.protocol=mediaDescription.getProtocol();
		this.rtpPort=mediaDescription.getPort();
		this.rtcpPort=mediaDescription.getRtcpPort();
		this.canMux=mediaDescription.isRtcpMux();
		if(sdp.getConnection()!=null) {
			this.ip=sdp.getConnection().getAddress();
		}
		else {
			this.ip=mediaDescription.getConnection().getAddress();
		}
		
		FingerprintAttribute fp = mediaDescription.getFingerprint();
   		if(fp!=null) {		
   			setFingerprint(fp.getFingerprint());
   			setFingerAlgorithm(fp.getHashFunction());
		
   		}
		
	}

	public boolean isSecure() {
		if(protocol!=null) {
			if(protocol.equalsIgnoreCase("RTP/AVP"))
				return false;
			else
				return true;
		}
		return false;
	}
	
	
	
	public CipherSuite[] getCipherSuites() {
		ArrayList<CipherSuite> ciphers=new ArrayList<CipherSuite>();
		CipherSuite[] suites = new CipherSuite[10];
		
		for(Crypto crypto: cryptos) {
			ciphers.add(crypto.getCryptoSuite());
		}
		
		return ciphers.toArray(suites);
	}
	
	public ArrayList<Crypto> getCryptos() {
		return cryptos;
	}
	public void setCryptos(ArrayList<Crypto> cryptos) {
		this.cryptos = cryptos;
	}
	
	public void addCrypto(String line) {
		this.cryptos.add(new Crypto(line));
	}
	

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	

	public void setSdp(SessionDescription sdp) {
		this.sdp = sdp;
	}
	
	public String toString() {
		String result;
		CipherSuite[] suites = this.getCipherSuites();
		result="MediaMetadata [";
			this.getCipherSuites();
				for(int i=0;i<cryptos.size();i++) {
					result+="\n"+cryptos.get(i)+":"+suites[i];
				}
				
				result+="\n{media="+mediaType+", IP="+getIp()+", RTPport="+rtpPort+", RTCPport="+rtcpPort+", Can Mux RTCP? "+canMux+"}";
				result+=", protocol="+protocol+", secure="+isSecure();
				if(isSecure()) {
					result+=", fingerprint="+fingerAlgorithm+":"+fingerprint;
				}
				return result;
	}
	
	
	public SessionDescription getSdp() {
		return sdp;
	}

    public static  MediaMetadata build(String mediaType, String text)
            throws UnknownHostException, SdpException {
    		
       MediaMetadata metadata=new MediaMetadata(mediaType, text);
     
       return metadata;
    }
    
    public String patch(String sdp) throws SdpException {

   		SessionDescription psdp = SessionDescriptionParser.parse(sdp);
   		
   		OriginField origin = psdp.getOrigin();
   		origin.setAddress(ip);
   		
   		SessionNameField sessionName=new SessionNameField("SBC Call");
		psdp.setSessionName(sessionName);
   		
   		ConnectionField connection = new ConnectionField();
   		
   		connection.setAddress(ip);
   		if(psdp.getConnection()!=null)
   			psdp.setConnection(connection);
   		
   		MediaDescriptionField mediaDescription = psdp.getMediaDescription(mediaType);
   		mediaDescription.setConnection(connection);
   		mediaDescription.setPort(this.getRtpPort());
   		
   		return psdp.toString().trim().concat("\n");
    	
    }
    
   
   
   public SessionDescription unSecureSdp() throws UnknownHostException, SdpException {
	   
	   		SessionDescription usdp = SessionDescriptionParser.parse(sdp.toString());
			
	   		
	   		MediaDescriptionField mediaDescription = usdp.getMediaDescription(mediaType);
	   		mediaDescription.setProtocol("RTP/AVP");
	   		mediaDescription.removeAllCandidates();
	   	
	   		
			return usdp;
	                  
	     
   }
   
   
   public SessionDescription secureSdp() throws UnknownHostException, SdpException {
	    SessionDescription ssdp = SessionDescriptionParser.parse(sdp.toString());
	    
	    IceAuthenticatorImpl auth = new IceAuthenticatorImpl();
	    auth.generateIceCredentials();
	    
		MediaDescriptionField mediaDescription = ssdp.getMediaDescription(mediaType);
   		mediaDescription.setProtocol("RTP/SAVPF");
   		
   		FingerprintAttribute fp;
   		
   		fp=mediaDescription.getFingerprint();
   		if(fp==null)
   			fp=new FingerprintAttribute();
   		fp.setFingerprint("E6:CE:47:0E:64:5D:EF:9B:08:B3:34:D1:72:3E:46:48:BD:6E:62:47");
   		fp.setHashFunction("sha-1");
		mediaDescription.setFingerprint(fp);
		
		IceUfragAttribute ice_ufrag;
		IcePwdAttribute ice_pwd;
		
		ice_ufrag=mediaDescription.getIceUfrag();
		ice_pwd=mediaDescription.getIcePwd();
		
		if(ice_ufrag==null) {
			ice_ufrag = new IceUfragAttribute();
			ice_pwd = new IcePwdAttribute();
		}
		ice_ufrag.setUfrag(auth.getUfrag());
		ice_pwd.setPassword(auth.getPassword());
		
		mediaDescription.setIcePwd(ice_pwd);
		mediaDescription.setIceUfrag(ice_ufrag);
		
		SetupAttribute setup=new SetupAttribute("passive");
		mediaDescription.setSetup(setup);
		
		
	
      return ssdp;         
    
   }
   
   public static void main(String argv[]) {
	   /*
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
					*/
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
		MediaMetadata metadata;
		try {
			metadata = MediaMetadata.build(MediaMetadata.MEDIATYPE_AUDIO, sdpText);
			
			System.out.println("---------------original-------------------");
			System.out.println(metadata.getSdp());
			System.out.println(metadata);
			
			String unsecure = metadata.patch(metadata.unSecureSdp().toString());
			System.out.println("---------------unsecure-------------------");
			System.out.println(unsecure);
			System.out.println(metadata);
			
			metadata.setIp("201.216.233.187");
			metadata.setRtpPort(50000);
			metadata.setRtcpPort(60000);
			
			String secure   = metadata.patch(metadata.secureSdp().toString());
			System.out.println("---------------secure---------------------");
			System.out.println(secure);
			
			System.out.println("---------------original-------------------");
			System.out.println(metadata.getSdp());
			System.out.println(metadata);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public String getMediaType() {
		return mediaType;
	}
	

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getRtpPort() {
		return rtpPort;
	}

	public void setRtpPort(int rtpPort) {
		this.rtpPort = rtpPort;
	}

	public int getRtcpPort() {
		return rtcpPort;
	}

	public void setRtcpPort(int rtcpPort) {
		this.rtcpPort = rtcpPort;
	}

	public String getFingerAlgorithm() {
		return fingerAlgorithm;
	}

	public void setFingerAlgorithm(String fingerAlgorithm) {
		this.fingerAlgorithm = fingerAlgorithm;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public void setRtcpMultiplexed(boolean mux) {
		canMux=mux;
	}

	public boolean isRtcpMultiplexed() {
		if(rtcpPort<=0)
			return true;
		return canMux;
	}

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
