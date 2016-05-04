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
 * @author Oscar Andres Carriles <ocarriles@eolos.la>.
 *******************************************************************************/
/*
 * Created on 13/07/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.restcomm.sbc.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;




/**
 * @author LOCO
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MD5Digest {
	
	
	

	public String getEolixMD5KeyWithNoEncoder(String text, String key) {
		String md5Key;
		md5Key = new String(getKeyedDigest(text.getBytes(), key.getBytes()));
		return md5Key;
	}

	private byte[] getKeyedDigest(byte[] data, byte[] key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(data);
            return md5.digest(key);
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }
	public static String getMD5Digest(String data) {
        try {
        	
        	return MD5.md5crypt(data);
           
            
        } catch (Exception e) {
        }
        return "";
    }
	public static void main(String argv[]){
		String ha1 = MD5Digest.getMD5Digest("003:telecom:Ja10881137");
		String ha2 = MD5Digest.getMD5Digest("REGISTER:sip:192.168.88.3");
		String ha3 = MD5Digest.getMD5Digest(ha1+":3ccc1ef1:"+ha2);
		System.err.println("ha1=user:realm:password->"+ha1);
		System.err.println("ha2=req_method:req_uri-->"+ha2);
		System.err.println("ha3=ha1:nonce:ha2------->"+ha3);
		
	}
}
