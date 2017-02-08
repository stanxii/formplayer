package application;

import annotations.NoLogging;
import annotations.UserLock;
import auth.DjangoAuth;
import beans.*;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hq.CaseAPIs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.schema.JSONReporter;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import util.Constants;

import java.io.StringReader;

/**
 * Controller class (API endpoint) containing all all logic that isn't associated with
 * a particular form session or menu navigation. Includes:
 *      Get Cases
 *      Filter Cases
 *      Sync User DB
 *      Get Sessions (Incomplete Forms)
 */
@Api(value = "Util Controller", description = "Operations that aren't associated with form or menu navigation")
@RestController
@EnableAutoConfiguration
public class UtilController extends AbstractBaseController {

    @ApiOperation(value = "Sync the user's database with the server")
    @RequestMapping(value = Constants.URL_SYNC_DB, method = RequestMethod.POST)
    @UserLock
    public SyncDbResponseBean syncUserDb(@RequestBody SyncDbRequestBean syncRequest,
                                         @CookieValue(Constants.POSTGRES_DJANGO_SESSION_ID) String authToken) throws Exception {
        restoreFactory.configure(syncRequest, new DjangoAuth(authToken));

        if (syncRequest.isPreserveCache()) {
            CaseAPIs.restoreIfNotExists(restoreFactory, false);
        } else {
            CaseAPIs.forceRestore(restoreFactory);
        }

        return new SyncDbResponseBean();
    }

    @ApiOperation(value = "Wipe the applications databases")
    @RequestMapping(value = Constants.URL_DELETE_APPLICATION_DBS, method = RequestMethod.POST)
    @UserLock
    public NotificationMessageBean deleteApplicationDbs(
            @RequestBody DeleteApplicationDbsRequestBean deleteRequest) {

        String message = "Successfully cleared application database for " + deleteRequest.getAppId();
        boolean success = deleteRequest.clear();
        if (success) {
            message = "Failed to clear application database for " + deleteRequest.getAppId();
        }
        return new NotificationMessageBean(message, !success);
    }

    @ApiOperation(value = "Gets the status of the Formplayer service")
    @RequestMapping(value = Constants.URL_SERVER_UP, method = RequestMethod.GET)
    public ServerUpBean serverUp() throws Exception {
        return new ServerUpBean();
    }

    @RequestMapping(value = "enikcal", method = RequestMethod.POST)
    public String enikResponse() throws Exception {
        return "{\n" +
                "  \"notification\": {\n" +
                "    \"message\": null,\n" +
                "    \"error\": false\n" +
                "  },\n" +
                "  \"title\": \"Record Adherence\",\n" +
                "  \"clearSession\": false,\n" +
                "  \"appId\": \"9df7f5b6f37e5a6d2401fca01272194b\",\n" +
                "  \"appVersion\": \"CommCare Version: 2.32, App Version: 463\",\n" +
                "  \"locales\": [\n" +
                "    \"default\",\n" +
                "    \"en\",\n" +
                "    \"hin\",\n" +
                "    \"guj\"\n" +
                "  ],\n" +
                "  \"breadcrumbs\": [\n" +
                "    \"eNikshay\",\n" +
                "    \"Record Adherence\",\n" +
                "    \"Record Adherence\"\n" +
                "  ],\n" +
                "  \"menuSessionId\": \"a676fa7c-9976-49aa-9947-2e4045b25338\",\n" +
                "  \"persistentCaseTile\": {\n" +
                "    \"details\": [\n" +
                "      \"hiv-positive-referral- same-district\"\n" +
                "    ],\n" +
                "    \"styles\": [\n" +
                "      {\n" +
                "        \"fontSize\": 12,\n" +
                "        \"widthHint\": null,\n" +
                "        \"displayFormat\": null\n" +
                "      }\n" +
                "    ],\n" +
                "    \"headers\": [\n" +
                "      \"\"\n" +
                "    ],\n" +
                "    \"title\": \"Details\",\n" +
                "    \"usesCaseTiles\": true,\n" +
                "    \"maxWidth\": 12,\n" +
                "    \"maxHeight\": 1,\n" +
                "    \"numEntitiesPerRow\": 1,\n" +
                "    \"tiles\": [\n" +
                "      {\n" +
                "        \"gridX\": 0,\n" +
                "        \"gridY\": 0,\n" +
                "        \"gridWidth\": 12,\n" +
                "        \"gridHeight\": 1,\n" +
                "        \"cssId\": null,\n" +
                "        \"fontSize\": \"large\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"useUniformUnits\": false\n" +
                "  },\n" +
                "  \"entities\": [\n" +
                "    {\n" +
                "      \"id\": \"17174\",\n" +
                "      \"data\": [\n" +
                "        \"8\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17175\",\n" +
                "      \"data\": [\n" +
                "        \"9\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17176\",\n" +
                "      \"data\": [\n" +
                "        \"10\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17177\",\n" +
                "      \"data\": [\n" +
                "        \"11\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17178\",\n" +
                "      \"data\": [\n" +
                "        \"12\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17179\",\n" +
                "      \"data\": [\n" +
                "        \"13\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17180\",\n" +
                "      \"data\": [\n" +
                "        \"14\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17181\",\n" +
                "      \"data\": [\n" +
                "        \"15\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17182\",\n" +
                "      \"data\": [\n" +
                "        \"16\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17183\",\n" +
                "      \"data\": [\n" +
                "        \"17\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17184\",\n" +
                "      \"data\": [\n" +
                "        \"18\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17185\",\n" +
                "      \"data\": [\n" +
                "        \"19\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17186\",\n" +
                "      \"data\": [\n" +
                "        \"20\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17187\",\n" +
                "      \"data\": [\n" +
                "        \"21\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17188\",\n" +
                "      \"data\": [\n" +
                "        \"22\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17189\",\n" +
                "      \"data\": [\n" +
                "        \"23\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17190\",\n" +
                "      \"data\": [\n" +
                "        \"24\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17191\",\n" +
                "      \"data\": [\n" +
                "        \"25\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17192\",\n" +
                "      \"data\": [\n" +
                "        \"26\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17193\",\n" +
                "      \"data\": [\n" +
                "        \"27\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17194\",\n" +
                "      \"data\": [\n" +
                "        \"28\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17195\",\n" +
                "      \"data\": [\n" +
                "        \"29\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17196\",\n" +
                "      \"data\": [\n" +
                "        \"30\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17197\",\n" +
                "      \"data\": [\n" +
                "        \"31\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17198\",\n" +
                "      \"data\": [\n" +
                "        \"1\",\n" +
                "        \"Feb\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17199\",\n" +
                "      \"data\": [\n" +
                "        \"2\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17200\",\n" +
                "      \"data\": [\n" +
                "        \"3\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17201\",\n" +
                "      \"data\": [\n" +
                "        \"4\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17202\",\n" +
                "      \"data\": [\n" +
                "        \"5\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17203\",\n" +
                "      \"data\": [\n" +
                "        \"6\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"17204\",\n" +
                "      \"data\": [\n" +
                "        \"<b><u>7</u></b>\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\",\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"details\": null\n" +
                "    }\n" +
                "  ],\n" +
                "  \"actions\": [],\n" +
                "  \"styles\": [\n" +
                "    {\n" +
                "      \"fontSize\": 12,\n" +
                "      \"widthHint\": null,\n" +
                "      \"displayFormat\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"fontSize\": 12,\n" +
                "      \"widthHint\": null,\n" +
                "      \"displayFormat\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"fontSize\": 0,\n" +
                "      \"widthHint\": null,\n" +
                "      \"displayFormat\": \"Image\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"fontSize\": 0,\n" +
                "      \"widthHint\": null,\n" +
                "      \"displayFormat\": \"Image\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"fontSize\": 0,\n" +
                "      \"widthHint\": null,\n" +
                "      \"displayFormat\": \"Image\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"headers\": [\n" +
                "    \"Date\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"\"\n" +
                "  ],\n" +
                "  \"tiles\": [\n" +
                "    {\n" +
                "      \"gridX\": 5,\n" +
                "      \"gridY\": 0,\n" +
                "      \"gridWidth\": 6,\n" +
                "      \"gridHeight\": 2,\n" +
                "      \"cssId\": null,\n" +
                "      \"fontSize\": \"medium\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"gridX\": 0,\n" +
                "      \"gridY\": 0,\n" +
                "      \"gridWidth\": 5,\n" +
                "      \"gridHeight\": 1,\n" +
                "      \"cssId\": null,\n" +
                "      \"fontSize\": \"small\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"gridX\": 0,\n" +
                "      \"gridY\": 4,\n" +
                "      \"gridWidth\": 7,\n" +
                "      \"gridHeight\": 7,\n" +
                "      \"cssId\": null,\n" +
                "      \"fontSize\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"gridX\": 8,\n" +
                "      \"gridY\": 5,\n" +
                "      \"gridWidth\": 3,\n" +
                "      \"gridHeight\": 3,\n" +
                "      \"cssId\": null,\n" +
                "      \"fontSize\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"gridX\": 8,\n" +
                "      \"gridY\": 8,\n" +
                "      \"gridWidth\": 3,\n" +
                "      \"gridHeight\": 3,\n" +
                "      \"cssId\": null,\n" +
                "      \"fontSize\": null\n" +
                "    }\n" +
                "  ],\n" +
                "  \"widthHints\": [\n" +
                "    20,\n" +
                "    20,\n" +
                "    20,\n" +
                "    20,\n" +
                "    20\n" +
                "  ],\n" +
                "  \"numEntitiesPerRow\": 7,\n" +
                "  \"useUniformUnits\": true,\n" +
                "  \"pageCount\": 0,\n" +
                "  \"currentPage\": 0,\n" +
                "  \"type\": \"entities\",\n" +
                "  \"usesCaseTiles\": true,\n" +
                "  \"maxWidth\": 11,\n" +
                "  \"maxHeight\": 11\n" +
                "}";
    }

    @ApiOperation(value = "Validates an XForm")
    @NoLogging
    @RequestMapping(
        value = Constants.URL_VALIDATE_FORM,
        method = RequestMethod.POST,
        produces = { MediaType.APPLICATION_JSON_VALUE },
        consumes = { MediaType.APPLICATION_XML_VALUE}
    )
    public String validateForm(@RequestBody String formXML) throws Exception {
        JSONReporter reporter = new JSONReporter();
        try {
            XFormParser parser = new XFormParser(new StringReader(formXML));
            parser.attachReporter(reporter);
            parser.parse();
            reporter.setPassed();
        } catch (XFormParseException xfpe) {
            reporter.setFailed(xfpe);
        } catch (Exception e) {
            reporter.setFailed(e);
        }

        return reporter.generateJSONReport();
    }
}
