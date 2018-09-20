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
import org.restcomm.sbc.dao.CallDetailRecordsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.bo.Account;
import org.restcomm.sbc.bo.CallDetailRecord;
import org.restcomm.sbc.bo.CallDetailRecordFilter;
import org.restcomm.sbc.bo.CallDetailRecordList;
import org.restcomm.sbc.bo.RestCommResponse;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.configuration.RestcommConfiguration;
import org.restcomm.sbc.rest.converter.CallDetailRecordConverter;
import org.restcomm.sbc.rest.converter.CallDetailRecordListConverter;
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


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author gvagenas@gmail.com (George Vagenas)
 */
@NotThreadSafe
public abstract class LiveCallsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected DaoManager daos;
    protected Gson gson;
    protected GsonBuilder builder;
    protected XStream xstream;
    protected CallDetailRecordListConverter listConverter;
    protected AccountsDao accountsDao;
    protected String instanceId;
    protected boolean normalizePhoneNumbers;
    protected CallManager callManager;

    public LiveCallsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        super.init(configuration);
        CallDetailRecordConverter converter = new CallDetailRecordConverter(configuration);
        listConverter = new CallDetailRecordListConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(CallDetailRecord.class, converter);
        builder.registerTypeAdapter(CallDetailRecordList.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
        xstream.registerConverter(listConverter);
		callManager = (CallManager) ShiroResources.getInstance().get(CallManager.class);

        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
       
        normalizePhoneNumbers = configuration.getBoolean("normalize-numbers-for-outbound-calls");
    }

    protected Response getCall(final String sid, final MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Calls");
        
        final CallDetailRecord cdr = callManager.getCall(sid).getCdr();
        if (cdr == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(account, account.getSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(cdr);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(cdr), APPLICATION_JSON).build();
            } else {
                return null;
            }
        }
    }

    
    protected Response getCalls(UriInfo info, MediaType responseType) {
    	Account account=userIdentityContext.getEffectiveAccount();
        secure(account, "RestComm:Read:Calls");

        boolean localInstanceOnly = true;
        try {
            String localOnly = info.getQueryParameters().getFirst("localOnly");
            if (localOnly != null && localOnly.equalsIgnoreCase("false"))
                localInstanceOnly = false;
        } catch (Exception e) {
        }

        String pageSize = info.getQueryParameters().getFirst("PageSize");
        String page = info.getQueryParameters().getFirst("Page");
        String recipient = info.getQueryParameters().getFirst("To");
        String sender = info.getQueryParameters().getFirst("From");
        String status = info.getQueryParameters().getFirst("Status");
        String startTime = info.getQueryParameters().getFirst("StartTime");
        String endTime = info.getQueryParameters().getFirst("EndTime");
        String parentCallSid = info.getQueryParameters().getFirst("ParentCallSid");
        String conferenceSid = info.getQueryParameters().getFirst("ConferenceSid");

        if (pageSize == null) {
            pageSize = "50";
        }

        if (page == null) {
            page = "0";
        }

        int limit = Integer.parseInt(pageSize);
        int offset = (page == "0") ? 0 : (((Integer.parseInt(page) - 1) * Integer.parseInt(pageSize)) + Integer
                .parseInt(pageSize));

        CallDetailRecordsDao dao = daos.getCallDetailRecordsDao();

        CallDetailRecordFilter filterForTotal;
        try {

            if (localInstanceOnly) {
                filterForTotal = new CallDetailRecordFilter(recipient, sender, status, startTime, endTime,
                        parentCallSid, conferenceSid, null, null);
            } else {
                filterForTotal = new CallDetailRecordFilter(recipient, sender, status, startTime, endTime,
                        parentCallSid, conferenceSid, null, null, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final int total = dao.getTotalCallDetailRecords(filterForTotal);

        if (Integer.parseInt(page) > (total / limit)) {
            return status(javax.ws.rs.core.Response.Status.BAD_REQUEST).build();
        }

        CallDetailRecordFilter filter;
        try {
            if (localInstanceOnly) {
                filter = new CallDetailRecordFilter(recipient, sender, status, startTime, endTime,
                        parentCallSid, conferenceSid, limit, offset);
            } else {
                filter = new CallDetailRecordFilter(recipient, sender, status, startTime, endTime,
                        parentCallSid, conferenceSid, limit, offset, instanceId);
            }
        } catch (ParseException e) {
            return status(BAD_REQUEST).build();
        }

        final List<CallDetailRecord> cdrs = dao.getCallDetailRecords(filter);

        listConverter.setCount(total);
        listConverter.setPage(Integer.parseInt(page));
        listConverter.setPageSize(Integer.parseInt(pageSize));
        listConverter.setPathUri(info.getRequestUri().getPath());

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new CallDetailRecordList(cdrs));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(new CallDetailRecordList(cdrs)), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
 

}
