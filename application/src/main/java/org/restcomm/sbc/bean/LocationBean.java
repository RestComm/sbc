package org.restcomm.sbc.bean;

import java.io.Serializable;

import java.util.ArrayList;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;





import org.restcomm.sbc.bo.Location;

import org.restcomm.sbc.managers.LocationManager;


@ManagedBean(name = "location")
@SessionScoped
public class LocationBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	public List<Location> getLocations() {
		LocationManager lm = LocationManager.getLocationManager();	
		return (List<Location>) lm.getLocations();
	}

	

}