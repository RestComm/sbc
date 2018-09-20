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

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

@Path("/Locations.json")
@ThreadSafe
public final class LocationsJsonEndpoint extends LocationsEndpoint {
    public LocationsJsonEndpoint() {
        super();
    }

    @GET
    public Response getLocationsAsJson(@Context UriInfo info) {
        return getLocations(info, APPLICATION_JSON_TYPE);
    }
    
    @Path("/{aor}.json")
    @GET
    public Response getLocationAsJson(@PathParam("aor") final String aor) {
        return getLocation(aor, APPLICATION_JSON_TYPE);
    }
    
    @Path("/{aor}.json")
    @DELETE
    public Response deleteLocationAsJson(@PathParam("aor") final String aor) {
        return deleteLocation(aor);
    }

    
}
