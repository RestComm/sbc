package org.restcomm.sbc.media;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.sdp.Attribute;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;

import javax.sdp.SessionDescription;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    31 oct. 2016 13:48:51
 * @class   MediaMetadata.java
 *
 */
public class MediaMetadata {
	/*
	 * 	v=0
		o=11 8000 8000 IN IP4 192.168.88.3
		s=SBC Call
		c=IN IP4 192.168.88.3
		t=0 0
		m=audio 10002 RTP/SAVP 18 4 3 0 8
		a=sendrecv
		a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:XK2+f3dMxqq9yfhYNSO3cwSnFACD+/h5xnXG15iQ
		a=crypto:2 AES_CM_128_HMAC_SHA1_32 inline:Fqpm95oH83bu61+saLnKi4NY0kzJ1fhwQS/DfxCz
		a=rtpmap:18 G729/8000
		a=rtpmap:4 G723/8000
		a=rtpmap:3 GSM/8000
		a=rtpmap:0 PCMU/8000
		a=rtpmap:8 PCMA/8000
		a=ptime:20
	 */
	
	
	private String protocol;
	private String ip;
	
	// a map for each Mediatype (audio, video, etc)
	private HashMap<String, Integer> portsMap=new HashMap<String, Integer>();
	// a map for each Mediatype (audio, video, etc)
	private HashMap<String, Fingerprint> fingerprintsMap=new HashMap<String, Fingerprint>();
	private ArrayList<Crypto> cryptos=new ArrayList<Crypto>();
	
	
	public MediaMetadata() {
		
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
	
	public ArrayList<Crypto> getCryptos() {
		return cryptos;
	}
	public void setCryptos(ArrayList<Crypto> cryptos) {
		this.cryptos = cryptos;
	}
	
	public void addCrypto(String line) {
		this.cryptos.add(new Crypto(line));
	}
	
	public void setPort(String mediaType, int port) {
		portsMap.put(mediaType, port);
	}
	
	public void setFingerprint(String mediaType, String line) {
		fingerprintsMap.put(mediaType, new Fingerprint(line));
	}
	
	public HashMap<String, Integer> getPortsMap() {
		return portsMap;
	}
	public void setPortsMap(HashMap<String, Integer> portsMap) {
		this.portsMap = portsMap;
	}
	
	public int getPort(String mediaType) {
		return portsMap.get(mediaType);
		
	}
	
	public Fingerprint getFingerprint(String mediaType) {
		return fingerprintsMap.get(mediaType);
		
	}

	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	public String toString() {
		String result;
		result="MediaMetadata [";
				for(int i=0;i<cryptos.size();i++) {
					result+="\n"+cryptos.get(i);
				}
				for(Entry<String, Integer> ports:portsMap.entrySet()) {
					result+="\n{media="+ports.getKey()+", IP="+getIp()+", port="+ports.getValue()+", "+fingerprintsMap.get(ports.getKey())+"}";
				}
				result+=", protocol="+protocol+", secure="+isSecure();
				return result;
	}
	
	/*
     * Returns audio/video port
     */
    @SuppressWarnings("unchecked")
    public static  MediaMetadata build(String text)
            throws UnknownHostException, SdpException {
        MediaMetadata metadata=new MediaMetadata();
        
            final SessionDescription sdp = SdpFactory.getInstance().createSessionDescription(text);
         // Handle the connections at the media description level.
            final Vector<MediaDescription> descriptions = sdp.getMediaDescriptions(false);
            for (final MediaDescription description : descriptions) {
            	
            	final Media media=description.getMedia();
            	
            		metadata.setPort(media.getMediaType(),media.getMediaPort());
            		
            		metadata.setProtocol(media.getProtocol());
            		Vector <Attribute>attrs = description.getAttributes(false);
            		for(Attribute attr:attrs){
            			if(attr.getName().equalsIgnoreCase("crypto")){
            				metadata.addCrypto(attr.getValue());
            				//System.out.println("name "+attr.getName()+" value "+attr.getValue());
            			}
            			if(attr.getName().equalsIgnoreCase("fingerprint")){
            				metadata.setFingerprint(media.getMediaType(),attr.getValue());
            				//System.out.println("name "+attr.getName()+" value "+attr.getValue());
            			}
            			if(attr.getName().equalsIgnoreCase("setup")){
            				metadata.setFingerprint(media.getMediaType(),attr.getValue());
            				//System.out.println("name "+attr.getName()+" value "+attr.getValue());
            			}
            		}
            		
            	
            }
       
       return metadata;
    }
    
   public static String fix(final byte[] data, MediaMetadata newMetadata) throws UnknownHostException, SdpException {
	       
	        return SdpUtils.patch("application/sdp", data, newMetadata);
	                  
	     
   }

   public static void main(String argv[]) {
		String sdp="v=0\n"+
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
					"a=fingerprint:sha-1 E6:CE:47:0E:64:5D:EF:9B:08:B3:34:D1:72:3E:46:48:BD:6E:62:47";
	/*	String sdp="v=0\n"+
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
			metadata = MediaMetadata.build(sdp);
			metadata.setIp("201.216.233.187");
			metadata.setPort("audio", 50000);
			metadata.setPort("video", 60000);
			System.out.println(MediaMetadata.fix(sdp.getBytes(), metadata));
			System.out.println(metadata);
		} catch (UnknownHostException | SdpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


   }

	class Crypto {
				
		private int tag;
		private String cryptoSuite;
		private String keyParams;
		
		Crypto(String line) {
			String fields[]=line.split("inline:");
			keyParams=fields[1];
			String crypto[]=fields[0].split(" ");
			tag=Integer.parseInt(crypto[0]);
			cryptoSuite=crypto[1];
		}
		
		public int getTag() {
			return tag;
		}
		public void setTag(int tag) {
			this.tag = tag;
		}
		public String getCryptoSuite() {
			return cryptoSuite;
		}
		public void setCryptoSuite(String cryptoSuite) {
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
		
		class Fingerprint {
			
			private String algorithm;
			private String hash;
			private String setup;
			
			Fingerprint(String line) {
				String fprint[]=line.split(" ");
				algorithm=fprint[0];
				hash=fprint[1];
			}
			
				
			public String toString() {
				return "Fingerprint [algorithm="+algorithm+", hash="+hash+"]";
			}

			public String getAlgorithm() {
				return algorithm;
			}


			public void setAlgorithm(String algorithm) {
				this.algorithm = algorithm;
			}

			public String getHash() {
				return hash;
			}

			public void setHash(String hash) {
				this.hash = hash;
			}

			public String getSetup() {
				return setup;
			}

			public void setSetup(String setup) {
				this.setup = setup;
			}
		
	
		
		

}
