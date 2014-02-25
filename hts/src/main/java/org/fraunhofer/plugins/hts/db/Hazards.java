package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
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
//TODO restrictions
public interface Hazards extends Entity {
	
	//@NotNull
	String getTitle();
	void setTitle(String title);
	
	String getHazardDesc();
	void setHazardDesc(String description);
	
	String getPreparer();
	void setPreparer(String preparer);
	
	String getHazardNum();
	void setHazardNum(String number);
	
	Date getInitiationDate();
	void setInitiationDate(Date created);
	
	Date getRevisionDate();
	void setRevisionDate(Date lastEdit);
	
	Date getCompletionDate();
	void setCompletionDate(Date completed);
	
	//TODO FOREIGN KEYS
}
