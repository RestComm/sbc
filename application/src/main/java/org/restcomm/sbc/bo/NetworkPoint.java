package org.restcomm.sbc.bo;

import java.net.InetAddress;

public class NetworkPoint {
	
	private int id;
	private int group;
	private String name;
	private String description;
	private InetAddress address;
	private String macAddress;
	private InetAddress broadcast;
	private short prefixMask;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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

}
