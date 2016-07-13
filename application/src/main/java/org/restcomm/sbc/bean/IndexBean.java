package org.restcomm.sbc.bean;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ViewScoped
public class IndexBean implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1658126672590975833L;
	private Subject subject = SecurityUtils.getSubject();

    public Subject getSubject() {
        return subject;
    }
}