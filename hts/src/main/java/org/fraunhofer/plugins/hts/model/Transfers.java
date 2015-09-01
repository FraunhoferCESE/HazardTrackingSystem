package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("Transfers")
public interface Transfers extends Entity {

	public enum TransferType {
		HAZARD("HAZARD"), CAUSE("CAUSE"), CONTROL("CONTROL"), VERIFICATION("VERIFICATION");

		private String code;

		private TransferType(String c) {
			code = c;
		}

		public String getCode() {
			return code;
		}
	}

	@NotNull
	public int getOriginID();

	void setOriginID(int originID);

	@NotNull
	public String getOriginType();

	void setOriginType(String originType);

	@NotNull
	public int getTargetID();

	void setTargetID(int targetID);

	@NotNull
	public String getTargetType();

	void setTargetType(String targetType);

}
