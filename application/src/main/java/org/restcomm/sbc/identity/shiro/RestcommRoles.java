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
package org.restcomm.sbc.identity.shiro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;


/**
 * @author orestis.tsakiridis@telestax.com (Orestis Tsakiridis)
 */
public class RestcommRoles {
    private Map<String, SimpleRole> roles;
    private static transient Logger LOG = Logger.getLogger(RestcommRoles.class);
	

    /**
     * Parses restcomm.xml configuration and builds a map out of roles from it.
     *
     * @param configuration - An apache configuration object based on the <security-roles/> element
     */
    public RestcommRoles(Configuration configuration) {
        roles = new HashMap<String, SimpleRole>();
        loadSecurityRoles(configuration);
    }

    public SimpleRole getRole(final String role) {
        return roles.get(role);
    }

    private void loadSecurityRoles(final Configuration configuration) {
        @SuppressWarnings("unchecked")
        final List<String> roleNames = (List<String>) configuration.getList("role[@name]");
        final int numberOfRoles = roleNames.size();
        if (numberOfRoles > 0) {
            for (int roleIndex = 0; roleIndex < numberOfRoles; roleIndex++) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("role(").append(roleIndex).append(")").toString();
                final String prefix = buffer.toString();
                final String name = configuration.getString(prefix + "[@name]");
                @SuppressWarnings("unchecked")
                final List<String> permissions = configuration.getList(prefix + ".permission");

                if (name != null) {
                    if (permissions.size() > 0 ) {
                        final SimpleRole role = new SimpleRole(name);
                        for (String permissionString: permissions) {
                            LOG.info("loading permission " + permissionString + " into " + name + " role");
                            final Permission permission = new WildcardPermission(permissionString);
                            role.add(permission);
                        }
                        roles.put(name, role);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if ( roles == null || roles.size() == 0 )
            return "no roles defined";
        else {
            StringBuffer buffer = new StringBuffer();
            for ( String role: roles.keySet() ) {
                buffer.append(role);
                SimpleRole simpleRole = roles.get(role);
                Set<Permission> permissions = simpleRole.getPermissions();
                buffer.append("[");
                for (Permission permission: permissions) {
                    buffer.append(permission.toString());
                    buffer.append(",");
                }
                buffer.append("]");
            }
            return buffer.toString();
        }
    }

}



