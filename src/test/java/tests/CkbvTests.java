package tests;

import beans.NewFormResponse;
import beans.SubmitResponseBean;
import beans.menus.CommandListResponseBean;
import beans.menus.EntityListResponse;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import utils.TestContext;

import java.util.LinkedHashMap;

/**
 * Tests specific to Enikshay
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class CkbvTests extends BaseTestClass {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configureRestoreFactory("ckbvdomain", "ckbvusername");
    }

    @Override
    protected String getMockRestoreFileName() {
        return "restores/ckbv.xml";
    }

    @Test
    public void testEndOfFormNavigation() throws Exception {
        NewFormResponse newFormResponse =
                sessionNavigate(
                        new String[]{"0", "ff17fb9d-96d4-485f-9cca-eaa225c6d748", "0", "0"},
                        "ckbv",
                        NewFormResponse.class);
        SubmitResponseBean submitResponse =
                submitForm("requests/submit/submit_ckbv_2.json", newFormResponse.getSessionId());
        LinkedHashMap responseRaw = (LinkedHashMap) submitResponse.getNextScreen();
        String jsonString = new JSONObject(responseRaw).toString();

        NewFormResponse secondForm = mapper.readValue(jsonString, NewFormResponse.class);
        SubmitResponseBean submitResponse2 =
                submitForm("requests/submit/submit_ckbv_3.json", secondForm.getSessionId());

        LinkedHashMap responseRaw2 = (LinkedHashMap) submitResponse2.getNextScreen();
        String jsonString2 = new JSONObject(responseRaw2).toString();
        CommandListResponseBean commandResponse = mapper.readValue(jsonString2, CommandListResponseBean.class);
        NewFormResponse thirdResponse = sessionNavigateWithId(new String[] {"1", "0"},
                commandResponse.getMenuSessionId(),
                NewFormResponse.class);

    }
}
