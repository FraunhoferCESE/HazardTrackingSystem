package org.fraunhofer.plugins.hts.rest.datatype;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.db.Transfers;

/**
 * Wrapper class to provide serialized Transfer data for REST requests.
 * Currently, ActiveObjects Entity models, such as {@link Transfers}, cannot be
 * serialized using xml annotations.
 * 
 * @author llayman
 *
 */
@XmlRootElement(name = "transfer")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class TransferJSON {
	private final List<CauseJSON> causes;
	private final List<ControlJSON> controls;

	public TransferJSON(List<CauseJSON> causes, List<ControlJSON> controls) {
		super();
		this.causes = causes;
		this.controls = controls;
	}

	public List<CauseJSON> getCauses() {
		return causes;
	}

	public List<ControlJSON> getControls() {
		return controls;
	}

}