package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;
import net.java.ao.schema.NotNull;

//TODO look into if skipping preload is better writing javadoc
/**
 * 
 * @author ASkulason
 *
 */
@Preload
@Table("Hazards")
//TODO Enable the rest of the fields and get them working with the HTML page
public interface Hazards extends Entity {
/*	@NotNull
	@Unique
	String getNumber();
	void setNumber(String number);
	*/
	@NotNull
	String getTitle();
	void setTitle(String title);
	/*
	String getDescription();
	void setDescription(String description);
	
	String getPreparer();
	void setPreparer(String preparer);
	
	Date getInitiationDate();
	void setInitiationDate(Date created);
	
	Date getRevisionDate();
	void setRevisionDate(Date lastEdit);
	
	Date getCompletionDate();
	void setCommpletionDate(Date completed);
	*/
	//TODO FOREIGN KEYS
}
