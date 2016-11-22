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
    public Connector getConnector(final Sid sid) {
    	Connector entry = null;
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("sid", sid.toString());
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
	public void removeConnector(Sid sid) {
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("sid", sid.toString());
        removeConnector(namespace + "updateEntry", parms);
		
	}
    
    @Override
    public void updateConnector(final Sid sid, final String state) {
    	Map<String, Object> parms = new HashMap<String, Object>();
    	parms.put("sid", sid.toString());
    	parms.put("state", state);
        updateConnector(namespace + "updateEntry", parms);
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
    
    private void updateConnector(final String selector, final Object parameters) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(selector, parameters);
            session.commit();
        } finally {
            session.close();
        }
    }

    private Connector toConnector(final Map<String, Object> map) {
    	final Sid sid = readSid(map.get("sid"));
        final int port = readInteger(map.get("port"));
        final String point=readString(map.get("n_point"));
        final Sid entrySid = readSid(map.get("account_sid"));
        final Connector.Transport transport = readTransport(map.get("transport"));
        final Connector.State state = readState(map.get("state"));
        return new Connector(sid, entrySid, port, transport, point, state);
    }
   
    private Map<String, Object> toMap(final Connector entry) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(entry.getSid()));
        map.put("port", entry.getPort());
        map.put("transport", writeTransport(entry.getTransport()));
        map.put("state", writeState(entry.getState()));
        map.put("account_sid", writeSid(entry.getAccountSid()));
        map.put("n_point", entry.getPoint());
    
        return map;
    }

	
    
}
