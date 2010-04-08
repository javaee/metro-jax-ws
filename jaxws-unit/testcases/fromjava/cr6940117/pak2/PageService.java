package fromjava.cr6940117.pak2;

import fromjava.cr6940117.pak2.bean.Page;

import javax.jws.WebMethod;

@javax.jws.WebService(serviceName = "PageService", targetNamespace = "http://namespace1", portName = "PageServicePort")
public class PageService {

	@WebMethod
	public Page getPage(String page_id, String client_id) {
		Page p = new Page();
        p.setIsEmailLinkRequired("yes");
        return p;
	}
}
