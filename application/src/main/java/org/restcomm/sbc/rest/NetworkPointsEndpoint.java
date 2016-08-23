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
import org.apache.ibatis.exceptions.PersistenceException;
import org.restcomm.sbc.dao.AccountsDao;
import org.restcomm.sbc.dao.NetworkPointsDao;
import org.restcomm.sbc.managers.NetworkManager;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.NetworkPointList;
import org.restcomm.sbc.bo.NetworkPoint.Tag;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.NetworkPointConverter;
import org.restcomm.sbc.rest.converter.NetworkPointListConverter;
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
 * @date    27 jul. 2016 19:08:07
 * @class   NetworkPointsEndpoint.java
 *
 */
@NotThreadSafe
public abstract class NetworkPointsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected AccountsDao accountsDao;
    protected String instanceId;
	protected NetworkPointListConverter listConverter;



    public NetworkPointsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        listConverter = new NetworkPointListConverter(configuration);
        NetworkPointConverter converter = new NetworkPointConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(NetworkPoint.class, converter);
        builder.registerTypeAdapter(NetworkPointList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
       
    }

    protected Response getNetworkPoint(final String id, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:NetworkPoints");
        final NetworkPointsDao dao = daos.getNetworkPointDao();
        NetworkPoint point = dao.getNetworkPoint(id);
        final NetworkPoint realPoint = NetworkManager.getNetworkPoint(id);
        
        if(realPoint!=null&& point!=null) {
        	point.setMacAddress(realPoint.getMacAddress());
        	point.setDescription(realPoint.getDescription());
        }
        
        if (point == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(point);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(point), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }
    protected Response deleteNetworkPoint(final String id) {
    	Account account=userIdentityContext.getEffectiveAccount();
        
       
        secure(account, "RestComm:Delete:NetworkPoints", SecuredType.SECURED_ACCOUNT);
        final NetworkPointsDao dao = daos.getNetworkPointDao();
        final NetworkPoint point = dao.getNetworkPoint(id);
        
        if (point == null)
            return status(NOT_FOUND).build();
        

        dao.removeNetworkPoint(id);

        return ok().build();
    }
    
    private NetworkPoint update(final NetworkPoint point, final MultivaluedMap<String, String> data) {
        NetworkPoint result = point;
        
        
        if (data.containsKey("Tag")) {
            result = result.setTag(Tag.getValueOf(data.getFirst("Tag")));
        }
        
        return result;
    }
    
    protected Response updateNetworkPoint(final String id) {
        //First check if the account has the required permissions in general, this way we can fail fast and avoid expensive DAO operations
        checkPermission("RestComm:Delete:NetworkPoints");
       
        final Account account = userIdentityContext.getEffectiveAccount();
        
        secure(account, "RestComm:Delete:NetworkPoints", SecuredType.SECURED_ACCOUNT);
        
        NetworkPointsDao dao = daos.getNetworkPointDao();
        
        NetworkPoint point=dao.getNetworkPoint(id);
        
        if (point == null)
            return status(NOT_FOUND).build();
        
        dao.updateNetworkPoint(point);

        return ok().build();
    }
    
    protected Response updateNetworkPoint(final String id, final MultivaluedMap<String, String> data,
            final MediaType responseType) {
        //First check if the account has the required permissions in general, this way we can fail fast and avoid expensive DAO operations
        checkPermission("RestComm:Modify:NetworkPoints");
        final Account account = userIdentityContext.getEffectiveAccount();
        if (account == null) {
            return status(NOT_FOUND).build();
        } else {

            secure(account, "RestComm:Modify:NetworkPoints", SecuredType.SECURED_ACCOUNT );
            
            NetworkPointsDao dao = daos.getNetworkPointDao();
            NetworkPoint point=dao.getNetworkPoint(id);
            
            update(point, data);
            
            dao.updateNetworkPoint(point);
            
            if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(point), APPLICATION_JSON).build();
            } else if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(point);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else {
                return null;
            }
        }
    }
    
    protected Response getNetworkPoints(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:NetworkPoints");

       
        NetworkPointsDao dao = daos.getNetworkPointDao();


        final List<NetworkPoint> points = NetworkManager.mergeNetworkPoints(dao.getNetworkPoints());


        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new NetworkPointList(points));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new NetworkPointList(points)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    private NetworkPoint createFrom(final String accountSid, final MultivaluedMap<String, String> data) {
        final NetworkPoint.Builder builder = NetworkPoint.builder();
        builder.setAccountSid(new Sid(accountSid));
        String tag = data.getFirst("Tag");
        builder.setTag(Tag.valueOf(tag));
        String id = data.getFirst("Id");
        builder.setId(id);
        return builder.build();
    }
    
    protected Response putNetworkPoint(final MultivaluedMap<String, String> data, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Modify:NetworkPoints");
        final NetworkPointsDao dao = daos.getNetworkPointDao();
        try {
            validate(data);
        } catch (final RuntimeException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        
        final NetworkPoint point = createFrom(account.getSid().toString(), data);
        
        
        dao.addNetworkPoint(point);
        
        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(point);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(point), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    private void validate(final MultivaluedMap<String, String> data) {
        if (!data.containsKey("Id")) {
            throw new NullPointerException("Id can not be null.");
        } 
        if (!data.containsKey("Tag")) {
            throw new NullPointerException("Tag can not be null.");
        }
        String id = data.getFirst("Id");
        if(!NetworkManager.exists(id)){
        	throw new NullPointerException("Real ID does not exist.");
        	
        }
        
        final NetworkPointsDao dao = daos.getNetworkPointDao();
        if(dao.getNetworkPoint(id)!=null) {
        	throw new PersistenceException("Id is yet taged");
        };
        
    }

}
