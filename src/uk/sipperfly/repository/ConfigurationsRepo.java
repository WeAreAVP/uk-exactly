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
package uk.sipperfly.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import uk.sipperfly.persistent.Configurations;

/**
 * ConfigurationRepo repository.
 *
 * @author noumantayyab
 */
public class ConfigurationsRepo {

	/**
	 * EntityManagerFactory.
	 *
	 */
	EntityManagerFactory entityManagerFactory;
	/**
	 * Entity Manager.
	 */
	EntityManager entityManager;

	/**
	 * Constructor for EmailSettingsRepo
	 *
	 */
	public ConfigurationsRepo() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("UKSipperflyPU");
		this.entityManager = this.entityManagerFactory.createEntityManager();
	}

	/**
	 * Save the email settings.
	 *
	 * @param emailSettings
	 * @return
	 */
	public Configurations save(Configurations emailSettings) {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(emailSettings);
		this.entityManager.getTransaction().commit();
		this.entityManager.refresh(emailSettings);
		return emailSettings;
	}

	/**
	 * Get existing email settings or create new object.
	 *
	 * @return Configurations
	 */
	public Configurations getOneOrCreateOne() {
		Configurations emailSettings;
		TypedQuery<Configurations> query
				= this.entityManager.createQuery("SELECT c FROM Configurations c", Configurations.class);
		try {
			emailSettings = query.getSingleResult();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
			emailSettings = new Configurations();
		}
		return emailSettings;

	}
	
	/**
	 * truncate BagInfo Configurations
	 */
	public void truncate() {
		EntityManager em = this.entityManager;
		em.getTransaction().begin();
		em.createNativeQuery("truncate table Configurations").executeUpdate();
		em.getTransaction().commit();
	}
}
