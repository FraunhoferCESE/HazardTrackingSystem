package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.OneToOne;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.NotNull;

/**
 * The table definition for the Hazard_Cause table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazard_Causes")
public interface Hazard_Causes extends Entity {
	String getCauseNumber();
	void setCauseNumber(String causeNumber);
	
	@NotNull
	@StringLength(value = 512)
	String getTitle();
	void setTitle(String title);

	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();
	void setDescription(String description);
	
	String getOwner();
	void setOwner(String owner);
	
	String getEffects();
	void setEffects(String effects);
	
	Date getLastUpdated();
	void setLastUpdated(Date lastEdit);
	
	Date getOriginalDate();
	void setOriginalDate(Date originalDate);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getDeleteReason();
	void setDeleteReason(String reason);
	
	@ManyToMany(value = Causes_to_Hazards.class)
	Hazards[] getHazards();
	
	@OneToOne
	Transfers getTransfer();
}
