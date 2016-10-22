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



import org.apache.log4j.Logger;

// RFC 3550

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 oct. 2016 10:04:35
 * @class   RTPParser.java
 *
 */
public class RTPParser {
	
	private static transient Logger LOG = Logger.getLogger(RTPParser.class);


    public RTPParser() {
        
    }

    public RTPPacket decode(byte[] packet) {
        if (packet.length < 12) {
            LOG.error("RTP packet too short");
            return null;
        }
        RTPPacket rtpPacket = new RTPPacket();
        int b = (int)(packet[0] & 0xff);
        rtpPacket.setVersion((b & 0xc0) >> 6);
        rtpPacket.setPadding((b & 0x20) != 0);
        rtpPacket.setExtension((b & 0x10) != 0);
        rtpPacket.setCsrcCount(b & 0x0f);
        b = (int)(packet[1] & 0xff);
        rtpPacket.setMarker((b & 0x80) != 0);
        rtpPacket.setPayloadType(b & 0x7f);
        b = (int)(packet[2] & 0xff);
        rtpPacket.setSequenceNumber(b * 256 + (int)(packet[3] & 0xff));
        b = (int)(packet[4] & 0xff);
        rtpPacket.setTimestamp(b * 256 * 256 * 256
                + (int)(packet[5] & 0xff) * 256 * 256
                + (int)(packet[6] & 0xff) * 256
                + (int)(packet[7] & 0xff));
        b = (int)(packet[8] & 0xff);
        rtpPacket.setSsrc(b * 256 * 256 * 256
                + (int)(packet[9] & 0xff) * 256 * 256
                + (int)(packet[10] & 0xff) * 256
                + (int)(packet[11] & 0xff));
        long[] csrcList = new long[rtpPacket.getCsrcCount()];
        for (int i = 0; i < csrcList.length; ++i)
            csrcList[i] = (int)(packet[12 + i] & 0xff) << 24
                + (int)(packet[12 + i + 1] & 0xff) << 16
                + (int)(packet[12 + i + 2] & 0xff) << 8
                + (int)(packet[12 + i + 3] & 0xff);
        rtpPacket.setCsrcList(csrcList);
        int dataOffset = 12 + csrcList.length * 4;
        int dataLength = packet.length - dataOffset;
        byte[] data = new byte[dataLength];
        System.arraycopy(packet, dataOffset, data, 0, dataLength);
        rtpPacket.setData(data);
        return rtpPacket;
    }

    public byte[] encode(RTPPacket rtpPacket) {
    	byte aByte = 0;
    	byte[] data = rtpPacket.getData();
        int packetLength = 12 + rtpPacket.getCsrcCount() * 4 + data.length;
        byte[] packet = new byte[packetLength];
        
        aByte |=(rtpPacket.getVersion() << 6);
        aByte |=((rtpPacket.isPadding() ? 1 : 0) << 5);
        aByte |=((rtpPacket.isExtension() ? 1 : 0) << 4);
        aByte |=(rtpPacket.getCsrcCount());
        packet[0] = aByte;
        aByte = 0;
        aByte |=((rtpPacket.isMarker() ? 1 : 0) << 7);
        aByte |= rtpPacket.getPayloadType();
        packet[1] = aByte;
        byte[] someBytes = Utils.uIntIntToByteWord(rtpPacket.getSequenceNumber());
        packet[2] = someBytes[0];
        packet[3] = someBytes[1];
        
       
        someBytes = Utils.uIntLongToByteWord(rtpPacket.getTimestamp());
        for(int i=0;i<4;i++) {
            packet[i + 4] = someBytes[i];
        }
       
        someBytes = Utils.uIntLongToByteWord(rtpPacket.getSsrc());
        System.arraycopy(someBytes, 0, packet, 8, 4);
        
        long[] csrcArray = rtpPacket.getCsrcList();
        
        for(int i=0; i<rtpPacket.getCsrcCount() ; i++) {
            someBytes = Utils.uIntLongToByteWord(csrcArray[i]);
            System.arraycopy(someBytes, 0, packet, 12 + 4*i, 4);
        }
        
        System.arraycopy(data, 0, packet, 12 + rtpPacket.getCsrcCount() * 4,
                data.length);
        
        return packet;
    }

}
