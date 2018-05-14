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
package org.restcomm.sbc.bo;

import java.net.Inet6Address;
import java.net.InetAddress;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 15:44:49
 * @class   NetworkPoint.java
 *
 */
public class NetworkPoint {
	
	private String id;
	private int group;
	private String description;
	private InetAddress address;
	private String macAddress;
	private InetAddress broadcast;
	private short prefixMask;
	private Tag tag;
	private Sid accountSid;
	
	public NetworkPoint(String id) {
		this.id = id;
		this.tag=Tag.IDLE;
	}
	
	public NetworkPoint(String id, InetAddress address) {
		this.id = id;
		this.tag=Tag.IDLE;
		this.address = address;
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
				"id="+id+
				", group="+group+
				", mac="+macAddress+
				", description="+description+
				", ip="+address.getHostAddress()+
				", ipv6 ?" +(address instanceof Inet6Address);
	}
	
	public enum Tag {
        DMZ("DMZ"), MZ("MZ"), IDLE("IDLE"), ORPHAN("ORPHAN");

        private final String text;

        private Tag(final String text) {
            this.text = text;
        }
        
        public boolean isTagged() {
        	switch(this) {
	        	case DMZ:
	        	case MZ:
	        		return true;
	        	default:
	        		return false;
        	}
        }
        /**
         * DMZ ->MZ allowed
         * Routing
         * @param tag
         * @return
         */
        
        public boolean allowRouting(Tag tag) {
        	if(this==DMZ && tag==MZ)
        		return true;
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
