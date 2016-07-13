package org.restcomm.sbc.bean;

import java.io.Serializable;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.CDR;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.dao.CDRDao;
import org.restcomm.sbc.dao.DaoManager;



@ManagedBean(name = "cdrs")
@SessionScoped
public class CDRBean implements Serializable {


	private static final long serialVersionUID = 5477678601931196965L;
	private static final Logger LOG = Logger.getLogger(CDRBean.class);
	
	public List<CDR> getCdrs() {
		final ShiroResources services = ShiroResources.getInstance();
        final DaoManager daos = services.get(DaoManager.class);
        final CDRDao cdr = daos.getCDRDao();
       
        return cdr.getCDRs();
	}

	

}