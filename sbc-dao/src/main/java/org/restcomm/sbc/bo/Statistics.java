package org.restcomm.sbc.bo;

import org.joda.time.DateTime;
public class Statistics {
	
	private Sid sid;
	private int memoryUsage;
	private int cpuUsage;
	private int liveCallsCount;
	private double callRate;
	private int callRejectedCount;
	private int threatCount;
	private DateTime dateCreated;
	
	public Statistics(Sid sid, int memoryUsage, int cpuUsage, int callCount, double callRate, int callRejected, int threatCount, DateTime dateCreated) {
		this.sid=sid;
		this.callRate=callRate;
		this.callRejectedCount=callRejected;
		this.cpuUsage=cpuUsage;
		this.memoryUsage=memoryUsage;
		this.threatCount=threatCount;
		this.dateCreated=dateCreated;
	}
	
	public static Builder builder() {
        return new Builder();
    }
	
	public int getMemoryUsage() {
		return memoryUsage;
	}
	public void setMemoryUsage(int memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public int getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public int getLiveCallsCount() {
		return liveCallsCount;
	}
	public void setLiveCallsCount(int liveCallsCount) {
		this.liveCallsCount = liveCallsCount;
	}
	public double getCallRate() {
		return callRate;
	}
	public void setCallRate(double callRate) {
		this.callRate = callRate;
	}
	public int getCallRejectedCount() {
		return callRejectedCount;
	}
	public void setCallRejectedCount(int callRejectedCount) {
		this.callRejectedCount = callRejectedCount;
	}
	public int getThreatCount() {
		return threatCount;
	}
	public void setThreatCount(int threatCount) {
		this.threatCount = threatCount;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Sid getSid() {
		return sid;
	}

	
	public static final class Builder {
		private Sid sid;
		private int memoryUsage;
		private int cpuUsage;
		private int liveCallsCount;
		private double callRate;
		private int callRejectedCount;
		private int threatCount;
		

        private Builder() {
            super();
        }

        public Statistics build() {
        	final DateTime now = DateTime.now();
            return new Statistics(sid, memoryUsage, cpuUsage, liveCallsCount, callRate, callRejectedCount, threatCount, now);
        }

		public void setSid(Sid sid) {
			this.sid = sid;
		}

		public void setMemoryUsage(int memoryUsage) {
			this.memoryUsage = memoryUsage;
		}

		public void setCpuUsage(int cpuUsage) {
			this.cpuUsage = cpuUsage;
		}

		public void setLiveCallsCount(int liveCallsCount) {
			this.liveCallsCount = liveCallsCount;
		}

		public void setCallRate(double callRate) {
			this.callRate = callRate;
		}

		public void setCallRejectedCount(int callRejectedCount) {
			this.callRejectedCount = callRejectedCount;
		}

		public void setThreatCount(int threatCount) {
			this.threatCount = threatCount;
		}

		

		
	}

	

}
