/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.5
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
import uk.sipperfly.persistent.DefaultTemplate;

/**
 *
 * @author rimsha@geeks
 */
public class DefaultTemplateRepo {
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
	public DefaultTemplateRepo() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("UKSipperflyPU");
		this.entityManager = this.entityManagerFactory.createEntityManager();
	}

	/**
	 * Save the bag info.
	 *
	 * @param bagInfo
	 * @return
	 */
	public DefaultTemplate save(DefaultTemplate bagInfo) {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(bagInfo);
		this.entityManager.getTransaction().commit();
		this.entityManager.refresh(bagInfo);
		return bagInfo;
	}

	/**
	 * delete bagInfo
	 * 
	 * @param bagInfo
	 * @return 
	 */
	public boolean delete(DefaultTemplate bagInfo) {
		this.entityManager.getTransaction().begin();
		this.entityManager.remove(bagInfo);
		this.entityManager.getTransaction().commit();
		return true;
	}

	/**
	 * Get list of existing bagInfo.
	 *
	 * @return DefaultTemplate
	 */
	public DefaultTemplate getOneOrCreateOne() {
		DefaultTemplate defaultTemp;
		TypedQuery<DefaultTemplate> query = this.entityManager.createQuery("SELECT c FROM DefaultTemplate c", DefaultTemplate.class);
		try {
			defaultTemp = query.getSingleResult();
 
		} catch (NoResultException exception) {
			System.out.println(exception.toString());
			defaultTemp = new DefaultTemplate();
		}
		return defaultTemp;
	}

	/**
	 * truncate BagInfo table
	 */
	public void truncate() {
		EntityManager em = this.entityManager;
		em.getTransaction().begin();
		em.createNativeQuery("truncate table DefaultTemplate").executeUpdate();
		em.getTransaction().commit();
	}
}
