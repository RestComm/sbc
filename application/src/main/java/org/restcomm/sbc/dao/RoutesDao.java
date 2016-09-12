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
package org.restcomm.sbc.dao;

import java.util.List;

import org.restcomm.sbc.bo.Route;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 ago. 2016 19:21:23
 * @class   RoutingPoliciesDao.java
 *
 */
public interface RoutesDao {
	
    void addRoute(Route route);
   
    Route getRoute(Sid sid);

    List<Route> getRoutes();

	void removeRoute(Sid sid);
}
