package tests;

import beans.NewFormResponse;
import beans.menus.EntityDetailListResponse;
import org.commcare.util.screen.CommCareSessionException;
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
public class UCRTests extends BaseTestClass{

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configureRestoreFactory("ucrdomain", "ucrusername");
    }

    @Override
    protected String getMockRestoreFileName() {
        return "restores/ucr.xml";
    }

    @Test
    public void testGetUcr() throws Throwable {
        EntityDetailListResponse detailListResponse =
                getDetails(new String[]{"2", "1", "3d7782e232135e0bc1d06233cda04642c9e67d0e"}, "ucr", EntityDetailListResponse.class);
    }
}
