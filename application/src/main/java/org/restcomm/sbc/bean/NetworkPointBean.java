package org.restcomm.sbc.bean;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;



import org.restcomm.sbc.bo.NetworkPoint;

import static java.lang.System.out;

@ManagedBean(name = "ifs")
@SessionScoped
public class NetworkPointBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static int id=0;

	private static ArrayList<NetworkPoint> eths;
	
	static {
		eths = new ArrayList<NetworkPoint>();
		try {
			init();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public List<NetworkPoint> getNetworkPoints() {
		return eths;
	}

	public static void init() throws SocketException {
	        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	        int group=0;
	        for (NetworkInterface netIf : Collections.list(nets)) {
	        	String mac=makeMAC(netIf.getHardwareAddress());
	        	if("".equals(mac.trim())) {
	        		continue;
	        	}
	        	List<InterfaceAddress> inetAddresses =  netIf.getInterfaceAddresses();
	 	       
		        for (InterfaceAddress inetAddress : inetAddresses) {
		        	NetworkPoint point=new NetworkPoint();
		        	point.setId(id);
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
	         
	        }
	       
	    }

	    static void displaySubInterfaces(NetworkInterface netIf) throws SocketException {
	        Enumeration<NetworkInterface> subIfs = netIf.getSubInterfaces();
	        
	        for (NetworkInterface subIf : Collections.list(subIfs)) {
	            out.printf("\tSub Interface Display name: %s\n", subIf.getDisplayName());
	            out.printf("\tSub Interface Name: %s\n", subIf.getName());
	        }
	     }
	    
	    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
	        out.printf("Display name: %s\n", netint.getDisplayName());
	        out.printf("Name: %s\n", netint.getName());
	        List<InterfaceAddress> inetAddresses =  netint.getInterfaceAddresses();
	       
	        for (InterfaceAddress inetAddress : inetAddresses) {
	            out.printf("InetAddress: %s/%d\n", inetAddress.getAddress().getHostAddress(),inetAddress.getNetworkPrefixLength());
	        }
	        out.printf("\n");
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
	  

}