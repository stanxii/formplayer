package tests;

import beans.menus.CommandListResponseBean;
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
public class ICDSTests extends BaseTestClass{

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configureRestoreFactory("icdsdomain", "icdsusername");
    }

    @Override
    protected String getMockRestoreFileName() {
        return "restores/icds.xml";
    }

    @Test
    public void testReportModule() throws Exception {
        sessionNavigate(new String[]{"2"}, "icds", CommandListResponseBean.class);
    }
}
