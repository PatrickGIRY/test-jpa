package test.jpa.util;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;

@Service
public class DBUnitForJpaHelper {

    @PersistenceContext(unitName = "basePU")
    private EntityManager entityManager;

    private static String buildDatasetFilePath(Class<?> testClass) {
        return "./" + testClass.getSimpleName() + "-dataset.xml";
    }

    @Transactional("transactionManager")
    public void initTestDatabase(Class<?> testClass) {
        try {
            Connection connection = entityManager.unwrap(SessionImpl.class).connection();
            DatabaseConnection databaseConnection = new DatabaseConnection(connection);
            DatabaseConfig databaseConfig = databaseConnection.getConfig();
            databaseConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
            databaseConfig.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
            databaseConfig.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, false);

            String dataSetName = buildDatasetFilePath(testClass);

            try (InputStream stream = testClass.getResourceAsStream(dataSetName)) {
                FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
                builder.setColumnSensing(true);
                builder.setCaseSensitiveTableNames(false);
                IDataSet dataSet = builder.build(stream);
                ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
                replacementDataSet.addReplacementObject("[NOW]", new Date());
                CompositeDataSet compositeDataSet = new CompositeDataSet(new IDataSet[]{replacementDataSet});
                DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, compositeDataSet);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load dataset " + dataSetName, e);
            }

        } catch (DatabaseUnitException e) {
            throw new AssertionError("Cannot connect to the database", e);
        }
    }
}
