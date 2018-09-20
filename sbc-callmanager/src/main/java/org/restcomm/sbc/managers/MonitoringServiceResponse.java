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
package org.restcomm.sbc.managers;

import java.util.List;
import java.util.Map;

import org.restcomm.sbc.bo.InstanceId;
import org.restcomm.sbc.call.Call;


/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
public class MonitoringServiceResponse {
    private final InstanceId instanceId;
    private final List<Call> callDetailsList;
    private final Map<String, Integer> countersMap;


    public MonitoringServiceResponse(final InstanceId instanceId, final List<Call> callDetailsList, final Map<String, Integer> countersMap) {
        super();
        this.instanceId = instanceId;
        this.callDetailsList = callDetailsList;
        this.countersMap = countersMap;
    }

    public List<Call> getCallDetailsList() {
        return callDetailsList;
    }

    public Map<String, Integer> getCountersMap() {
        return countersMap;
    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

}
