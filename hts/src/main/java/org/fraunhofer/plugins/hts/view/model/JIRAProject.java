package org.fraunhofer.plugins.hts.view.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jiraProject")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class JIRAProject {
	private Long ID;
	private String name;
	
	public JIRAProject(Long ID, String name) {
		this.ID = ID;
		this.name = name;
	}
	
	public Long getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}

}