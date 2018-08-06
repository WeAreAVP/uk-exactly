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

import uk.sipperfly.persistent.FTP;

public class FTPRepo {

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
	public FTPRepo() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("UKSipperflyPU");
		this.entityManager = this.entityManagerFactory.createEntityManager();
	}

	/**
	 * save ftp settings
	 *
	 * @param ftpSettings
	 * @return
	 */
	public FTP save(FTP ftpSettings) {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(ftpSettings);
		this.entityManager.getTransaction().commit();
		this.entityManager.refresh(ftpSettings);
		return ftpSettings;
	}

	/**
	 * Get existing ftp settings or create new object.
	 *
	 * @return
	 */
	public FTP getOneOrCreateOne() {
		FTP ftpSettings;
		TypedQuery<FTP> query = this.entityManager.createQuery("SELECT ftp FROM FTP ftp", FTP.class);
		try {
			ftpSettings = query.getSingleResult();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
			ftpSettings = new FTP();
		}
		return ftpSettings;

	}

	/**
	 * truncate BagInfo FTP
	 */
	public void truncate() {
		EntityManager em = this.entityManager;
		em.getTransaction().begin();
		em.createNativeQuery("truncate table FTP").executeUpdate();
		em.getTransaction().commit();
	}
}
