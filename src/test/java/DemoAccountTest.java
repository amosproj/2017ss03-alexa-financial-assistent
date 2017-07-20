
import api.aws.DynamoDbMapper;
import model.db.Category;
import org.junit.Before;
import org.junit.Test;

public class DemoAccountTest {

    private static DynamoDbMapper dynamoDbMapper = DynamoDbMapper.getInstance();


    @Before
    public void createTables() throws InterruptedException {

        /**
         * properly drop old tables first
          */

        /*
        dynamoDbMapper.createTable(AccountDB.class);
        dynamoDbMapper.createTable(CategoryDB.class);
        dynamoDbMapper.createTable(ContactDB.class);
        dynamoDbMapper.createTable(StandingOrderDB.class);
        */
    }


    @Test
    public void createDemoAccount(){
       //Account demoAccount = AccountFactory.getInstance().createDemo();
    }

}
