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
 */

package org.restcomm.sbc.managers.controller;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.shiro.ShiroResources;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 nov. 2016 3:25:21
 * @class   JMXProviderFactory.java
 *
 */
public class ManagementProviderFactory {
	
	private static transient Logger LOG = Logger.getLogger(ManagementProviderFactory.class);
	
	private static ManagementProvider instance;
	
	private ManagementProviderFactory() {
		
		
	}
	
	
	public static ManagementProvider getDefaultProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
		if(instance!=null )
			return instance;
		
		String providerClass = null;
		Path currentRelativePath = Paths.get("");
		String path = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + path);
		Configuration configuration = (Configuration) ShiroResources.getInstance().get(Configuration.class);
		
		if(path.endsWith("testsuite")) { 
			providerClass="org.restcomm.sbc.managers.controller.dummy.embedded.Provider";
		}
		else if(path.toLowerCase().contains("jboss")) {
			providerClass="org.restcomm.sbc.managers.controller.wildfly.Provider";
		}
		else if(path.endsWith("tomcat")) {
			providerClass="org.restcomm.sbc.managers.controller.tomcat.Provider";
		}
		
		if(providerClass==null) {
			System.err.println("Please check your configuration!");
			System.exit(0);
		}
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Factoring ManagerProvider "+providerClass);
		}
		Class<?> factory=Class.forName(providerClass);
		instance=(ManagementProvider) factory.newInstance();
		return instance;
		
	}
	
	
	
	public static ManagementProvider getProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
		if(instance!=null )
			return instance;
		
		String providerClass;
		
		Configuration configuration = (Configuration) ShiroResources.getInstance().get(Configuration.class);
		
		if(configuration==null) {
			providerClass="org.restcomm.sbc.managers.controller.wildfly.Provider";
			
		}
		else {
			providerClass=configuration.getString("jmx-management.provider");
			
			if(providerClass==null) {
				providerClass="org.restcomm.sbc.managers.controller.wildfly.Provider";
				
			}
		}
		if(LOG.isInfoEnabled()) {
			LOG.info("Factoring ManagerProvider "+providerClass);
		}
		Class<?> factory=Class.forName(providerClass);
		instance=(ManagementProvider) factory.newInstance();
		return instance;
		
	}

}
