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
import org.restcomm.sbc.bo.Sid;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    12 oct. 2016 18:04:41
 * @class   StatisticsXmlEndpoint.java
 *
 */
@Path("/Statistics")
@ThreadSafe
public final class StatisticsXmlEndpoint extends StatisticsEndpoint {
    public StatisticsXmlEndpoint() {
        super();
    }

    @Path("/{sid}.json")
    @GET
    public Response getStatisticsAsJson(@PathParam("sid") final Sid sid) {
        return getStatistics(sid, APPLICATION_JSON_TYPE);
    }

    @Path("/{sid}")
    @GET
    public Response getStatisticsAsXml(@PathParam("sid") final Sid sid) {
        return getStatistics(sid, APPLICATION_XML_TYPE);
    }
    
    @Path("/{sid}")
    @DELETE
    public Response deleteStatisticsAsXml(@PathParam("sid") final Sid sid) {
        return deleteStatistics(sid);
    }

    @GET
    public Response getRecords(@Context UriInfo info) {
        return getStatistics(info, APPLICATION_XML_TYPE);
    }

    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response putRecord(final MultivaluedMap<String, String> data) {
        return putStatistics(data, APPLICATION_XML_TYPE);
    }
    
    
}
