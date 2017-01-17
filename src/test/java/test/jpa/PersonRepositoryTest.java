package test.jpa;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.jpa.config.TestConfig;
import test.jpa.util.DBUnitForJpaHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, PersonRepository.class})
public class PersonRepositoryTest {

    @Autowired
    private DBUnitForJpaHelper dbUnitForJpaHelper;

    @Autowired
    private PersonRepository personRepository;

    @Before
    public void setUp() throws Exception {
        dbUnitForJpaHelper.initTestDatabase(this.getClass());
    }

    @Test
    public void should_return_person_of_id_1() throws Exception {
       Person person = personRepository.getById(1L);
       assertThat(person).isNotNull();
    }

    @Test
    public void should_return_the_persons_named_Durant() throws Exception {
          List<Person> personsFound = personRepository.findByName("Durant");
          assertThat(personsFound).isNotEmpty();
    }

}
