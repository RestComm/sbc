package org.restcomm.sbc.rest;



import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

@Provider
public class CORSResponseFilter
implements ContainerResponseFilter {

			private static transient Logger LOG = Logger.getLogger(CORSResponseFilter.class);
	
			@Override
		    public ContainerResponse filter(ContainerRequest creq, ContainerResponse cres) {
				if(LOG.isTraceEnabled()) {
					LOG.trace("Filtering CORS Response for Method: "+creq.getMethod()+":"+cres.getStatus());
			        cres.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
			        cres.getHttpHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
			        cres.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
			        cres.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
			        cres.getHttpHeaders().add("Access-Control-Max-Age", "1209600");
				}
		        return cres;
		    }
			
	

}