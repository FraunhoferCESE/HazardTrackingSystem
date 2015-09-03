package org.fraunhofer.plugins.hts.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fraunhofer.plugins.hts.model.Transfers;

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
	private final List<VerificationJSON> verifications;

	public TransferJSON(List<CauseJSON> causes, List<ControlJSON> controls, List<VerificationJSON> verifications) {
		super();
		this.causes = causes;
		this.controls = controls;
		this.verifications = verifications;
	}

	public List<CauseJSON> getCauses() {
		return causes;
	}

	public List<ControlJSON> getControls() {
		return controls;
	}
	
	public List<VerificationJSON> getVerifications() {
		return verifications;
	}

}