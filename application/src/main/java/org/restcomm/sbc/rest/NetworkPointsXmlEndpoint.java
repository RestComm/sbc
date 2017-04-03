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
 * @date    27 jul. 2016 19:44:29
 * @class   NetworkPointsXmlEndpoint.java
 *
 */
@Path("/NetworkPoints")
@ThreadSafe
public final class NetworkPointsXmlEndpoint extends NetworkPointsEndpoint {
    public NetworkPointsXmlEndpoint() {
        super();
    }

    @Path("/{id}.json")
    @GET
    public Response getNetworkPointsAsJson(@PathParam("id") final String id) {
        return getNetworkPoint(id, APPLICATION_JSON_TYPE);
    }

    @Path("/{id}")
    @GET
    public Response getNetworkPointsAsXml(@PathParam("id") final String id) {
        return getNetworkPoint(id, APPLICATION_XML_TYPE);
    }
    
    @Path("/{id}")
    @DELETE
    public Response deleteNetworkPointAsXml(@PathParam("id") final String id) {
        return deleteNetworkPoint(id);
    }

    @GET
    public Response getNetworkPoints(@Context UriInfo info) {
        return getNetworkPoints(info, APPLICATION_XML_TYPE);
    }

    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response putNetworkPoint(final MultivaluedMap<String, String> data) {
        return putNetworkPoint(data, APPLICATION_XML_TYPE);
    }
    
    @Path("/{id}.json")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response updateNetworkPointAsJsonPost(@PathParam("id") final String id,
            final MultivaluedMap<String, String> data) {
        return updateNetworkPoint(id, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{id}.json")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @PUT
    public Response updateNetworkPointAsJsonPut(@PathParam("id") final String id,
            final MultivaluedMap<String, String> data) {
        return updateNetworkPoint(id, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{id}")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response updateNetworkPointAsXmlPost(@PathParam("id") final String id,
            final MultivaluedMap<String, String> data) {
        return updateNetworkPoint(id, data, APPLICATION_XML_TYPE);
    }

    @Path("/{id}")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @PUT
    public Response updateNetworkPointAsXmlPut(@PathParam("id") final String id,
            final MultivaluedMap<String, String> data) {
        return updateNetworkPoint(id, data, APPLICATION_XML_TYPE);
    }
}
