package org.restcomm.sbc.media;


import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mobicents.media.server.io.sdp.fields.MediaDescriptionField;
import org.restcomm.sbc.media.helpers.ExtendedMediaDescriptionField;
import org.restcomm.sbc.media.helpers.ExtendedSessionDescription;
import org.restcomm.sbc.media.helpers.SessionDescriptionParser;

/**
 * @author <a href="mailto:ocarriles@eolos.la">ocarriles</a>
 */

public class SDPTest {
    private final static Logger LOG = Logger.getLogger(SDPTest.class.getName());
   

	@Test
    public void testSDP() {
		Path resourceDirectory = Paths.get("src","test","resources");
		//System.out.println("Path "+resourceDirectory.toString());
 		try {
 			MediaSession session=new MediaSession("ID");
 			
 			TextReader  reader = new TextReader(resourceDirectory+"/savp-offer.sdp");
 			String sdpOffer=reader.getContent();

 			ExtendedSessionDescription sdpo = SessionDescriptionParser.parse(sdpOffer);
 			ExtendedMediaDescriptionField audio = sdpo.getExtendedMediaDescription("audio");
 			
 			MediaController offer=session.buildOffer(sdpo,"127.0.0.1");
 			
 			reader = new TextReader(resourceDirectory+"/savp-answer.sdp");
 			String sdpAnswer=reader.getContent();
 			
 			ExtendedSessionDescription sdpa = SessionDescriptionParser.parse(sdpAnswer);
 			MediaController answer = session.buildAnswer(sdpa,"192.168.88.3");
 			
 			//offer.setLocalProxy("192.168.88.3");
 			
 			String sdpContent=offer.getProxySdp("1.1.1.1");		
 			
 			session.attach();
 			//answer.setLocalProxy("127.0.0.1");	
 			
 			sdpContent=answer.getSAVPSdp("2.2.2.2");		
 			LOG.info("----->"+sdpContent);
 			LOG.info("----->"+sdpa);
 			LOG.info(answer.getMediaZone("audio").toPrint());
 			//LOG.info(answer.getMediaZone("video").toPrint());
 			LOG.info(offer.getMediaZone("audio").toPrint());
 			//LOG.info(offer.getMediaZone("video").toPrint());
 			
 			LOG.info("---------------original-------------------");
 			//System.out.println(metadata.getSdp());
 			//System.out.println(metadata.mediaType+", "+metadata.getProtocol()+", "+metadata.getIp()+":"+metadata.getRtpPort());
 			LOG.info(offer.getSdp().toString());
 			
 			String unsecure = offer.getAVPSdp();
 			
 			LOG.info("---------------unsecure-------------------");
 			LOG.info(unsecure);
 			
 			String secure = offer.getSAVPSdp();
 			
 			LOG.info("---------------unsecure-------------------");
 			LOG.info(unsecure);
 			
 			
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
    
}
