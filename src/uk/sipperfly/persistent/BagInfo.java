/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.persistent;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * BagInfo Entity
 *
 * @author noumantayyab
 */
@Entity
public class BagInfo implements Serializable {
private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;	
	private String label;
	
	@Lob
	private String value;

	/**
	 * Get ID.
	 *
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set ID.
	 *
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Set label
	 *
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set label
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get value.
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set value
	 *
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}


	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof BagInfo)) {
			return false;
		}
		BagInfo other = (BagInfo) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "my.persistent.BagInfo[ id=" + id + " ]";
	}

}
