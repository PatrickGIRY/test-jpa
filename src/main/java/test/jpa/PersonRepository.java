package test.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional("transactionManager")
public class PersonRepository {

    @PersistenceContext(unitName = "basePU")
    private EntityManager entityManager;

    public List<Person> findByName(String firstName) {
        TypedQuery<Person> query = entityManager.createQuery("select person from Person person where person.firstName like :firstName", Person.class);
        query.setParameter("firstName", firstName  + "%");
        return query.getResultList();
    }

    public Person getById(long id) {
        return entityManager.find(Person.class, id);
    }
}
