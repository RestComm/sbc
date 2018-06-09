package org.restcomm.sbc.rest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/2012-04-24")
public class ApplicationConfig extends Application {

public Set<Class<?>> getClasses() {
    return new HashSet<Class<?>>(
    		Arrays.asList(
    				AccountsXmlEndpoint.class,
    				AccountsJsonEndpoint.class,
    				BlackListsJsonEndpoint.class,
    				BlackListsXmlEndpoint.class,
    				CallsJsonEndpoint.class,
    				CallsXmlEndpoint.class,
    				ConnectorsXmlEndpoint.class,
    				ConnectorsJsonEndpoint.class,
    				LocationsJsonEndpoint.class,
    				LocationsXmlEndpoint.class,
    				NetworkPointsXmlEndpoint.class,
    				NetworkPointsJsonEndpoint.class,
    				RoutesXmlEndpoint.class,
    				RoutesJsonEndpoint.class,
    				StatisticsXmlEndpoint.class,
    				SupervisorJsonEndpoint.class,
    				WhiteListsJsonEndpoint.class,
    				WhiteListsXmlEndpoint.class));
}
}