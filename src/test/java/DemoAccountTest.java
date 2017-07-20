
import amosalexa.AccountFactory;
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
        dynamoDbMapper.dropTable(ContactDB.class);
        dynamoDbMapper.dropTable(StandingOrderDB.class);
        dynamoDbMapper.dropTable(model.db.User.class);

        dynamoDbMapper.createTable(AccountDB.class);
        dynamoDbMapper.createTable(Category.class);
        dynamoDbMapper.createTable(ContactDB.class);
        dynamoDbMapper.createTable(StandingOrderDB.class);
        dynamoDbMapper.createTable(model.db.User.class);

    }

    @Test
    public void createDemoAccount(){
       Account demoAccount = AccountFactory.getInstance().createDemo();
    }*/

}
