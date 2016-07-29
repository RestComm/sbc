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
import org.restcomm.sbc.dao.BanListDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.notification.impl.Monitor.Action;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.BanList.Reason;
import org.restcomm.sbc.bo.BanList.Type;
import org.restcomm.sbc.bo.BanListFilter;
import org.restcomm.sbc.bo.BanListList;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.BanListConverter;
import org.restcomm.sbc.rest.converter.BanListListConverter;
import org.restcomm.sbc.rest.converter.RestCommResponseConverter;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.NotThreadSafe;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author gvagenas@gmail.com (George Vagenas)
 */
@NotThreadSafe
public abstract class BlackListsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected BanListListConverter listConverter;
    protected AccountsDao accountsDao;
    protected String instanceId;



    public BlackListsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        BanListConverter converter = new BanListConverter(Type.BLACK, configuration);
        listConverter = new BanListListConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(BanList.class, converter);
        builder.registerTypeAdapter(BanListList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
        
        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
       
    }

    protected Response getBanList(final String ipAddress, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:BanLists");
        final BanListDao dao = daos.getBlackListDao();
        final BanList banList = dao.getBanList(ipAddress);
        if (banList == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(banList);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(banList), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }
    protected Response deleteBanList(final String ipAddress) {
    	Account account=userIdentityContext.getEffectiveAccount();
        
       
        secure(account, "RestComm:Delete:BanLists", SecuredType.SECURED_ACCOUNT);
        final BanListDao dao = daos.getBlackListDao();
        final BanList banList = dao.getBanList(ipAddress);
        
        if (banList == null)
            return status(NOT_FOUND).build();
        
        dao.removeBanList(banList);

        return ok().build();
    }
    
    private BanList update(final BanList banList, final MultivaluedMap<String, String> data) {
        BanList result = banList;
        
        
        if (data.containsKey("IpAddress")) {
            result = result.setIpAddress(data.getFirst("IpAddress"));
        }
        
        return result;
    }
    
    protected Response updateBanList(final String ipAddress) {
        //First check if the account has the required permissions in general, this way we can fail fast and avoid expensive DAO operations
        checkPermission("RestComm:Delete:BanLists");
       
        final Account account = userIdentityContext.getEffectiveAccount();
        
        secure(account, "RestComm:Delete:BanLists", SecuredType.SECURED_ACCOUNT);
        
        BlackListDao dao = daos.getBlackListDao();
        
        BanList banList=dao.getBanList(ipAddress);
        
        if (banList == null)
            return status(NOT_FOUND).build();
        
        banList=banList.setAction(Action.REMOVE);
       
        dao.updateBanList(banList);

        return ok().build();
    }
    
    protected Response updateBanList(final String ipAddress, final MultivaluedMap<String, String> data,
            final MediaType responseType) {
        //First check if the account has the required permissions in general, this way we can fail fast and avoid expensive DAO operations
        checkPermission("RestComm:Modify:BanLists");
        final Account account = userIdentityContext.getEffectiveAccount();
        if (account == null) {
            return status(NOT_FOUND).build();
        } else {

            secure(account, "RestComm:Modify:BanLists", SecuredType.SECURED_ACCOUNT );
            
            BlackListDao dao = daos.getBlackListDao();
            BanList banList=dao.getBanList(ipAddress);
            
            update(banList, data);
            
            dao.updateBanList(banList);
            
            if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(banList), APPLICATION_JSON).build();
            } else if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(banList);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else {
                return null;
            }
        }
    }
    
    protected Response getBanLists(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:BanLists");

        boolean localInstanceOnly = true;
        try {
            String localOnly = info.getQueryParameters().getFirst("localOnly");
            if (localOnly != null && localOnly.equalsIgnoreCase("false"))
                localInstanceOnly = false;
        } catch (Exception e) {
        }

        String pageSize = info.getQueryParameters().getFirst("PageSize");
        String page = info.getQueryParameters().getFirst("Page");
        String ipAddress = info.getQueryParameters().getFirst("IpAddress");
        String reason = info.getQueryParameters().getFirst("Reason");
        String dateCreated = info.getQueryParameters().getFirst("DateCreated");
        String dateExpires = info.getQueryParameters().getFirst("DateExpires");
        String accountSid = info.getQueryParameters().getFirst("AccountSid");
        String action = info.getQueryParameters().getFirst("Action");

        if (pageSize == null) {
            pageSize = "50";
        }

        if (page == null) {
            page = "0";
        }

        int limit = Integer.parseInt(pageSize);
        int offset = (page == "0") ? 0 : (((Integer.parseInt(page) - 1) * Integer.parseInt(pageSize)) + Integer
                .parseInt(pageSize));

        BanListDao dao = daos.getBlackListDao();

        BanListFilter filterForTotal;
        try {

            if (localInstanceOnly) {
                filterForTotal = new BanListFilter(Type.BLACK.toString(), ipAddress, accountSid, dateCreated, dateExpires, reason, action, limit, offset, null);
            } else {
                filterForTotal = new BanListFilter(Type.BLACK.toString(), ipAddress, accountSid, dateCreated, dateExpires, reason, action, limit, offset, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final int total = dao.getTotalBanLists(filterForTotal);

        if (Integer.parseInt(page) > (total / limit)) {
            return status(javax.ws.rs.core.Response.Status.BAD_REQUEST).build();
        }

        BanListFilter filter;
        try {
            if (localInstanceOnly) {
                filter = new BanListFilter(Type.BLACK.toString(), ipAddress, accountSid, dateCreated, dateExpires, reason, action, limit, offset, null);
            } else {
                filter = new BanListFilter(Type.BLACK.toString(), ipAddress, accountSid, dateCreated, dateExpires, reason, action, limit, offset, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final List<BanList> banLists = dao.getBanLists(filter);

        listConverter.setCount(total);
        listConverter.setPage(Integer.parseInt(page));
        listConverter.setPageSize(Integer.parseInt(pageSize));
        listConverter.setPathUri(info.getRequestUri().getPath());

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new BanListList(banLists));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new BanListList(banLists)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
    
    private BanList createFrom(final String accountSid, final MultivaluedMap<String, String> data) {
        final BanList.Builder builder = BanList.builder();
        builder.setAccountSid(new Sid(accountSid));
        String ipAddress = data.getFirst("IpAddress");
        builder.setipAddress(ipAddress);
        builder.setAction(Action.APPLY);
        builder.setReason(Reason.ADMIN);
        return builder.build();
    }
    
    protected Response putBanList(final MultivaluedMap<String, String> data, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Modify:BanLists");
        final BanListDao dao = daos.getBlackListDao();
        try {
            validate(data);
        } catch (final RuntimeException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        
        final BanList banList = createFrom(account.getSid().toString(), data);
        dao.addBanList(banList);
        
        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(banList);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(banList), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    private void validate(final MultivaluedMap<String, String> data) {
        if (!data.containsKey("IpAddress")) {
            throw new NullPointerException("IpAddress can not be null.");
        } 
        
    }

}
