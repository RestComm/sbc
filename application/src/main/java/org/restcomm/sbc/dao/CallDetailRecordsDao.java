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

import org.joda.time.DateTime;
import org.restcomm.sbc.bo.CallDetailRecord;
import org.restcomm.sbc.bo.CallDetailRecordFilter;
import org.restcomm.sbc.bo.Sid;



/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
public interface CallDetailRecordsDao {
    void addCallDetailRecord(CallDetailRecord cdr);

    CallDetailRecord getCallDetailRecord(Sid sid);

    List<CallDetailRecord> getCallDetailRecords(Sid accountSid);

    List<CallDetailRecord> getCallDetailRecordsByRecipient(String recipient);

    List<CallDetailRecord> getCallDetailRecordsBySender(String sender);

    List<CallDetailRecord> getCallDetailRecordsByStatus(String status);

    List<CallDetailRecord> getCallDetailRecordsByStartTime(DateTime startTime);

    List<CallDetailRecord> getCallDetailRecordsByEndTime(DateTime endTime);

    List<CallDetailRecord> getCallDetailRecordsByStarTimeAndEndTime(DateTime endTime);

    List<CallDetailRecord> getCallDetailRecordsByParentCall(Sid parentCallSid);

    List<CallDetailRecord> getCallDetailRecordsByConferenceSid(Sid conferenceSid);

    List<CallDetailRecord> getCallDetailRecordsByInstanceId(Sid instanceId);

    void removeCallDetailRecord(Sid sid);

    void removeCallDetailRecords(Sid accountSid);

    void updateCallDetailRecord(CallDetailRecord cdr);

    // Support for filtering of calls list result, Issue 153
    List<CallDetailRecord> getCallDetailRecords(CallDetailRecordFilter filter);

    Integer getTotalCallDetailRecords(CallDetailRecordFilter filter);
}
