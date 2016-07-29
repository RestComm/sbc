package org.restcomm.sbc.bo;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27 jul. 2016 23:58:42
 * @class   Connector.java
 *
 */
public class Connector {
	private Sid accountSid;
	private int port;
	private Transport transport;
	private String point;
	private String route;
	private String altRoute;
	
	public Connector(final Sid accountSid, final int port, final Transport transport, final String point, final String route, final String altRoute) {
		this.port = port;
		this.transport = transport;
		this.point = point;
		this.route = route;
		this.altRoute = altRoute;
		this.accountSid = accountSid;
	}
	
	public Connector(final int port, final Transport transport, final String point) {
		this.port = port;
		this.transport = transport;
		this.point = point;
	}
	
	public static Builder builder() {
        return new Builder();
    }
    
    public enum Transport {
        UDP("UDP"), TCP("TCP"), TLS("TLS"), WS("WS");

        private final String text;

        private Transport(final String text) {
            this.text = text;
        }

        public static Transport getValueOf(final String text) {
        	Transport[] values = values();
            for (final Transport value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid transport.");
        }

        @Override
        public String toString() {
            return text;
        }
    };

	public int getPort() {
		return port;
	}

	public Transport getTransport() {
		return transport;
	}

	
	public String getPoint() {
		return point;
	}
	
	public String getRoute() {
		return route;
	}

	public String getAltRoute() {
		return altRoute;
	}

	public Sid getAccountSid() {
		return accountSid;
	}
	
	public Connector setPort(int port) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public Connector setTransport(Transport transPort) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public Connector setPoint(String point) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public Connector setRoute(String route) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public Connector setAltRoute(String altRoute) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public Connector setAccountSid(Sid accountSid) {
		return new Connector(accountSid, port, transport, point, route, altRoute);
	}
	
	public static final class Builder {
		private Sid accountSid;
		private int port;
		private Transport transport;
		private String point;
		private String route;
		private String altRoute;

        private Builder() {
            super();
        }

        public Connector build() {
            return new Connector(accountSid, port, transport, point, route, altRoute);
        }

		public void setTransport(Transport transport) {
			this.transport = transport;
		}
		
		public void setPort(int port) {
			this.port = port;
		}

		public void setPoint(String point) {
			this.point = point;
		}

		public void setRoute(String route) {
			this.route = route;
		}

		public void setAltRoute(String altRoute) {
			this.altRoute = altRoute;
		}
		
		public void setAccountSid(Sid accountSid) {
			this.accountSid = accountSid;
		}
	}

	
}
