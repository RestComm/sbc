/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
 */
package org.restcomm.sbc.rest;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;

import org.restcomm.sbc.dao.AccountsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationFilter;
import org.restcomm.sbc.bo.LocationList;
import org.restcomm.sbc.bo.LocationNotFoundException;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.LocationConverter;
import org.restcomm.sbc.rest.converter.LocationsListConverter;
import org.restcomm.sbc.rest.converter.RestCommResponseConverter;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.NotThreadSafe;


import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import java.text.ParseException;
import java.util.List;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

//import org.joda.time.DateTime;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author gvagenas@gmail.com (George Vagenas)
 */
@NotThreadSafe
public abstract class LocationsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected LocationsListConverter listConverter;
    protected AccountsDao accountsDao;
    protected String instanceId;
    protected LocationManager locationManager=LocationManager.getLocationManager();
    
    protected boolean normalizePhoneNumbers;

    public LocationsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        LocationConverter converter = new LocationConverter(configuration);
        listConverter = new LocationsListConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Location.class, converter);
        builder.registerTypeAdapter(LocationList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);

        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();

        normalizePhoneNumbers = configuration.getBoolean("normalize-numbers-for-outbound-calls");
    }

    protected Response getLocation(final String aor, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Locations");
        
        Location location;
		try {
			location = locationManager.getLocation(aor);
		} catch (LocationNotFoundException e) {
			return status(NOT_FOUND).build();
		}   
            
        if (APPLICATION_XML_TYPE == responseType) {
               final RestCommResponse response = new RestCommResponse(location);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(location), APPLICATION_JSON).build();
        } else {
                return null;
        }
        
    }

    protected Response getLocations(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Locations");

        boolean localInstanceOnly = true;
        try {
            String localOnly = info.getQueryParameters().getFirst("localOnly");
            if (localOnly != null && localOnly.equalsIgnoreCase("false"))
                localInstanceOnly = false;
        } catch (Exception e) {
        }

        String pageSize = info.getQueryParameters().getFirst("PageSize");
        String page = info.getQueryParameters().getFirst("Page");
        String user = info.getQueryParameters().getFirst("User");
        String domain = info.getQueryParameters().getFirst("Domain");
        String userAgent = info.getQueryParameters().getFirst("UserAgent");
        String host = info.getQueryParameters().getFirst("Host");
        String sport = info.getQueryParameters().getFirst("Port");
        String transport = info.getQueryParameters().getFirst("Transport");
       
       

        if (pageSize == null) {
            pageSize = "50";
        }

        if (page == null) {
            page = "0";
        }
        
        if(sport == null) {
        	sport = "5060";
        }
        
        int port = Integer.parseInt(sport);
        int limit = Integer.parseInt(pageSize);
        int offset = (page == "0") ? 0 : (((Integer.parseInt(page) - 1) * Integer.parseInt(pageSize)) + Integer
                .parseInt(pageSize));


        LocationFilter filterForTotal;
        try {

            if (localInstanceOnly) {
                filterForTotal = new LocationFilter(user, domain, host, port, transport, userAgent, limit, offset, null);
            } else {
                filterForTotal = new LocationFilter(user, domain, host, port, transport, userAgent, limit, offset, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final int total = locationManager.getTotalLocations(filterForTotal);

        if (Integer.parseInt(page) > (total / limit)) {
            return status(javax.ws.rs.core.Response.Status.BAD_REQUEST).build();
        }

        LocationFilter filter = null;
        try {
        	if (localInstanceOnly) {
                filter = new LocationFilter(user, domain, host, port, transport, userAgent, limit, offset, null);
            } else {
                filter = new LocationFilter(user, domain, host, port, transport, userAgent, limit, offset, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final List<Location> locations = locationManager.getLocations(filter);

        listConverter.setCount(total);
        listConverter.setPage(Integer.parseInt(page));
        listConverter.setPageSize(Integer.parseInt(pageSize));
        listConverter.setPathUri(info.getRequestUri().getPath());

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new LocationList(locations));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new LocationList(locations)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    protected Response deleteLocation(final String aor) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Delete:Locations");
        
		try {
			locationManager.getLocation(aor);
		} catch (LocationNotFoundException e) {
			return status(NOT_FOUND).build();

		}      
            
        locationManager.unregister(aor);

        return ok().build();
    }



}
