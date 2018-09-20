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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 18:33:52
 * @class   BanListsXmlEndpoint.java
 *
 */
@Path("/BlackList")
@ThreadSafe
public final class BlackListsXmlEndpoint extends BlackListsEndpoint {
    public BlackListsXmlEndpoint() {
        super();
    }

    @Path("/{ipAddress}.json")
    @GET
    public Response getBanListsAsJson(@PathParam("ipAddress") final String ipAddress) {
        return getBanList(ipAddress, APPLICATION_JSON_TYPE);
    }

    @Path("/{ipAddress}")
    @GET
    public Response getBanListsAsXml(@PathParam("ipAddress") final String ipAddress) {
        return getBanList(ipAddress, APPLICATION_XML_TYPE);
    }
    
    @Path("/{ipAddress}")
    @DELETE
    /* Do not really remove entry but tag entry and delegate deletion to Monitor */
    public Response deleteBanListAsXml(@PathParam("ipAddress") final String ipAddress) {
        return updateBanList(ipAddress);
    }

    @GET
    public Response getBanLists(@Context UriInfo info) {
        return getBanLists(info, APPLICATION_XML_TYPE);
    }

    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response putBanList(final MultivaluedMap<String, String> data) {
        return putBanList(data, APPLICATION_XML_TYPE);
    }
    
    @Path("/{ipAddress}.json")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response updateAccountAsJsonPost(@PathParam("ipAddress") final String ipAddress,
            final MultivaluedMap<String, String> data) {
        return updateBanList(ipAddress, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{ipAddress}.json")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @PUT
    public Response updateAccountAsJsonPut(@PathParam("ipAddress") final String ipAddress,
            final MultivaluedMap<String, String> data) {
        return updateBanList(ipAddress, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{ipAddress}")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response updateAccountAsXmlPost(@PathParam("ipAddress") final String ipAddress,
            final MultivaluedMap<String, String> data) {
        return updateBanList(ipAddress, data, APPLICATION_XML_TYPE);
    }

    @Path("/{ipAddress}")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @PUT
    public Response updateAccountAsXmlPut(@PathParam("ipAddress") final String ipAddress,
            final MultivaluedMap<String, String> data) {
        return updateBanList(ipAddress, data, APPLICATION_XML_TYPE);
    }
}
