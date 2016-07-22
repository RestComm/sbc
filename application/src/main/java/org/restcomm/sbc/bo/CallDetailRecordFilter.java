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
package org.restcomm.sbc.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;


/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

@Immutable
public class CallDetailRecordFilter {

    
    private final String recipient;
    private final String sender;
    private final String status;
    private final Date startTime;  // to initialize it pass string arguments with  yyyy-MM-dd format
    private final Date endTime;
    private final String parentCallSid;
    private final String conferenceSid;
    private final Integer limit;
    private final Integer offset;
    private final String instanceid;

    public CallDetailRecordFilter(String recipient, String sender, String status, String startTime, String endTime,
            String parentCallSid, String conferenceSid, Integer limit, Integer offset) throws ParseException {
        this(recipient,sender,status,startTime,endTime,parentCallSid, conferenceSid, limit,offset,null);
    }

    public CallDetailRecordFilter(String recipient, String sender, String status, String startTime, String endTime,
            String parentCallSid, String conferenceSid, Integer limit, Integer offset, String instanceId) throws ParseException { 

        // The LIKE keyword uses '%' to match any (including 0) number of characters, and '_' to match exactly one character
        // Add here the '%' keyword so +15126002188 will be the same as 15126002188 and 6002188
        if (recipient != null)
            recipient = "%".concat(recipient);
        if (sender != null)
            sender = "%".concat(sender);

        this.recipient = recipient;
        this.sender = sender;
        this.status = status;
        this.parentCallSid = parentCallSid;
        this.conferenceSid = conferenceSid;
        this.limit = limit;
        this.offset = offset;
        if (startTime != null) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            Date date = parser.parse(startTime);
            this.startTime = date;
        } else
            this.startTime = null;

        if (endTime != null) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            Date date = parser.parse(endTime);
            this.endTime = date;
        } else {
            this.endTime = null;
        }
        if (instanceId != null && !instanceId.isEmpty()) {
            this.instanceid = instanceId;
        } else {
            this.instanceid = null;
        }
    }


    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getParentCallSid() {
        return parentCallSid;
    }

    public String getConferenceSid() {
        return conferenceSid;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public String getInstanceid() { return instanceid; }
}
