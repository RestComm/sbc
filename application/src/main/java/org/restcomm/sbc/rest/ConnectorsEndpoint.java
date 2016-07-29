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
import org.apache.log4j.Logger;
import org.restcomm.sbc.dao.AccountsDao;
import org.restcomm.sbc.dao.ConnectorsDao;
import org.restcomm.sbc.managers.JMXManager;
import org.restcomm.sbc.managers.NetworkManager;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.NetworkPointsDao;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.ConnectorList;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.ConnectorConverter;
import org.restcomm.sbc.rest.converter.ConnectorListConverter;
import org.restcomm.sbc.rest.converter.RestCommResponseConverter;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.NotThreadSafe;
import javax.annotation.PostConstruct;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.util.List;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 19:08:07
 * @class   ConnectorsEndconnector.java
 *
 */
@NotThreadSafe
public abstract class ConnectorsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected AccountsDao accountsDao;
    protected String instanceId;
	protected ConnectorListConverter listConverter;
	protected JMXManager jmxManager;

	private static transient Logger LOG = Logger.getLogger(ConnectorsEndpoint.class);
    public ConnectorsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        listConverter = new ConnectorListConverter(configuration);
        ConnectorConverter converter = new ConnectorConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Connector.class, converter);
        builder.registerTypeAdapter(ConnectorList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
        try {
			jmxManager=JMXManager.getInstance();
		} catch (MalformedObjectNameException | InstanceNotFoundException | IntrospectionException | ReflectionException
				| IOException e) {
			LOG.error("Connector Manager unavailable!");
		}
       
    }

    protected Response getConnector(final String pointId, final String transport, final int port, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Connectors");
        final ConnectorsDao dao = daos.getConnectorsDao();
        Connector connector = dao.getConnector(pointId, transport, port);
        
        if (connector == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(connector);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(connector), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }
    
    protected Response getConnectorsByNetworkPoint(final String pointId, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Connectors");
        final ConnectorsDao dao = daos.getConnectorsDao();
        List<Connector> connectors = dao.getConnectorsByNetworkPoint(pointId);
        
        if (connectors == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(new ConnectorList(connectors));
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(new ConnectorList(connectors)), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }
    
    protected Response deleteConnector(final String pointId, final String transport, final int port) {
    	Account account=userIdentityContext.getEffectiveAccount();
        
       
        secure(account, "RestComm:Delete:Connectors", SecuredType.SECURED_ACCOUNT);
        final ConnectorsDao dao = daos.getConnectorsDao();
        final Connector connector = dao.getConnector(pointId, transport, port);
        
        if (connector == null)
            return status(NOT_FOUND).build();
        
        NetworkPoint point=NetworkManager.getNetworkPoint(pointId);
        boolean status=false;
        
        if(point!=null) {
        	String ipAddress=point.getAddress().getHostAddress();
        	try {
				status=jmxManager.removeSipConnector(ipAddress, port, transport);
			} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
				LOG.error("JMX Manager failed");
			}

        }
        if (!status)
            return status(PRECONDITION_FAILED).build();
        
        dao.removeConnector(pointId, transport, port);

        return ok().build();
    }
    
    
    protected Response getConnectors(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Connectors");

       
        ConnectorsDao dao = daos.getConnectorsDao();

        final List<Connector> connectors = dao.getConnectors();

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new ConnectorList(connectors));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new ConnectorList(connectors)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    private Connector createFrom(final String accountSid, final MultivaluedMap<String, String> data) {
        final Connector.Builder builder = Connector.builder();
        int port = Integer.parseInt(data.getFirst("Port"));
        builder.setPort(port);
        builder.setAccountSid(new Sid(accountSid));
        String transport = data.getFirst("Transport");
        builder.setTransport(Connector.Transport.valueOf(transport));
        String point = data.getFirst("NetworkPointId");
        builder.setPoint(point);
        String route = data.getFirst("NetworkPointRouteId");
        builder.setRoute(route);
        return builder.build();
    }
    
    protected Response putConnector(final MultivaluedMap<String, String> data, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Modify:Connectors");
        final ConnectorsDao dao = daos.getConnectorsDao();
        try {
            validate(data);
        } catch (final RuntimeException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        
        final Connector connector = createFrom(account.getSid().toString(), data);
        
        NetworkPoint point=NetworkManager.getNetworkPoint(connector.getPoint());
        boolean status=false;
        
        if(point!=null) {
        	String ipAddress=point.getAddress().getHostAddress();
        	try {
				status=jmxManager.addSipConnector(ipAddress, connector.getPort(), connector.getTransport().toString());
			} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
				LOG.error("JMX Manager failed");
			}

        }
        if (!status)
            return status(PRECONDITION_FAILED).entity("Cannot add SIP Connector").build();
        
        dao.addConnector(connector);
        
        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(connector);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(connector), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    private void validate(final MultivaluedMap<String, String> data) {
    	final NetworkPointsDao dao = daos.getNetworkPointDao();
        
    	if (!data.containsKey("Port")) {
            throw new NullPointerException("Port can not be null.");
        } 
    	
        if (!data.containsKey("Transport")) {
            throw new NullPointerException("Transport can not be null.");
        }
        
        String id = data.getFirst("NetworkPointId");
        
        if(!NetworkManager.exists(id)){
        	throw new NullPointerException("Real NetworkPointId does not exist.");
        	
        }
        
        
        String rid = data.getFirst("NetworkPointRouteId");
        
        if(!NetworkManager.exists(rid)){
        	throw new NullPointerException("Real NetworkPointRouteId does not exist.");
        	
        }
        
        NetworkPoint point= dao.getNetworkPoint(id);
        NetworkPoint rpoint= dao.getNetworkPoint(rid);
        
        if(point==null||rpoint==null) {
        	throw new NullPointerException("Real NetworkPointRouteId does not exist.");
        }
        
        if(!point.getTag().isRouting(rpoint.getTag())) {
        	throw new NullPointerException("NetworkPoint/Route does not fit routing policy. Check NetworkPoints involved are already taged.");
        	
        }
        
        
    }

}
