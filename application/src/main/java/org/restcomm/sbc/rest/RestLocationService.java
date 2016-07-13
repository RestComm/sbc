/*******************************************************************************
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc, Eolos IT Corp and individual contributors
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
 *******************************************************************************/

package org.restcomm.sbc.rest;


 

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.restcomm.sbc.managers.LocationManager;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    4/6/2016 13:11:49
 * @class   RestLocationService.java
 *
 */
 
@Path("/locationservice")
public class RestLocationService {
 
	  @GET
	  @Produces("application/json")
	  public Response get() throws JSONException {
		    
		LocationManager lm = LocationManager.getLocationManager();	
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("location", lm.getLocations());
		
		String result = "@Produces(\"application/json\") Output: \n\nLocation Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	  }
 
	 
}