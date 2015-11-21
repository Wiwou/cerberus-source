/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        var doc = new Doc();
        var system = getUser().defaultSystem;

        oldPreferenceCompatibility();

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapse();
        appendCampaignList();
        appendCountryList();
        typeSelectHandler();
        showLoader("#filtersPanel");
        $.when(
                loadMultiSelect("ReadTest", "sEcho=1", "test", ["test"], "test"),
                loadMultiSelect("ReadProject", "sEcho=1", "project", ["idProject"], "idProject"),
                loadMultiSelect("ReadApplication", "system=" + system, "application", ["application"], "application", true),
                loadMultiSelect("ReadUserPublic", "", "creator", ["login"], "login"),
                loadMultiSelect("ReadUserPublic", "", "implementer", ["login"], "login"),
                loadMultiSelect("ReadTestBattery", "", "testBattery", ["testbattery"], "testbattery"),
                loadMultiSelect("ReadCampaign", "", "campaign", ["campaign"], "campaign"),
                loadMultiSelect("ReadBuildRevisionInvariant", "level=1&system=" + system, "targetSprint", ["versionName"], "versionName"),
                loadMultiSelect("ReadBuildRevisionInvariant", "level=2&system=" + system, "targetRev", ["versionName"], "versionName"),
                loadInvariantMultiSelect("priority", "PRIORITY"),
                loadInvariantMultiSelect("group", "GROUP"),
                loadInvariantMultiSelect("status", "TCSTATUS")
                ).then(function () {
            hideLoader("#filtersPanel");
            if ($("#typeSelect").val() === "filters") {
                loadTestCaseFromFilter();
            }
        });

        $("#typeSelect").on("change", typeSelectHandler);
        $("#loadbutton").click(loadTestCaseFromFilter);
        $("#resetbutton").click(function () {
            $(".multiselectelement").each(function () {
                $(this).multiselect('deselectAll', false);
                $(this).multiselect('updateButtonText');
            });
        });
        $("#addQueue").click({"select": "#testCaseList option:selected"}, checkExecution);
        $("#addAllQueue").click({"select": "#testCaseList option"}, checkExecution);
        $("#resetQueue").click(function (event) {
            stopPropagation(event);
            $("#notValid").empty();
            $("#queue").empty();
        });

        loadExecForm();
        loadRobotForm();
        displayEnvList("environment", system);

        $('[name="envSettings"]').on("change", function () {
            if (this.value === "auto") {
                $("#envSettingsMan").hide();
                $("#envSettingsAuto").show();
            } else if (this.value === "manual") {
                $("#envSettingsAuto").hide();
                $("#envSettingsMan").show();
            }
        });

        $("#saveRobotPreferences").click(saveRobotPreferences);
        $("#saveExecutionParams").click(saveExecutionPreferences);
        $("#robotConfig").change(loadRobotInfo);

    });
});

function typeSelectHandler() {
    var value = $("#typeSelect").val();
    if (value === "filters") {
        $("#campaignSelection").hide();
        $("#filters").show();
        $("#resetbutton").show();
        loadTestCaseFromFilter();
    } else if (value === "campaign") {
        $("#filters").hide();
        $("#resetbutton").hide();
        $("#campaignSelection").show();
        $("#testCaseList").empty();
    }
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name) === 0)
            var value = c.substring(name.length, c.length);

        document.cookie = cname + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
        return value;
    }
    return "";
}

function deleteRow() {
    $(this).parent('li').remove();
}

function getCountries() {
    var countries = [];

    $("#countryList .countrycb").each(function () {
        if ($(this).prop("checked")) {
            countries.push($(this).prop("name"));
        }
    });

    return countries;
}

function getEnvironment() {
    var envList = [];
    var settings = $('input[name="envSettings"]:checked').val();

    if (settings === "auto") {
        var envListAuto = $("#envSettingsAuto select").val();

        if (envListAuto !== null) {
            for (var index = 0; index < envListAuto.length; index++) {
                var env = {"env": envListAuto[index]};

                envList.push(env);
            }
        }
    } else if (settings === "manual") {
        var env = {"env": "MANUAL"};

        envList.push(env);
    }

    return envList;
}

function addToQueue(executionList) {
    var notValidList = [];
    var queue = $("#queue");
    var removeBtn = $("<span></span>").addClass("glyphicon glyphicon-remove delete").click(deleteRow);

    for (var index = 0; index < executionList.length; index++) {
        var execution = executionList[index];

        if (execution.isValid) {
            queue.append($('<li></li>').addClass("list-group-item").text(execution.test + " - " + execution.testcase + " - " +
                    execution.env + " - " + execution.country)
                    .prepend(removeBtn.clone(true)).data("item", execution));
        } else {
            notValidList.push(execution);
        }
    }

    if (notValidList.length !== 0) {
        $("#notValid").text("!!! There is " + notValidList.length + " that couldn't be added to the queue !!!");
    }
}

function checkExecution(event) {
    var select = $(event.data.select);
    var environment = getEnvironment();
    var countries = getCountries();
    var testcase = [];

    select.each(function () {
        var item = $(this).data("item");

        testcase.push({"test": item.test,
            "testcase": item.testCase,
            "application": item.application});
    });

    $("#error").empty();
    if (testcase.length === 0) {
        $("#error").text("!!! Please, select at least 1 Test Case !!!");
    } else if (environment.length === 0) {
        $("#error").text("!!! Please, select at least 1 Environment !!!");
    } else if (countries.length === 0) {
        $("#error").text("!!! Please, select at least 1 Country !!!");
    } else {
        $.ajax({
            url: "GetExecutionQueue",
            method: "POST",
            data: {"system": getUser().defaultSystem,
                "testcase": JSON.stringify(testcase),
                "environment": JSON.stringify(environment),
                "countries": JSON.stringify(countries)},
            success: function (data) {
                addToQueue(data.contentTable);
            },
            error: showUnexpectedError
        });
    }
}

function loadTestCaseFromFilter() {
    if ($("#typeSelect").val() === "filters") {
        showLoader("#chooseTest");
        $.ajax({
            url: "ReadTestCase",
            method: "GET",
            data: "filter=true&" + $("#filters").serialize() + "&system=" + getUser().defaultSystem,
            datatype: "json",
            async: true,
            success: function (data) {
                var testCaseList = $("#testCaseList");

                testCaseList.empty();

                for (var index = 0; index < data.contentTable.length; index++) {
                    var text = data.contentTable[index].test + " - " + data.contentTable[index].testCase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].shortDescription;

                    testCaseList.append($("<option></option>")
                            .text(text)
                            .val(data.contentTable[index].testCase)
                            .data("item", data.contentTable[index]));
                }
                hideLoader("#chooseTest");
            }
        });
    }
}

function saveRobotPreferences() {
    var pref = convertSerialToJSONObject($("#robotSettingsForm").serialize());
    localStorage.setItem("robotSettings", JSON.stringify(pref));
}

function saveExecutionPreferences() {
    var pref = convertSerialToJSONObject($("#executionSettingsForm").serialize());
    localStorage.setItem("executionSettings", JSON.stringify(pref));
}

function loadExecForm() {
    var pref = JSON.parse(localStorage.getItem("executionSettings"));

    if (pref !== null) {
        $("#tag").val(pref.tag);
        $("#timeout").val(pref.timeout);
    }

    loadSelect("OUTPUTFORMAT", "outputFormat", pref);
    loadSelect("VERBOSE", "verbose", pref);
    loadSelect("SCREENSHOT", "screenshot", pref);
    loadSelect("SELENIUMLOG", "seleniumLog", pref);
    loadSelect("MANUALEXECUTION", "manualExecution", pref);
    loadSelect("PAGESOURCE", "pageSource", pref);
    loadSelect("SYNCHRONEOUS", "synchroneous", pref);
    loadSelect("RETRIES", "retries", pref);
}

function loadRobotForm() {
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (pref !== null) {
        $("#seleniumIP").val(pref.seleniumIP);
        $("#seleniumPort").val(pref.seleniumPort);
        $("#version").val(pref.version);
    }

    appendRobotList(pref);
    loadSelect("BROWSER", "browser", pref);
    $("[name=platform]").append($('<option></option>').text("Optional").val(""));
    loadSelect("PLATFORM", "platform", pref);
    $("[name=screenSize]").append($('<option></option>').text("Default (Client Full Screen)").val(""));
    loadSelect("screensize", "screenSize", pref);
}

function loadRobotInfo() {
    var value = "";
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (this.value !== undefined) {
        value = $(this).val();
    } else if (pref !== null) {
        value = pref.robotConfig;
    }

    if (value !== "") {
        $.ajax({
            url: "ReadRobot",
            method: "GET",
            data: {robotid: value},
            dataType: "json",
            async: true,
            success: function (data) {
                $("#seleniumIP").val(data.contentTable.host).attr("readonly", true);
                $("#seleniumPort").val(data.contentTable.port).attr("readonly", true);
                $("#browser").val(data.contentTable.browser).attr("readonly", true);
                $("#version").val(data.contentTable.version).attr("readonly", true);
                $("#platform").val(data.contentTable.platform).attr("readonly", true);
                $("#screenSize").val("").attr("readonly", true);
            }
        });
    } else {
        if (pref !== null) {
            for (var key in pref) {
                if (key !== "robotConfig") {
                    $("#" + key).attr("readonly", false).val(pref[key]);
                }
            }
        } else {
            $("#seleniumIP").attr("readonly", false).val("");
            $("#seleniumPort").attr("readonly", false).val("");
            $("#browser").attr("readonly", false).val("");
            $("#version").attr("readonly", false).val("");
            $("#platform").attr("readonly", false).val("");
            $("#screenSize").attr("readonly", false).val("");
        }
    }
}

function loadSelect(idName, selectName, pref) {
    $.ajax({
        url: "FindInvariantByID",
        method: "GET",
        data: {idName: idName},
        dataType: "json",
        async: true,
        success: function (data) {
            for (var option in data) {
                $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
            }
            if (pref !== null) {
                $("[name='" + selectName + "']").val(pref[selectName]);
            }
        }
    });
}

function multiSelectConf(name) {
    this.maxHeight = 150;
    this.checkboxName = name;
    this.buttonWidth = "100%";
    this.enableFiltering = true;
    this.enableCaseInsensitiveFiltering = true;
}

//function updateMultiSelect(selectName, dataToCheck, valueList) {
//    $("#" + selectName + "Filter option").each(function () {
//        if (valueList !== null && valueList.indexOf($(this).data("item")[dataToCheck]) === -1) {
//            $(this).prop("checked", false);
//            $(this).prop("disabled", true);
//        } else {
//            $(this).prop("disabled", false);
//        }
//    });
//
//    $("#" + selectName + "Filter").multiselect('rebuild');
//}

function loadMultiSelect(url, urlParams, selectName, textItem, valueItem, isUpdate) {
    var jqXHR = $.ajax({
        url: url,
        method: "GET",
        data: urlParams,
        dataType: "json",
        async: true,
        success: function (data) {
            var select = $("#" + selectName + "Filter");

            for (var index = 0; index < data.contentTable.length; index++) {
                var text = textItem.map(function (item) {
                    return data.contentTable[index][item];
                }).join(" - ");

                select.append($("<option></option>").text(text)
                        .val(data.contentTable[index][valueItem])
                        .data("item", data.contentTable[index]));
            }

            select.multiselect(new multiSelectConf(selectName));
        },
        "error": showUnexpectedError
    });

    return jqXHR;
}

function loadInvariantMultiSelect(selectName, idName) {
    var jqXHR = $.ajax({
        url: "FindInvariantByID",
        method: "GET",
        data: {idName: idName},
        dataType: "json",
        async: true,
        success: function (data) {
            var select = $("#" + selectName + "Filter");

            for (var option in data) {
                select.append($("<option></option>").text(data[option].value)
                        .val(data[option].value)
                        .data("item", data[option]));
            }

            select.multiselect(new multiSelectConf(selectName));
        },
        error: showUnexpectedError
    });
    return jqXHR;
}

function appendCampaignList() {
    var jqxhr = $.getJSON("ReadCampaign");
    $.when(jqxhr).then(function (data) {
        var campaignList = $("#campaignSelect");

        campaignList.append($('<option></option>').text(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign + " - " + data.contentTable[index].description)
                    .val(data.contentTable[index].campaign));
        }
    });
}

function appendCountryList() {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" name="' + country + '"/>' + country + '\
                                </label>');
        }
    });
}

function appendRobotList(pref) {
    var jqxhr = $.getJSON("ReadRobot");
    $.when(jqxhr).then(function (data) {
        var robotList = $("#robotConfig");

        robotList.append($('<option></option>').text("Custom configuration").val(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            robotList.append($('<option></option>').text(data.contentTable[index].robot).val(data.contentTable[index].robotID));
        }

        if (pref !== null) {
            robotList.val(pref["robotConfig"]);
            loadRobotInfo();
        }
    });
}

/** UTILITY FONCTIONS TO MIGRATE THE PREFERENCES **/

function oldPreferenceCompatibility() {
    if (localStorage.getItem("robotSettings") === null
            && localStorage.getItem("executionSettings") === null) {

        var user = getUser();

        var robotConfig = {
            robotConfig: user.robot,
            seleniumIP: user.robotHost,
            seleniumPort: user.robotPort,
            version: user.robotVersion,
            platform: user.robotPlatform,
            screenSize: getCookie("ExecutionScreenSize")
        };

        var execConfig = {
            tag: getCookie("TagPreference"),
            outputFormt: getCookie("OutputFormatPreference"),
            verbose: getCookie("VerbosePreference"),
            screenshot: getCookie("ScreenshotPreference"),
            pageSource: getCookie("PageSourcePreference"),
            seleniumLog: getCookie("SeleniumLogPreference"),
            synchroneous: getCookie("SynchroneousPreference"),
            timeout: getCookie("TimeoutPreference"),
            retries: getCookie("ExecutionRetries"),
            manualExecution: getCookie("ManualExecutionPreference")
        };

        localStorage.setItem("executionSettings", JSON.stringify(execConfig));
        localStorage.setItem("robotSettings", JSON.stringify(robotConfig));
    }
}