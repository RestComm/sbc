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
import org.restcomm.sbc.dao.ConnectorsDao;
import org.restcomm.sbc.dao.RoutesDao;
import org.restcomm.sbc.managers.RouteManager;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.NetworkPointsDao;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.Route;
import org.restcomm.sbc.bo.RouteList;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.bo.Sid.Type;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.RouteConverter;
import org.restcomm.sbc.rest.converter.RouteListConverter;
import org.restcomm.sbc.rest.converter.RestCommResponseConverter;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.NotThreadSafe;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 ago. 2016 19:48:16
 * @class   RoutingPoliciesEndpoint.java
 *
 */
@NotThreadSafe
public abstract class RoutesEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected AccountsDao accountsDao;
    protected String instanceId;
	protected RouteListConverter listConverter;

	protected RouteManager routeManager;

	//private static transient Logger LOG = Logger.getLogger(RoutesEndpoint.class);
    public RoutesEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        listConverter = new RouteListConverter(configuration);
        RouteConverter converter = new RouteConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Route.class, converter);
        builder.registerTypeAdapter(RouteList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
        routeManager=RouteManager.getRouteManager();
       
    }

    protected Response getRoute(final Sid sid, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Routes");
        final RoutesDao dao = daos.getRoutesDao();
        Route route = dao.getRoute(sid);
        
        if (route == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(route);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(route), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }
    
    protected Response deleteRoute(final Sid sid) {
    	Account account=userIdentityContext.getEffectiveAccount();
        
       
        secure(account, "RestComm:Delete:Routes", SecuredType.SECURED_ACCOUNT);
        final RoutesDao dao = daos.getRoutesDao();
        final Route route = dao.getRoute(sid);
        
        if (route == null)
            return status(NOT_FOUND).build();
        
       
        dao.removeRoute(sid);
        routeManager.updateRoutingTable();
        return ok().build();
    }
    
    
    protected Response getRoutes(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Routes");

       
        RoutesDao dao = daos.getRoutesDao();

        final List<Route> routes = dao.getRoutes();

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new RouteList(routes));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new RouteList(routes)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    private Route createFrom(final String accountSid, final MultivaluedMap<String, String> data) {
        final Route.Builder builder = Route.builder();
       
        String sourceConnector = data.getFirst("SourceConnectorSid");
        String targetConnector = data.getFirst("TargetConnectorSid");
        builder.setSourceConnectorSid(new Sid(sourceConnector));
        builder.setTargetConnectorSid(new Sid(targetConnector));
        builder.setAccountSid(new Sid(accountSid));
        
        Sid sid=Sid.generate(Type.ROUTE);
        builder.setSid(sid);
        return builder.build();
    }
    
    protected Response putRoute(final MultivaluedMap<String, String> data, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Modify:Routes");
        final RoutesDao dao = daos.getRoutesDao();
        try {
            validate(data);
        } catch (final RuntimeException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        
        final Route route = createFrom(account.getSid().toString(), data);
              
        dao.addRoute(route);
        routeManager.updateRoutingTable();
        
        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(route);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(route), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    /**
     * RoutingPolicies depends on NetworkPoints
     * @param data
     */

    private void validate(final MultivaluedMap<String, String> data) {
    	final ConnectorsDao cdao = daos.getConnectorsDao();
    	final NetworkPointsDao ndao = daos.getNetworkPointDao();
        
    	if (!data.containsKey("SourceConnectorSid")) {
            throw new NullPointerException("Source Connector can not be null.");
        } 
    	if (!data.containsKey("TargetConnectorSid")) {
            throw new NullPointerException("Target Connector can not be null.");
        } 
    
        String ssid = data.getFirst("SourceConnectorSid");
        String tsid = data.getFirst("TargetConnectorSid");
            
        Connector sconnector= cdao.getConnector(new Sid(ssid));
        Connector tconnector= cdao.getConnector(new Sid(tsid));
        
        if(sconnector==null || tconnector==null) {
        	throw new NullPointerException("Connector does not exist.");
        }
        
        NetworkPoint spoint=ndao.getNetworkPoint(sconnector.getPoint());
        NetworkPoint tpoint=ndao.getNetworkPoint(tconnector.getPoint());
        
        if(spoint==null || tpoint==null) {
        	throw new NullPointerException("NetworkPoint does not exist.");
        }
        
        if(!(spoint.getTag().allowRouting(tpoint.getTag()))) {
        	throw new IllegalArgumentException("No routable NetworkPoints. Check that source is DMZ owned and target MZ owned!");
        }
        
        if(routeExists(sconnector.getSid().toString())) {
        	throw new IllegalArgumentException("Route from sourceConnector already exists!");
        }
        
    }
    
    private boolean routeExists(String sourceConnector) {
    	final RoutesDao rdao = daos.getRoutesDao();
    	List<Route> routes = rdao.getRoutes();
    	for(Route route:routes) {
    		if(route.getSourceConnector().equals(sourceConnector)) {
    			return true;
    		}
    	}
    	return false;
    }

}
