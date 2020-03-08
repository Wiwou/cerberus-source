/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.servlet.crud.test.testcase;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.Label;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseDepService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mlombard
 */
@WebServlet(name = "ReadTestCaseV2", urlPatterns = {"/ReadTestCaseV2"})
public class ReadTestCaseV2 extends AbstractCrudTestCase {

    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private ITestCaseDepService testCaseDepService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;
    @Autowired
    private ICampaignParameterService campaignParameterService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ILabelService labelService;
    @Autowired
    private IInvariantService invariantService;

    private static final Logger LOG = LogManager.getLogger(ReadTestCase.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        //Parsing and securing all required parameters.
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        List<String> system = ParameterParserUtil.parseListParamAndDecodeAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
        String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), null);
        boolean withSteps = ParameterParserUtil.parseBooleanParam(request.getParameter("withSteps"), false);

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(msg);
        JSONObject jsonResponse = new JSONObject();

        try {
            if (!Strings.isNullOrEmpty(test) && testCase != null) {
                answer = findTestCaseByTestTestCase(test, testCase, request, withSteps);
            }

            jsonResponse = answer.getItem() == null ? new JSONObject() : (JSONObject) answer.getItem();
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", sEcho);

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            LOG.warn(e, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
            // TODO return to the gui
        } catch (Exception ex) {
            LOG.error(ex, ex);
            // TODO return to the gui
        }
    }

    private AnswerItem<JSONObject> findTestCaseByTestTestCase(String test, String testCase, HttpServletRequest request, boolean withSteps) throws JSONException, CerberusException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        JSONObject jsonTestCase = new JSONObject();
        JSONArray jsonContentTable = new JSONArray();

        //finds the project
        AnswerItem answerTestCase = testCaseService.readByKey(test, testCase);

        if (answerTestCase.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerTestCase.getItem() != null) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCase tc = (TestCase) answerTestCase.getItem();
            LOG.debug(tc.getBugID().toString());

            jsonTestCase.put("header", getTestCaseHeader(test, testCase, tc));

            //Get steps if withSteps param is set on true
            if (withSteps) {
                jsonTestCase = findTestCaseSteps(test, testCase, jsonTestCase);
                jsonResponse.put("hasPermissionsStepLibrary", (request.isUserInRole("TestStepLibrary")));
            }

            jsonContentTable.put(jsonTestCase);

            jsonResponse.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(tc, request));
            jsonResponse.put("hasPermissionsDelete", testCaseService.hasPermissionsDelete(tc, request));
            jsonResponse.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(tc, request));
            jsonResponse.put("contentTable", jsonContentTable);

        } else {
            item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_NOT_FOUND_OR_NOT_AUTHORIZE));
            return item;
        }

        item.setItem(jsonResponse);
        item.setResultMessage(answerTestCase.getResultMessage());

        return item;
    }

    private JSONObject findTestCaseSteps(String test, String testCase, JSONObject jsonTestCase) throws JSONException, CerberusException {

        JSONObject jsonProperties = new JSONObject();
        JSONArray stepList = new JSONArray();
        Gson gson = new Gson();

        AnswerList<TestCaseStep> testCaseStepList = testCaseStepService.readByTestTestCase(test, testCase);
        AnswerList<TestCaseStepAction> testCaseStepActionList = testCaseStepActionService.readByTestTestCase(test, testCase);
        AnswerList<TestCaseStepActionControl> testCaseStepActionControlList = testCaseStepActionControlService.readByTestTestCase(test, testCase);

        for (TestCaseStep step : (List<TestCaseStep>) testCaseStepList.getDataList()) {
            step = testCaseStepService.modifyTestCaseStepDataFromUsedStep(step);
            JSONObject jsonStep = new JSONObject(gson.toJson(step));

            //Fill JSON with step info
            jsonStep.put("objType", "step");
            //Add a JSON array for Action List from this step
            jsonStep.put("actions", new JSONArray());
            //Fill action List

            if (step.getUseStep().equals("Y")) {
                //If this step is imported from library, we call the service to retrieve actions
                TestCaseStep usedStep = testCaseStepService.findTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepAction> actionList = testCaseStepActionService.getListOfAction(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());

                // Get the used step sort
                jsonStep.put("useStepStepSort", usedStep.getSort());
                jsonProperties.put("inheritedProperties", getTestCaseCountryProperties(step.getUseStepTest(), step.getUseStepTestCase()));

                for (TestCaseStepAction action : actionList) {

                    if (action.getStep() == step.getUseStepStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");

                        jsonAction.put("controls", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : controlList) {

                            if (control.getStep() == step.getUseStepStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));

                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controls").put(jsonControl);
                            }
                        }
                        //we put the action in the actionList for the corresponding step
                        jsonStep.getJSONArray("actions").put(jsonAction);
                    }
                }
            } else {

                jsonProperties.put("testCaseProperties", getTestCaseCountryProperties(step.getTest(), step.getTestCase()));

                //else, we fill the actionList with the action from this step
                for (TestCaseStepAction action : (List<TestCaseStepAction>) testCaseStepActionList.getDataList()) {

                    if (action.getStep() == step.getStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");
                        jsonAction.put("controls", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : (List<TestCaseStepActionControl>) testCaseStepActionControlList.getDataList()) {

                            if (control.getStep() == step.getStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));
                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controls").put(jsonControl);
                            }
                        }
                        //we put the action in the actionList for the corresponding step
                        jsonStep.getJSONArray("actions").put(jsonAction);
                    }
                }
            }
            stepList.put(jsonStep);
        }

        jsonTestCase.put("properties", jsonProperties);
        jsonTestCase.put("steps", stepList);

        return jsonTestCase;
    }

    private JSONObject getTestCaseHeader(String test, String testCase, TestCase tc) throws JSONException, CerberusException {

        JSONObject jsonTestCaseHeader = new JSONObject();
        jsonTestCaseHeader = convertToJSONObject(tc);
        jsonTestCaseHeader.put("bugs", tc.getBugID());
        jsonTestCaseHeader.put("countries", getTestCaseCountries(test, testCase));
        jsonTestCaseHeader.put("dependencies", getTestCaseDependencies(test, testCase));
        jsonTestCaseHeader.put("labels", getTestCaseLabels(test, testCase));

        return jsonTestCaseHeader;
    }

    private Collection<JSONObject> getTestCaseCountryProperties(String test, String testCase) throws JSONException {
        List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(test, testCase);
        HashMap<String, JSONObject> hashProp = new HashMap<>();
        JSONObject propertyFound = new JSONObject();

        for (TestCaseCountryProperties prop : properties) {
            propertyFound = convertToJSONObject(prop);
            propertyFound.put("countries", getPropertyCountries(testCaseCountryPropertiesService.findCountryByProperty(prop)));
            hashProp.put(prop.getTest() + "_" + prop.getTestCase() + "_" + prop.getProperty(), propertyFound);
        }
        return hashProp.values();
    }

    private JSONArray getPropertyCountries(List<String> countriesSelected) throws JSONException {

        JSONArray countries = new JSONArray();
        for (String country : countriesSelected) {
            countries.put(convertToJSONObject(invariantService.readByKey("COUNTRY", country).getItem()));
        }
        return countries;
    }

    private JSONArray getTestCaseCountries(String test, String testCase) throws JSONException {
        JSONArray countries = new JSONArray();
        AnswerList<TestCaseCountry> answerTestCaseCountryList = testCaseCountryService.readByTestTestCase(null, test, testCase, null);
        for (TestCaseCountry country : (List<TestCaseCountry>) answerTestCaseCountryList.getDataList()) {
            countries.put(convertToJSONObject(invariantService.readByKey("COUNTRY", country.getCountry()).getItem()));
        }
        return countries;
    }

    private JSONArray getTestCaseDependencies(String test, String testCase) throws CerberusException, JSONException {
        JSONArray dependencies = new JSONArray();
        List<TestCaseDep> testCaseDepList = testCaseDepService.readByTestAndTestCase(test, testCase);
        for (TestCaseDep testCaseDep : testCaseDepList) {
            dependencies.put(convertToJSONObject(testCaseDep));
        }
        return dependencies;
    }

    private JSONArray getTestCaseLabels(String test, String testCase) throws JSONException {
        JSONArray labels = new JSONArray();
        AnswerList<TestCaseLabel> answerTestCaseLabelList = testCaseLabelService.readByTestTestCase(test, testCase, null);
        for (TestCaseLabel label : (List<TestCaseLabel>) answerTestCaseLabelList.getDataList()) {
            labels.put(convertToJSONObject(labelService.readByKey(label.getLabelId()).getItem()));
        }
        return labels;
    }

    private JSONObject convertToJSONObject(TestCase testCase) throws JSONException {
        return new JSONObject()
                .put("test", testCase.getTest())
                .put("testCase", testCase.getTestCase())
                .put("application", testCase.getApplication())
                .put("description", testCase.getDescription())
                .put("behaviourOrValueExpected", testCase.getBehaviorOrValueExpected())
                .put("priority", testCase.getPriority())
                .put("status", testCase.getStatus())
                .put("tcActive", testCase.getTcActive())
                .put("conditionOper", testCase.getConditionOper())
                .put("conditionValue1", testCase.getConditionVal1())
                .put("conditionValue2", testCase.getConditionVal2())
                .put("conditionValue3", testCase.getConditionVal3())
                .put("group", testCase.getGroup())
                .put("origine", testCase.getOrigine())
                .put("refOrigine", testCase.getRefOrigine())
                .put("howTo", testCase.getHowTo())
                .put("comment", testCase.getComment())
                .put("fromBuild", testCase.getFromBuild())
                .put("fromRev", testCase.getFromRev())
                .put("toBuild", testCase.getToBuild())
                .put("toRev", testCase.getToRev())
                .put("targetBuild", testCase.getTargetBuild())
                .put("targetRev", testCase.getTargetRev())
                .put("implementer", testCase.getImplementer())
                .put("executor", testCase.getExecutor())
                .put("activeQA", testCase.getActiveQA())
                .put("activeUAT", testCase.getActiveUAT())
                .put("activePROD", testCase.getActivePROD())
                .put("function", testCase.getFunction())
                .put("usrAgent", testCase.getUserAgent())
                .put("screenSize", testCase.getScreenSize())
                .put("usrCreated", testCase.getUsrCreated())
                .put("dateCreated", testCase.getDateCreated())
                .put("usrModif", testCase.getUsrModif())
                .put("dateModif", testCase.getDateModif())
                .put("testCaseVersion", testCase.getTestCaseVersion());
    }

    private JSONObject convertToJSONObject(TestCaseCountryProperties prop) throws JSONException {
        return new JSONObject()
                .put("fromTest", prop.getTest())
                .put("fromTestCase", prop.getTestCase())
                .put("property", prop.getProperty())
                .put("description", prop.getDescription())
                .put("type", prop.getType())
                .put("database", prop.getDatabase())
                .put("value1", prop.getValue1())
                .put("value2", prop.getValue2())
                .put("length", prop.getLength())
                .put("rowLimit", prop.getRowLimit())
                .put("nature", prop.getNature())
                .put("rank", prop.getRank());
    }

    private JSONObject convertToJSONObject(Invariant countryInvariant) throws JSONException {
        return new JSONObject()
                .put("value", countryInvariant.getValue())
                .put("description", countryInvariant.getDescription())
                .put("grp1", countryInvariant.getGp1())
                .put("grp2", countryInvariant.getGp2())
                .put("grp3", countryInvariant.getGp3());
    }

    private JSONObject convertToJSONObject(TestCaseDep testCaseDep) throws JSONException {
        return new JSONObject()
                .put("id", testCaseDep.getId())
                .put("test", testCaseDep.getDepTest())
                .put("testCase", testCaseDep.getDepTestCase())
                .put("type", testCaseDep.getType())
                .put("active", "Y".equals(testCaseDep.getActive()))
                .put("description", testCaseDep.getDepDescription())
                .put("event", testCaseDep.getDepEvent());
    }

    private JSONObject convertToJSONObject(Label label) throws JSONException {
        return new JSONObject()
                .put("id", label.getId())
                .put("system", label.getSystem())
                .put("label", label.getLabel())
                .put("type", label.getType())
                .put("color", label.getColor())
                .put("parentLabelID", label.getParentLabelID())
                .put("reqType", label.getReqType())
                .put("reqStatus", label.getReqStatus())
                .put("reqCriticity", label.getReqCriticity())
                .put("description", label.getDescription())
                .put("longDesc", label.getLongDesc())
                .put("usrCreated", label.getUsrCreated())
                .put("dateCreated", label.getDateCreated())
                .put("usrModif", label.getUsrModif())
                .put("counter1", label.getCounter1());
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}