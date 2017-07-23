
import amosalexa.AccountFactory;
import api.aws.DynamoDbClient;
import api.aws.DynamoDbMapper;
import model.banking.Account;
import model.db.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DemoAccountTest {

    private static DynamoDbMapper dynamoDbMapper = DynamoDbMapper.getInstance();


    /*@BeforeClass
    public static void createTables() throws InterruptedException {
        // Drop and recreate tables

        dynamoDbMapper.dropTable(AccountDB.class);
        dynamoDbMapper.dropTable(Category.class);
        dynamoDbMapper.dropTable(Contact.class);
        dynamoDbMapper.dropTable(DynamoTestObject.class);
        dynamoDbMapper.dropTable(Spending.class);
        dynamoDbMapper.dropTable(StandingOrderDB.class);
        dynamoDbMapper.dropTable(TransactionDB.class);
        dynamoDbMapper.dropTable(model.db.User.class);
        dynamoDbMapper.dropTable(LastIds.class);


        dynamoDbMapper.createTable(AccountDB.class);
        dynamoDbMapper.createTable(Category.class);
        dynamoDbMapper.createTable(Contact.class);
        dynamoDbMapper.createTable(DynamoTestObject.class);
        dynamoDbMapper.createTable(Spending.class);
        dynamoDbMapper.createTable(StandingOrderDB.class);
        dynamoDbMapper.createTable(TransactionDB.class);
        dynamoDbMapper.createTable(model.db.User.class);
        dynamoDbMapper.createTable(LastIds.class);
    }

    @Test
    public void createDemoAccount(){
       //Account demoAccount = AccountFactory.getInstance().createDemo();
    }*/

}
