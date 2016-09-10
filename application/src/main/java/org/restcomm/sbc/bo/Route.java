package org.restcomm.sbc.bo;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 ago. 2016 19:16:34
 * @class   RoutingPolicy.java
 *
 */
public class Route {
	private Sid sid;
	
	private Sid accountSid;
	private Sid sourceConnector;
	private Sid targetConnector;
	
	
	
	public Route(final Sid sid, final Sid accountSid, final Sid sourceConnector, final Sid targetConnector) {
		this.sid = sid;
		this.sourceConnector = sourceConnector;
		this.targetConnector = targetConnector;
		this.accountSid = accountSid;
	}
	
	
	public static Builder builder() {
        return new Builder();
    }
    
	public Sid getSourceConnector() {
		return sourceConnector;
	}

	public Sid getTargetConnector() {
		return targetConnector;
	}
	
	public Sid getAccountSid() {
		return accountSid;
	}
	
	public Sid getSid() {
		return sid;
	}
	
	public Route setSourceConnector(Sid sourceConnector) {
		return new Route(sid, accountSid, sourceConnector, targetConnector);
	}
	
	public Route setTargetConnector(Sid targetConnector) {
		return new Route(sid, accountSid, sourceConnector, targetConnector);
	}
	
	public Route setAccountSid(Sid accountSid) {
		return new Route(sid, accountSid, sourceConnector, targetConnector);
	}
	
	public static final class Builder {
		private Sid sid;
		private Sid accountSid;
		private Sid sourceConnectorSid;
		private Sid targetConnectorSid;
		

        private Builder() {
            super();
        }

        public Route build() {
            return new Route(sid, accountSid, sourceConnectorSid, targetConnectorSid);
        }

		public void setSourceConnectorSid(Sid sourceConnector) {
			this.sourceConnectorSid=sourceConnector;
		}
		
		public void setTargetConnectorSid(Sid targetConnector) {
			this.targetConnectorSid=targetConnector;
		}
		
		
		public void setAccountSid(Sid accountSid) {
			this.accountSid = accountSid;
		}
		
		public void setSid(Sid sid) {
			this.sid = sid;
		}
	}

	
}
