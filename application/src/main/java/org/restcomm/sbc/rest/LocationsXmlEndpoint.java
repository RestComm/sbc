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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;



/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Path("/Locations")
@ThreadSafe
public final class LocationsXmlEndpoint extends LocationsEndpoint {
    public LocationsXmlEndpoint() {
        super();
    }


    @Path("/{aor}")
    @GET
    public Response getLocationAsXml(@PathParam("aor") final String aor) {
        return getLocation(aor, APPLICATION_XML_TYPE);
    }
    
    @Path("/{aor}")
    @DELETE
    public Response deleteLocationAsXml(@PathParam("aor") final String aor) {
        return deleteLocation(aor);
    }

    @GET
    public Response getLocations(@Context UriInfo info) {
        return getLocations(info, APPLICATION_XML_TYPE);
    }

    
}
