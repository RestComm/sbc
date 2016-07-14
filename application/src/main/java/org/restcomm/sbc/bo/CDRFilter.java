package org.restcomm.sbc.bo;

import org.restcomm.sbc.annotations.concurrency.Immutable;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1/7/2016 19:06:38
 * @class   CDRFilter.java
 *
 */
@Immutable
public class CDRFilter {

    private final String accountSid;
    private final String recipient;
    private final String sender;
    private final String status;
    private final String startTime;
    private final String parentCallSid;
    private final Integer limit;
    private final Integer offset;

    public CDRFilter(String accountSid, String recipient, String sender, String status, String startTime,
            String parentCallSid, Integer limit, Integer offset) {
        this.accountSid = accountSid;

        // The LIKE keyword uses '%' to match any (including 0) number of characters, and '_' to match exactly one character
        // Add here the '%' keyword so +15126002188 will be the same as 15126002188 and 6002188
        if (recipient != null)
            recipient = "%".concat(recipient);
        if (sender != null)
            sender = "%".concat(sender);

        this.recipient = recipient;
        this.sender = sender;
        this.status = status;
        this.startTime = startTime;
        this.parentCallSid = parentCallSid;
        this.limit = limit;
        this.offset = offset;
    }

    public String getSid() {
        return accountSid;
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

    public String getStartTime() {
        return startTime;
    }

    public String getParentCallSid() {
        return parentCallSid;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

}
