package org.restcomm.sbc.bo;

import java.net.Inet6Address;
import java.net.InetAddress;

import org.mobicents.servlet.sip.restcomm.util.IPUtils;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 15:44:49
 * @class   NetworkPoint.java
 *
 */
public class NetworkPoint {
	
	private String id;
	private int group;
	private String name;
	private String description;
	private InetAddress address;
	private String macAddress;
	private InetAddress broadcast;
	private short prefixMask;
	private Tag tag;
	private Sid accountSid;
	
	public NetworkPoint(String id) {
		this.id = id;
		this.tag=Tag.UNINITIALIZED;
	}
	
	public NetworkPoint(final String id, final Sid accountSid, final Tag tag) {
		this.id = id;
		this.accountSid = accountSid;
		this.tag = tag;
	}
	
	public static Builder builder() {
        return new Builder();
    }
	
	public NetworkPoint setTag(Tag tag) {
		return new NetworkPoint( id, accountSid, tag);
	}
	
	public NetworkPoint setId(String id) {
		return new NetworkPoint( id, accountSid, tag);
	}
	
	public String getId() {
		return id;
	}
	
	public Tag getTag() {
		return tag;
	}

	public Sid getAccountSid() {
		return accountSid;
	}
	
	public int getGroup() {
		return group;
	}
	
	public void setGroup(int group) {
		this.group = group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public InetAddress getBroadcast() {
		return broadcast;
	}
	public void setBroadcast(InetAddress broadcast) {
		this.broadcast = broadcast;
	}
	public short getPrefixMask() {
		return prefixMask;
	}
	public void setPrefixMask(short prefixMask) {
		this.prefixMask = prefixMask;
	}
	
	public String toPrint() {
		return
				"id: "+id+
				" group: "+group+
				" MAC: "+macAddress+
				" name: "+name+
				" description: "+description+
				" IP : "+address.getHostAddress()+
				" Routable? "+IPUtils.isRoutableAddress(address.getHostAddress())+
				" IPV6 ?" +(address instanceof Inet6Address);
	}
	
	public enum Tag {
        DMZ("DMZ"), MZ("MZ"), UNINITIALIZED("UNINITIALIZED"), ORPHAN("ORPHAN");

        private final String text;

        private Tag(final String text) {
            this.text = text;
        }
        
        public boolean isTaged() {
        	switch(this) {
	        	case DMZ:
	        	case MZ:
	        		return true;
	        	default:
	        		return false;
        	}
        }
        /**
         * DMZ<->MZ allowed
         * @param tag
         * @return
         */
        
        public boolean isRouting(Tag tag) {
        	if(isTaged()&& tag.isTaged()) {
        		if(this.equals(tag)) {
        			return false;
        		}
        		return true;
        	}
        	return false; 	  	
        }

        public static Tag getValueOf(final String text) {
        	Tag[] values = values();
            for (final Tag value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid interface tag.");
        }

        @Override
        public String toString() {
            return text;
        }
    };
    
    public static final class Builder {
		private String id;
		private Sid accountSid;
		private Tag tag;

        private Builder() {
            super();
        }

        public NetworkPoint build() {
            return new NetworkPoint(id, accountSid, tag);
        }

		public void setTag(Tag tag) {
			this.tag = tag;
		}
		
		public void setAccountSid(Sid sid) {
			this.accountSid = sid;
		}
		
		public void setId(String id) {
			this.id = id;
		}

		
	}

	

}
