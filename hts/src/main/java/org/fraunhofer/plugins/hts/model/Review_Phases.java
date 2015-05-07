package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("Review_Phases")
public interface Review_Phases extends Entity {

	String getLabel();

	void setLabel(String label);

	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();

	void setDescription(String description);

	@OneToOne
	Hazards getHazards();
}
