package tests;

import beans.EvaluateXPathResponseBean;
import beans.NewFormResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import utils.TestContext;

/**
 * Regression tests for fixed behaviors
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class JrInsertTest extends BaseTestClass{

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configureRestoreFactory("jrinsertdomain", "jrinsertusername");
    }

    @Override
    protected String getMockRestoreFileName() {
        return "restores/jrinsert.xml";
    }

    @Test
    public void testJrInsert() throws Throwable {
        NewFormResponse newFormResponse = sessionNavigate(new String[]{"1", "bc750e701e064a02b92a449faad2c0d8"}, "jrinsert", null, NewFormResponse.class);
        String sessionId = newFormResponse.getSessionId();
        answerQuestionGetResult("0", "2017-03-01", sessionId);
        answerQuestionGetResult("1", "[1]", sessionId);
        answerQuestionGetResult("2", "[1]", sessionId);
        EvaluateXPathResponseBean eval = evaluateXPath(sessionId, "/data/products/item/received/transfer/entry/@id");
        System.out.println("Eval " + eval);
    }
}
