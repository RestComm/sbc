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
package org.restcomm.sbc.dao.mybatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;

import static org.restcomm.sbc.dao.DaoUtils.*;
import org.restcomm.sbc.dao.ConnectorsDao;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 19:06:21
 * @class   MybatisConnectorsDao.java
 *
 */
@ThreadSafe
public final class MybatisConnectorsDao implements ConnectorsDao {  
private final static String namespace = "org.restcomm.sbc.dao.ConnectorsDao.";
    private final SqlSessionFactory sessions;
    private static transient Logger LOG = Logger.getLogger(MybatisConnectorsDao.class);
	
    public MybatisConnectorsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addConnector(final Connector entry) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addEntry", toMap(entry));
            session.commit();
        } finally {
            session.close();
        }
    }


    @Override
    public Connector getConnector(final String pointId, final String transport, final int port) {
    	Connector entry = null;
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("n_point", pointId);
    	parms.put("transport", transport);
    	parms.put("port", port);
        entry = getConnector(namespace + "getEntry", parms);
        
        return entry;
    }

    private Connector getConnector(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameters);
            if (result != null) {
                return toConnector(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Connector> getConnectors() {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntries");
            final List<Connector> entrys = new ArrayList<Connector>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toConnector(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Connector> getConnectorsByNetworkPoint(String pointId) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getEntriesByNetworkPoint", pointId);
            final List<Connector> entrys = new ArrayList<Connector>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    entrys.add(toConnector(result));
                }
            }
            return entrys;
        } finally {
            session.close();
        }
    }
    
    @Override
    public void removeConnector(final String pointId, final String transport, final int port) {
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("n_point", pointId);
    	parms.put("transport", transport);
    	parms.put("port", port);
        removeConnector(namespace + "removeEntry", parms);
    }

    private void removeConnector(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, parameters);
            session.commit();
        } finally {
            session.close();
        }
    }

    private Connector toConnector(final Map<String, Object> map) {
        final int port = readInteger(map.get("port"));
        final String point=readString(map.get("n_point"));
        final String route=readString(map.get("n_point_route"));
        final String routeAlt=readString(map.get("n_point_route_alt"));
        final Sid entrySid = readSid(map.get("account_sid"));
        final Connector.Transport transport = readTransport(map.get("transport"));
        return new Connector(entrySid, port, transport, point, route, routeAlt);
    }
   
    private Map<String, Object> toMap(final Connector entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("port", entry.getPort());
        map.put("transport", writeTransport(entry.getTransport()));
        map.put("account_sid", writeSid(entry.getAccountSid()));
        map.put("n_point", entry.getPoint());
        map.put("n_point_route", entry.getRoute());
        map.put("n_point_route_alt", entry.getAltRoute());
    
        return map;
    }
    
}
