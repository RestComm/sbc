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
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.StatisticsDao;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.Statistics;
import org.restcomm.sbc.bo.StatisticsList;
import org.restcomm.sbc.bo.Sid.Type;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.StatisticsConverter;
import org.restcomm.sbc.rest.converter.StatisticsListConverter;
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
 * @date    12 oct. 2016 16:19:41
 * @class   StatisticsEndpoint.java
 *
 */
@NotThreadSafe
public abstract class StatisticsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected AccountsDao accountsDao;
    protected String instanceId;
	protected StatisticsListConverter listConverter;
	
	private static transient Logger LOG = Logger.getLogger(StatisticsEndpoint.class);
    public StatisticsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        listConverter = new StatisticsListConverter(configuration);
        StatisticsConverter converter = new StatisticsConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Statistics.class, converter);
        builder.registerTypeAdapter(StatisticsList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
        
       
    }

    protected Response getStatistics(final Sid sid, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Statistics");
        final StatisticsDao dao = daos.getStatisticsDao();
        Statistics route = dao.getRecord(sid);
        
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
    
    protected Response deleteStatistics(final Sid sid) {
    	Account account=userIdentityContext.getEffectiveAccount();
        
       
        secure(account, "RestComm:Delete:Statistics", SecuredType.SECURED_ACCOUNT);
        final StatisticsDao dao = daos.getStatisticsDao();
        final Statistics route = dao.getRecord(sid);
        
        if (route == null)
            return status(NOT_FOUND).build();
        
       
        dao.removeRecord(sid);

        return ok().build();
    }
    
    
    protected Response getStatistics(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Statistics");

       
        StatisticsDao dao = daos.getStatisticsDao();

        final List<Statistics> routes = dao.getRecords();

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new StatisticsList(routes));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new StatisticsList(routes)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    private Statistics createFrom(final String accountSid, final MultivaluedMap<String, String> data) {
        final Statistics.Builder builder = Statistics.builder();


        int mem_usage = Integer.parseInt(data.getFirst("MemoryUsage"));
        int cpu_usage = Integer.parseInt(data.getFirst("CpuUsage"));
        int rejected = Integer.parseInt(data.getFirst("CallsRejected"));
        int live = Integer.parseInt(data.getFirst("LiveCalls"));
        int threats = Integer.parseInt(data.getFirst("Threats"));
        double rate  = Double.parseDouble(data.getFirst("CallRate"));
        
        builder.setCallRate(rate);
        builder.setCallRejectedCount(rejected);
        builder.setCpuUsage(cpu_usage);
        builder.setLiveCallsCount(live);
        builder.setMemoryUsage(mem_usage);
        builder.setThreatCount(threats);
        Sid sid=Sid.generate(Type.RANDOM);
        builder.setSid(sid);
       
        return builder.build();
    }
    
    protected Response putStatistics(final MultivaluedMap<String, String> data, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Modify:Statistics");
        final StatisticsDao dao = daos.getStatisticsDao();
        try {
            validate(data);
        } catch (final RuntimeException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        
        final Statistics route = createFrom(account.getSid().toString(), data);
              
        dao.addRecord(route);
        
        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(route);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(route), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    

    private void validate(final MultivaluedMap<String, String> data) {
    	
        
    }

}
