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

/**
 * Configurations Entity
 *
 * @author noumantayyab
 */
@Entity
public class Configurations implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	/**
	 * Server name for smtp host.
	 *
	 */
	private String serverName = "smtp.gmail.com";
	
	/**
	 * Server name for smtp port.
	 *
	 */
	private String serverPort = "587";
		/**
	 * Server name for smtp protocol.
	 *
	 */
	private String serverProtocol = "TLS";
	
	/**
	 * username for email settings
	 */
	private String username;
	/**
	 * password for email settings.
	 */
	private String password;
	/**
	 * Drop location path.
	 */
	private String dropLocation;
	
	private int size = 200;
	/**
	 * Ignore filters
	 */
	private String filters;
	/**
	 * Email notifications
	 */
	private boolean emailNotifications = false;

	/**
	 * Get Email notifications
	 *
	 * @return
	 */
	public boolean getEmailNotifications() {
		return emailNotifications;
	}

	/**
	 * Set email notifications
	 *
	 * @param emailNotifications
	 */
	public void setEmailNotifications(boolean emailNotifications) {
		this.emailNotifications = emailNotifications;
	}

	/**
	 * Get Drop Location
	 *
	 * @return
	 */
	public String getDropLocation() {
		return dropLocation;
	}

	/**
	 * Set Drop Location
	 *
	 * @param dropLocation
	 */
	public void setDropLocation(String dropLocation) {
		this.dropLocation = dropLocation;
	}

	/**
	 * Get Drop Location
	 *
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Set Drop Location
	 *
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Get Filters
	 *
	 * @return
	 */
	public String getFilters() {
		return filters;
	}

	/**
	 * Set Filters
	 *
	 * @param filters
	 */
	public void setFilters(String filters) {
		this.filters = filters;
	}

	/**
	 * Get Server Name
	 *
	 * @return
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Set Server Name
	 *
	 * @param serverName
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * Get Username
	 *
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set Username
	 *
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get Password
	 *
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set Password
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * Get Server Port
	 *
	 * @return
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * Set Server Port
	 *
	 * @param serverPort
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	/**
	 * Get Server Protocol
	 *
	 * @return
	 */
	public String getServerProtocol() {
		return serverProtocol;
	}

	/**
	 * Set Server Protocol
	 *
	 * @param serverProtocol
	 */
	public void setServerProtocol(String serverProtocol) {
		this.serverProtocol = serverProtocol;
	}

	/**
	 * Get ID
	 *
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set ID
	 *
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
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
		if (!(object instanceof Configurations)) {
			return false;
		}
		Configurations other = (Configurations) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "persistent.Configurations[ id=" + id + " ]";
	}

}
