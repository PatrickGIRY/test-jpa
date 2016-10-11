package test.jpa;


import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.internal.SessionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PersonRepositoryTest.TestConfig.class)
public class PersonRepositoryTest {

    @Autowired
    private DatabaseConnection databaseConnection;

    @Autowired
    private  EntityManager entityManager;

    private PersonRepository personRepository;

    private EntityTransaction transaction;

    @Before
    public void setUp() throws Exception {

        personRepository = new PersonRepository(entityManager);
        transaction = entityManager.getTransaction();
        transaction.begin();;
        String dataSetName = buildDatasetFilePath();

        try(InputStream stream = getClass().getResourceAsStream(dataSetName)) {
            FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
            builder.setColumnSensing(true);
            IDataSet dataSet = builder.build(stream);
            ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
            replacementDataSet.addReplacementObject("[NOW]", new Date());
            CompositeDataSet compositeDataSet = new CompositeDataSet(new IDataSet[]{replacementDataSet});
            DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, compositeDataSet);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load dataset " + dataSetName, e);
        }
    }

    @After
    public void tearDown() throws Exception {
        transaction.rollback();
    }

    private String buildDatasetFilePath() {
        return "./" + this.getClass().getSimpleName() + "Test-dataset.xml";
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


    @Configuration
    public static class TestConfig {
        private static final String JDBC_DRIVER = "org.h2.Driver";
        private static final String JDBC_URL = "jdbc:h2:mem:dbunit2-test;MODE=MSSQLServer;INIT=create schema IF NOT EXISTS person";
        private static final String PERSISTENCE_UNIT_NAME = "test";

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            Map<String, String> databaseProperties = new HashMap<>();
            databaseProperties.put("javax.persistence.jdbc.driver", JDBC_DRIVER);
            databaseProperties.put("javax.persistence.jdbc.url", JDBC_URL);
            databaseProperties.put("hibernate.dialect", "org.hibernate.dialect.SQLServer2005Dialect");
            databaseProperties.put("hibernate.hbm2ddl.auto", "create");
            databaseProperties.put("hibernate.show_sql", "false");

            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, databaseProperties);
        }

        @Bean
        public EntityManager entityManager() {
            return entityManagerFactory().createEntityManager();
        }

        @Bean
        public DatabaseConnection connectionToDatabase(EntityManager entityManager) {

            try {
                DatabaseConnection connection = new DatabaseConnection(((SessionImpl) (entityManager.getDelegate())).connection());
                connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
                connection.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, Boolean.TRUE);
                return connection;
            } catch (DatabaseUnitException e) {
                throw new AssertionError("Cannot connect to the database", e);
            }

        }


    }
}
