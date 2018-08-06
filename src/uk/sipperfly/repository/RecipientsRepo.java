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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import uk.sipperfly.persistent.Recipients;

/**
 * RecipientsRepo repository.
 *
 * @author noumantayyab
 */
public class RecipientsRepo {

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
	 * Constructor for RecipientsRepos
	 *
	 */
	public RecipientsRepo() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("UKSipperflyPU");
		this.entityManager = this.entityManagerFactory.createEntityManager();
	}

	/**
	 * Save the recipient info in database.
	 *
	 * @param recipient
	 * @return Recipients
	 */
	public Recipients save(Recipients recipient) {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(recipient);
		this.entityManager.getTransaction().commit();
		this.entityManager.refresh(recipient);
		return recipient;
	}

	/**
	 * Get all recipients email addresses.
	 *
	 * @return Recipients
	 */
	public List<Recipients> getAll() {

		TypedQuery<Recipients> query = this.entityManager.createQuery("SELECT r FROM Recipients r", Recipients.class);

		List<Recipients> recipients = query.getResultList();

		return recipients;

	}

	/**
	 * Delete email address that are no longer active.
	 *
	 * @param emails
	 */
	public void deleteInactiveEmails(List emails) {
		TypedQuery<Recipients> query = this.entityManager.createQuery("SELECT r FROM Recipients r WHERE r.email NOT IN :email", Recipients.class);
		List<Recipients> recipients = query.setParameter("email", emails).getResultList();
		if (!recipients.isEmpty()) {
			for (Recipients recipient : recipients) {
				this.delete(recipient);
			}
		}
	}

	/**
	 * delete record by id
	 *
	 * @param delId
	 * @return
	 */
	public boolean deleteRecord(int delId) {
		String selectQuery = "SELECT bi FROM Recipients bi";
		List<Recipients> emailsToRemove = entityManager.createQuery(selectQuery).getResultList();
		for (Recipients m : emailsToRemove) {
			if (m.getId() == delId) {
				this.delete(m);
			}
		}
		return true;
	}

	/**
	 * Get existing email object or create new object.
	 *
	 * @return Configurations
	 */
	public Recipients getOneOrCreateOne(String email) {
		Recipients recipient;
		TypedQuery<Recipients> query = this.entityManager.createQuery("SELECT r FROM Recipients r WHERE r.email = :email", Recipients.class);
		try {
			recipient = query.setParameter("email", email).getSingleResult();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
			recipient = new Recipients();
		}
		return recipient;

	}

	/**
	 * Delete the recipient form database.
	 *
	 * @param recipient
	 * @return boolean
	 */
	public boolean delete(Recipients recipient) {
		this.entityManager.getTransaction().begin();
		this.entityManager.remove(recipient);
		this.entityManager.getTransaction().commit();
		return true;
	}

	/**
	 * get record by id
	 *
	 * @param id
	 * @return
	 */
	public Recipients getOneRecord(int id) {
		Recipients rec = null;
		TypedQuery<Recipients> query = this.entityManager.createQuery("SELECT bi FROM Recipients bi WHERE bi.id = " + id, Recipients.class);
		try {
			rec = query.getSingleResult();

		} catch (NoResultException exception) {
			System.out.println(exception.toString());
		}
		return rec;
	}

	/**
	 * truncate BagInfo Recipients
	 */
	public void truncate() {
		EntityManager em = this.entityManager;
		em.getTransaction().begin();
		em.createNativeQuery("truncate table Recipients").executeUpdate();
		em.getTransaction().commit();
	}
}
