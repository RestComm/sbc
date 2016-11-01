package org.restcomm.sbc.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.sip.ServletParseException;


public class LazyRule  {
	
	
	private HashMap<String, String> parameters=new HashMap<String, String>();
	private Rule rule;
	
	public LazyRule(Rule rule) {
		this.rule=rule;
	}
	
	@Override
	public Object clone() {
		return this.clone();
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}

	public Iterator<String> getParameterNames() {
		return parameters.values().iterator();
	}

	public Set<Entry<String, String>> getParameters() {
		return parameters.entrySet();
	}

	
	public String getValue() {
		String value="";
		for(Entry<String, String> parameter: parameters.entrySet()) {
			value+=parameter.getKey()+"="+parameter.getValue()+";";				
		}
		return value;
		
	}
	
	public String getHeaderName() {
		return rule.text;
		
	}

	
	public void removeParameter(String name) {
		parameters.remove(name);
		
	}

	
	public void setParameter(String name, String value) {
		parameters.put(name, value);
		
	}

	
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}
	
	public static LazyRule buildNATPatchRule(String host, int port) {
		LazyRule rule=new LazyRule(Rule.NAT);
		rule.setParameter("host", host);
		rule.setParameter("port",""+port);
		return rule;
		
	}
	
	public static LazyRule buildSDPPatchRule(String host, int audioPort, int videoPort) {
		LazyRule rule=new LazyRule(Rule.SDP_PATCH);
		rule.setParameter("host", host);
		rule.setParameter("a",""+audioPort);
		rule.setParameter("v",""+videoPort);
		return rule;
		
	}
	
	public static LazyRule buildContactPatchRule(String address) {
		LazyRule rule=new LazyRule(Rule.CONTACT);
		rule.setParameter("address", address);
		
		return rule;
		
	}
	public static void main(String argv[]) {
		System.out.println(LazyRule.buildNATPatchRule("10.0.0.0", 5060).getValue());
		LazyRule sdp = LazyRule.buildSDPPatchRule("10.0.0.0", 5060,999);
		System.out.println(sdp.getHeaderName()+":"+sdp.getValue());
	}
	
	public enum Rule {
		
        NAT			("X-SBC-LR-nat-patch"), 
        CONTACT		("X-SBC-LR-contact-patch"),
		SDP_PATCH	("X-SBC-LR-sdp-patch");

        private final String text;

        private Rule(final String text) {
            this.text = text;
        }

        public static Rule getValueOf(final String text) {
        	Rule[] values = values();
            for (final Rule value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid rule.");
        }

        @Override
        public String toString() {
            return text;
        }
    }

	public static LazyRule parse(Rule rule, String content) throws ServletParseException {
		HashMap<String, String> parsedParams=new HashMap<String, String>();
		LazyRule lrule=new LazyRule(rule);
		String[] tokens=content.split(";");
		for(int i=0;i<tokens.length;i++) {
			String[] pair=tokens[i].split("=");
			
			parsedParams.put(pair[0], pair[1]);
			
		}
		
		for(Entry<String, String> parameter: parsedParams.entrySet()) {
				lrule.setParameter(parameter.getKey(), parameter.getValue());				
		}		
	
		return lrule;
	}
}
