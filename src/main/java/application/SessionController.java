package application;

import auth.DjangoAuth;
import beans.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import hq.CaseAPIs;
import objects.SerializableSession;
import objects.SessionList;
import org.apache.commons.logging.LogFactory;
import org.commcare.api.json.AnswerQuestionJson;
import org.commcare.modern.process.FormRecordProcessorHelper;
import org.commcare.suite.model.MenuDisplayable;
import org.commcare.util.cli.MenuScreen;
import org.commcare.util.cli.Screen;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import repo.MenuRepo;
import repo.SessionRepo;
import requests.InstallRequest;
import requests.NewFormRequest;
import services.RestoreService;
import services.XFormService;
import session.FormEntrySession;
import org.apache.commons.logging.Log;
import session.MenuSession;
import util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by willpride on 1/12/16.
 */
@RestController
@EnableAutoConfiguration
public class SessionController {

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private XFormService xFormService;

    @Autowired
    private RestoreService restoreService;

    @Autowired
    private MenuRepo menuRepo;

    Log log = LogFactory.getLog(SessionController.class);
    ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(Constants.URL_NEW_SESSION)
    public NewSessionResponse newFormResponse(@RequestBody NewSessionRequestBean newSessionBean) throws Exception {
        NewFormRequest newFormRequest = new NewFormRequest(newSessionBean, sessionRepo, xFormService, restoreService);
        return newFormRequest.getResponse();
    }

    @RequestMapping(value = Constants.URL_LIST_SESSIONS, method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<SerializableSession> findAllSessions() {

        Map<Object, Object> mMap = sessionRepo.findAll();
        SessionList sessionList = new SessionList();

        for (Object obj : mMap.values()) {
            sessionList.add((SerializableSession) obj);
        }
        return sessionList;
    }

    @RequestMapping(value = Constants.URL_GET_SESSION, method = RequestMethod.GET)
    @ResponseBody
    public SerializableSession getSession(@RequestParam(value="id") String id) {
        SerializableSession serializableSession = sessionRepo.find(id);
        return serializableSession;
    }


    @RequestMapping(Constants.URL_ANSWER_QUESTION)
    public AnswerQuestionResponseBean answerQuestion(@RequestBody AnswerQuestionRequestBean answerQuestionBean) throws Exception {
        SerializableSession session = sessionRepo.find(answerQuestionBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(session);

        JSONObject resp = AnswerQuestionJson.questionAnswerToJson(formEntrySession.getFormEntryController(),
                formEntrySession.getFormEntryModel(),
                answerQuestionBean.getAnswer(),
                answerQuestionBean.getFormIndex());

        session.setFormXml(formEntrySession.getFormXml());
        session.setInstanceXml(formEntrySession.getInstanceXml());
        session.setSequenceId(formEntrySession.getSequenceId() + 1);
        sessionRepo.save(session);
        AnswerQuestionResponseBean responseBean = mapper.readValue(resp.toString(), AnswerQuestionResponseBean.class);
        return responseBean;

    }

    @RequestMapping(value = Constants.URL_CURRENT, method = RequestMethod.GET)
    @ResponseBody
    public CurrentResponseBean getCurrent(@RequestBody CurrentRequestBean currentRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(currentRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);
        return new CurrentResponseBean(formEntrySession);
    }

    @RequestMapping(value = Constants.URL_SUBMIT_FORM, method = RequestMethod.POST)
    @ResponseBody
    public SubmitResponseBean submitForm(@RequestBody SubmitRequestBean submitRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(submitRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);
        FormRecordProcessorHelper.processXML(formEntrySession.getSandbox(), formEntrySession.submitGetXml());
        return new SubmitResponseBean(formEntrySession);
    }

    @RequestMapping(value = Constants.URL_GET_INSTANCE, method = RequestMethod.GET)
    @ResponseBody
    public GetInstanceResponseBean getInstance(@RequestBody GetInstanceRequestBean getInstanceRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(getInstanceRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);
        return new GetInstanceResponseBean(formEntrySession);
    }

    @RequestMapping(value = Constants.URL_EVALUATE_XPATH, method = RequestMethod.GET)
    @ResponseBody
    public EvaluateXPathResponseBean evaluateXpath(@RequestBody EvaluateXPathRequestBean evaluateXPathRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(evaluateXPathRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);
        return new EvaluateXPathResponseBean(formEntrySession, evaluateXPathRequestBean.getXpath());
    }

    @RequestMapping(value = Constants.URL_NEW_REPEAT, method = RequestMethod.GET)
    @ResponseBody
    public RepeatResponseBean newRepeat(@RequestBody RepeatRequestBean newRepeatRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(newRepeatRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);

        AnswerQuestionJson.descendRepeatToJson(formEntrySession.getFormEntryController(),
                formEntrySession.getFormEntryModel(),
                newRepeatRequestBean.getFormIndex());

        serializableSession.setFormXml(formEntrySession.getFormXml());
        serializableSession.setInstanceXml(formEntrySession.getInstanceXml());
        sessionRepo.save(serializableSession);
        JSONObject response =  AnswerQuestionJson.getCurrentJson(formEntrySession.getFormEntryController(),
                formEntrySession.getFormEntryModel());
        return mapper.readValue(response.toString(), RepeatResponseBean.class);
    }

    @RequestMapping(value = Constants.URL_DELETE_REPEAT, method = RequestMethod.GET)
    @ResponseBody
    public RepeatResponseBean deleteRepeat(@RequestBody RepeatRequestBean repeatRequestBean) throws Exception {
        SerializableSession serializableSession = sessionRepo.find(repeatRequestBean.getSessionId());
        FormEntrySession formEntrySession = new FormEntrySession(serializableSession);

        JSONObject resp = AnswerQuestionJson.deleteRepeatToJson(formEntrySession.getFormEntryController(),
                formEntrySession.getFormEntryModel(),
                repeatRequestBean.getFormIndex());

        serializableSession.setFormXml(formEntrySession.getFormXml());
        serializableSession.setInstanceXml(formEntrySession.getInstanceXml());
        sessionRepo.save(serializableSession);

        JSONObject response =  AnswerQuestionJson.getCurrentJson(formEntrySession.getFormEntryController(),
                formEntrySession.getFormEntryModel());

        return mapper.readValue(response.toString(), RepeatResponseBean.class);
    }

    @RequestMapping(Constants.URL_FILTER_CASES)
    public CaseFilterResponseBean filterCasesHQ(@RequestBody CaseFilterRequestBean filterRequest) throws Exception {
        filterRequest.setRestoreService(restoreService);
        String caseResponse = CaseAPIs.filterCases(filterRequest);
        return new CaseFilterResponseBean(caseResponse);
    }

    @RequestMapping(Constants.URL_SYNC_DB)
    public SyncDbResponseBean syncUserDb(@RequestBody SyncDbRequestBean syncRequest) throws Exception {
        syncRequest.setRestoreService(restoreService);
        String restoreXml = syncRequest.getRestoreXml();
        CaseAPIs.restoreIfNotExists(syncRequest.getUsername(), restoreXml);
        return new SyncDbResponseBean();
    }

    @RequestMapping(Constants.URL_INSTALL)
    public MenuResponseBean performInstall(@RequestBody InstallRequestBean installRequestBean) throws Exception {
        InstallRequest installRequest = new InstallRequest(installRequestBean, xFormService, restoreService, menuRepo);
        return installRequest.getResponse();
    }

    @RequestMapping(Constants.URL_MENU_SELECT)
    public SessionBean selectMenu(@RequestBody MenuSelectBean menuSelectBean) throws Exception {
        System.out.println("Select Menu Select: " + menuSelectBean);
        MenuSession menuSession = new MenuSession(menuRepo.find(menuSelectBean.getSessionId()), restoreService);
        System.out.println("Select Menu Session: " + menuSession);
        Screen nextScreen = menuSession.handleInput(menuSelectBean.getSelection());
        System.out.println("Next Screen: " + nextScreen);
        menuRepo.save(menuSession.serialize());
        if(nextScreen instanceof MenuScreen){
            MenuScreen menuScreen = (MenuScreen) nextScreen;
            MenuDisplayable[] options = menuScreen.getChoices();
            HashMap<Integer, String> optionsStrings = new HashMap<Integer, String>();
            for(int i=0; i <options.length; i++){
                optionsStrings.put(i, options[i].getDisplayText());
            }
            MenuResponseBean menuResponseBean = new MenuResponseBean();
            menuResponseBean.setMenuType(Constants.MENU_MODULE);
            menuResponseBean.setOptions(optionsStrings);
            menuResponseBean.setSessionId(menuSession.getSessionId());
            return menuResponseBean;
        } else if (nextScreen == null){
            System.out.println("Next Screen null!");
            NewSessionResponse response = menuSession.startFormEntry(sessionRepo, xFormService, restoreService).getResponse();
            String stringResponse = new ObjectMapper().writeValueAsString(response);
            System.out.println("New Session Response: " + stringResponse);
            return response;
        }
        return null;
    }
}