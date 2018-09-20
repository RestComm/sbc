/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.sbc.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;


/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@Path("/Accounts/{accountSid}/Supervisor.json")
@ThreadSafe
public class SupervisorJsonEndpoint extends SupervisorEndpoint{

    public SupervisorJsonEndpoint() {
        super();
    }

    //Simple PING/PONG message
    @GET
    public Response ping(@PathParam("accountSid") final String accountSid) {
        return pong(accountSid, APPLICATION_JSON_TYPE);
    }

    //Get live calls and statistics
    @Path("/metrics")
    @GET
    public Response getMetrics(@PathParam("accountSid") final String accountSid) {
        return getMetrics(accountSid, APPLICATION_JSON_TYPE);
    }

    //Register a remote location where Restcomm will send monitoring updates
    @Path("/remote")
    @POST
    public Response registerForMetricsUpdates(@PathParam("accountSid") final String accountSid, final MultivaluedMap<String, String> data) {
        return registerForUpdates(accountSid, data, APPLICATION_JSON_TYPE);
    }

    //Register a remote location where Restcomm will send monitoring updates for a specific Call
    @Path("/remote/{sid}")
    @POST
    public Response registerForCallMetricsUpdates(@PathParam("accountSid") final String accountSid, @PathParam("sid") final String sid, final MultivaluedMap<String, String> data) {
        return registerForCallUpdates(accountSid, sid, data, APPLICATION_JSON_TYPE);
    }
}
