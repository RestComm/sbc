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

package org.restcomm.sbc.managers.jmx;


import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.shiro.ShiroResources;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 nov. 2016 3:25:21
 * @class   JMXProviderFactory.java
 *
 */
public class JMXProviderFactory {
	
	private static transient Logger LOG = Logger.getLogger(JMXProviderFactory.class);
	
	private static JMXProvider instance;
	
	private JMXProviderFactory() {
		
		
	}
	
	public static JMXProvider getJMXProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
		if(instance!=null)
			return instance;
		
		Configuration configuration = (Configuration) ShiroResources.getInstance().get(Configuration.class);
		String providerClass=configuration.getString("jmx-management.provider");
		if(LOG.isDebugEnabled()) {
			LOG.debug("Factoring JMXProvider "+providerClass);
		}
		Class<?> factory=Class.forName(providerClass);
		instance=(JMXProvider) factory.newInstance();
		return instance;
		
	}

}
