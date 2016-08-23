package org.restcomm.sbc.managers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.NetworkPoint;


import static java.lang.System.out;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 16:36:38
 * @class   NetworkManager.java
 *
 */
public class NetworkManager  {
	
	private static transient Logger LOG = Logger.getLogger(NetworkManager.class);
	private static int id=0;

	private static ArrayList<NetworkPoint> eths;
	private static ArrayList<NetworkPoint> tots;
	
	static {
		eths = new ArrayList<NetworkPoint>();
		tots = new ArrayList<NetworkPoint>();
		try {
			init();
		} catch (SocketException e) {
			LOG.error("Can't Obtain Interface data", e);
		}
		
	}
	
	public static List<NetworkPoint> getNetworkPoints() {
		return eths;
	}
	
	public static List<NetworkPoint> mergeNetworkPoints(List<NetworkPoint> persistents) {
		tots = new ArrayList<NetworkPoint>(eths);
		for(NetworkPoint realPoint:eths) {
			for(NetworkPoint persistentPoint:persistents) {
				if(persistentPoint.getId().equals(realPoint.getId())) {
					persistentPoint.setMacAddress(realPoint.getMacAddress());
					persistentPoint.setDescription(realPoint.getDescription());
					tots.remove(realPoint);
					tots.add(persistentPoint);		
				}
			}
			
		}
		
		return tots;
		
	}
	
	public static NetworkPoint getNetworkPoint(String id) {
		for(NetworkPoint point:eths) {
			if(point.getId().equals(id)) {
				return point;
			}
		}
		return null;
	}
	
	public static NetworkPoint getNetworkPointByIpAddress(String ipAddress) {
		for(NetworkPoint point:eths) {
			if(point.getAddress().getHostAddress().equals(ipAddress)) {
				return point;
			}
		}
		return null;
	}
	
	public static boolean exists(String id) {
		for(NetworkPoint point:eths) {
			if(point.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static void init() throws SocketException {
	        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	        int group=0;
	        for (NetworkInterface netIf : Collections.list(nets)) {
	        	String mac=makeMAC(netIf.getHardwareAddress());
	        	if("".equals(mac.trim())) {
	        		continue;
	        	}
	        	List<InterfaceAddress> inetAddresses =  netIf.getInterfaceAddresses();
	 	       
		        for (InterfaceAddress inetAddress : inetAddresses) {
		        	NetworkPoint point=new NetworkPoint(netIf.getName()+"-"+id);
		        	
		        	point.setGroup(group);
		        	point.setName(netIf.getName());
		        	point.setDescription(netIf.getDisplayName());
		        	point.setMacAddress(mac);
		        	point.setAddress(inetAddress.getAddress());
		        	point.setBroadcast(inetAddress.getBroadcast());
		        	point.setPrefixMask(inetAddress.getNetworkPrefixLength());
		        	id++;
		            eths.add(point);
		        }
		        group++;
		        id=0;
	         
	        }
	       
	    }
	
	    private static String makeMAC(byte[] mac) {
	    	StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
	    	if(mac==null) {
	    		return "";
	    	}
	    		
			for(int i=0;i<mac.length;i++){
				out.format("%02X", mac[i]);
			}
			return writer.toString();
	    }
	    
	    public static void main(String argv[]) {
	    	for(NetworkPoint point:NetworkManager.getNetworkPoints()) {
	    		out.println(point.toPrint());
	    	}
	    }
	  

}