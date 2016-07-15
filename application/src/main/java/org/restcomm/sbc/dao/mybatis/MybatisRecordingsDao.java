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

import static org.restcomm.sbc.dao.DaoUtils.readDateTime;
import static org.restcomm.sbc.dao.DaoUtils.readDouble;
import static org.restcomm.sbc.dao.DaoUtils.readSid;
import static org.restcomm.sbc.dao.DaoUtils.readString;
import static org.restcomm.sbc.dao.DaoUtils.readUri;
import static org.restcomm.sbc.dao.DaoUtils.writeDateTime;
import static org.restcomm.sbc.dao.DaoUtils.writeSid;
import static org.restcomm.sbc.dao.DaoUtils.writeUri;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.Recording;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.dao.RecordingsDao;


/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisRecordingsDao implements RecordingsDao {
    private static final String namespace = "org.restcomm.sbc.dao.RecordingsDao.";
    private final SqlSessionFactory sessions;
   
    private String recordingPath;

    public MybatisRecordingsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    

    @Override
    public void addRecording(Recording recording) {
        recording = recording.updateFileUri(generateLocalFileUri("/restcomm/recordings/" + recording.getSid()));
       
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addRecording", toMap(recording));
            session.commit();
        } finally {
            session.close();
        }
    }

    public URI generateLocalFileUri(String recordingRelativeUri) {
        URI uriToResolve = null;
        try {
            //For local stored recordings, add .wav suffix to the URI
            uriToResolve = new URI(recordingRelativeUri+".wav");
        } catch (URISyntaxException e) {}
        return null;
    }

    @Override
    public Recording getRecording(final Sid sid) {
        return getRecording(namespace + "getRecording", sid);
    }

    @Override
    public Recording getRecordingByCall(final Sid callSid) {
        return getRecording(namespace + "getRecordingByCall", callSid);
    }

    @Override
    public List<Recording> getRecordingsByCall(Sid callSid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getRecordingsByCall", callSid.toString());
            final List<Recording> recordings = new ArrayList<Recording>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    recordings.add(toRecording(result));
                }
            }
            return recordings;
        } finally {
            session.close();
        }
    }

    private Recording getRecording(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, sid.toString());
            if (result != null) {
                return toRecording(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Recording> getRecordings(final Sid accountSid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getRecordings", accountSid.toString());
            final List<Recording> recordings = new ArrayList<Recording>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    recordings.add(toRecording(result));
                }
            }
            return recordings;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeRecording(final Sid sid) {
        removeRecording(namespace + "removeRecording", sid);
    }

    @Override
    public void removeRecordings(final Sid accountSid) {
        removeRecording(namespace + "removeRecordings", accountSid);
    }

    private void removeRecording(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    private Map<String, Object> toMap(final Recording recording) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(recording.getSid()));
        map.put("date_created", writeDateTime(recording.getDateCreated()));
        map.put("date_updated", writeDateTime(recording.getDateUpdated()));
        map.put("account_sid", writeSid(recording.getAccountSid()));
        map.put("call_sid", writeSid(recording.getCallSid()));
        map.put("duration", recording.getDuration());
        map.put("api_version", recording.getApiVersion());
        map.put("uri", writeUri(recording.getUri()));
        map.put("file_uri", writeUri(recording.getFileUri()));
        return map;
    }

    private Recording toRecording(final Map<String, Object> map) {
        final Sid sid = readSid(map.get("sid"));
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateUpdated = readDateTime(map.get("date_updated"));
        final Sid accountSid = readSid(map.get("account_sid"));
        final Sid callSid = readSid(map.get("call_sid"));
        final Double duration = readDouble(map.get("duration"));
        final String apiVersion = readString(map.get("api_version"));
        final URI uri = readUri(map.get("uri"));
        //For backward compatibility. For old an database that we upgraded to the latest schema, the file_uri will be null so we need
        //to create the file_uri on the fly
        String fileUri = (String) map.get("file_uri");
        if (fileUri == null || fileUri.isEmpty()) {
            fileUri = generateLocalFileUri("/restcomm/recordings/" + sid).toString();
        }
        return new Recording(sid, dateCreated, dateUpdated, accountSid, callSid, duration, apiVersion, uri, readUri(fileUri));
    }
    
}
