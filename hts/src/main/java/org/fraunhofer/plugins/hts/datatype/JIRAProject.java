package org.fraunhofer.plugins.hts.datatype;

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
