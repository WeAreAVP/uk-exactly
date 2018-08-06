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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import uk.sipperfly.persistent.BagInfo;

/**
 * BagInfoRepo Repository
 *
 * @author noumantayyab
 */
public class BagInfoRepo {

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
	public BagInfoRepo() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("UKSipperflyPU");
		this.entityManager = this.entityManagerFactory.createEntityManager();
	}

	/**
	 * Save the bag info.
	 *
	 * @param bagInfo
	 * @return
	 */
	public BagInfo save(BagInfo bagInfo) {
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
	public boolean delete(BagInfo bagInfo) {
		this.entityManager.getTransaction().begin();
		this.entityManager.remove(bagInfo);
		this.entityManager.getTransaction().commit();
		return true;
	}

	/**
	 * Get list of existing bagInfo.
	 *
	 * @return BagInfo
	 */
	public List<BagInfo> getOneOrCreateOne() {
		List<BagInfo> bagInfoNew = null;
		TypedQuery<BagInfo> query = this.entityManager.createQuery("SELECT bi FROM BagInfo bi", BagInfo.class);
		try {
			bagInfoNew = query.getResultList();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
		}
		return bagInfoNew;
	}

	/**
	 * delete record by id
	 *
	 * @param delId
	 * @return
	 */
	public boolean deleteRecord(int delId) {
		String selectQuery = "SELECT bi FROM BagInfo bi";
		List<BagInfo> bagsToRemove = entityManager.createQuery(selectQuery).getResultList();
		for (BagInfo m : bagsToRemove) {
			if (m.getId() == delId) {
				this.delete(m);
			}
		}
		return true;
	}

	/**
	 * get record by id
	 *
	 * @param id
	 * @return
	 */
	public BagInfo getOneRecord(int id) {
		BagInfo bagInfo = null;
		TypedQuery<BagInfo> query = this.entityManager.createQuery("SELECT bi FROM BagInfo bi WHERE bi.id = " + id, BagInfo.class);
		try {
			bagInfo = query.getSingleResult();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
		}
		return bagInfo;
	}

	/**
	 * truncate BagInfo table
	 */
	public void truncate() {
		EntityManager em = this.entityManager;
		em.getTransaction().begin();
		em.createNativeQuery("truncate table BagInfo").executeUpdate();
		em.getTransaction().commit();
	}

	/**
	 *
	 * @param delId
	 * @return
	 */
	public boolean deleteRecordById(String delId) {
		String selectQuery = "SELECT bi FROM BagInfo bi WHERE bi.id NOT IN " + delId;
		List<BagInfo> bagsToRemove = entityManager.createQuery(selectQuery).getResultList();
		for (BagInfo m : bagsToRemove) {
			this.delete(m);
		}
		return true;
	}
}
