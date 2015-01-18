package org.fraunhofer.plugins.hts.rest.datatype;

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
	private final int originId;
	private final String originType;
	private final int targetId;
	private final String targetType;

	public TransferJSON(Transfers transfer) {
		super();
		this.originId = transfer.getOriginID();
		this.originType = transfer.getOriginType();
		this.targetId = transfer.getTargetID();
		this.targetType = transfer.getTargetType();
	}

	public int getOriginId() {
		return originId;
	}

	public String getOriginType() {
		return originType;
	}

	public int getTargetId() {
		return targetId;
	}

	public String getTargetType() {
		return targetType;
	}

}