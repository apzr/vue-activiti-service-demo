/*
 * Copyright 2015-2017 the original author or authors.
 * 东华软件-马鞍山金融事业部
 * 工作流引擎模块
 * 为内蒙农信资金系统需求定制开发
 */

package com.activiti.resource;

/**
 * 使用 ccmt scmt acmt 生成类注释
 */
public class StrAttri {

    /*<TX_TYPE>*/
    public static final String APPLY_TASK = "申请";
    public static final String CHECK_TASK = "复核";
    public static final String TRIAL_TASK = "初审";
    public static final String AUDIT_TASK = "审核";
    public static final String APPROVE_TASK = "审批";
    public static final String REACHED_TASK = "达成";
    public static final String ACCEPTED_TASK = "受理";
    public static final String AUTHENTICATE_TASK = "确认";
    public static final String ACCOUNT_TASK = "记账";
    /*</TX_TYPE>*/
    static public final String PREFIX = "T_";
    static public final String INTERFIX = "9__9";
    static public final String SUFFIX = "_E";

    static public final String MUTLI_TRACE_URL = "/process-edit/viewer-app/traceView";
    static public final String SINGLE_TRACE_URL = "/process-edit/viewer-app/callactivityTraceView";
    static public final String MODEL_PRINT_URL = "/process-edit/print-app/printView";
    static public final String PROCESS_VIEW_URL = "/page/base/process/processTree";


    static public final String MOLO_TENANT = "xxxxxx";
    static public final String DEFAULT_VALUE = "xxxxxx";
    static public final String MOLE = "mole";
    static public final String ATOM = "atom";

    static public final String BPMN_NAMESPACE = "http://b3mn.org/stencilset/bpmn2.0#";
    static public final String MODLE_TYPE = "type";
    static public final String BPMN_FILE_EXT = ".bpmn20.xml";
    static public final String DEFAULT_CHARSET = "UTF-8";
    static public final String APPEND_SEPARATOR = "-";

    static public final String PROCESS = "process";
    static public final String EXCLUSIVEGATEWAY = "exclusiveGateway";
    static public final String USERTASK = "userTask";
    static public final String STARTEVENT = "startEvent";
    static public final String ENDEVENT = "endEvent";
    static public final String CALLACTIVITY = "callActivity";

    static public final String IMAGE_RESOURCE = "image";
    static public final String XML_RESOURCE = "xml";

    static public final String ACTIVITY_DOCUMENTATION = "documentation";

    static public final String TRUE = "true";
    static public final String FALSE = "false";

    static public final String ACTIVE_STATUS = "active";
    static public final String COMPLETED_STATUS = "complete";
    static public final String ROLLBACK_STATUS = "rollback";

    static public final String RESULT_PASS = "pass";
    static public final String RESULT_UNPASS = "unpass";
    static public final String RESULT_ROLLBACK_LAST = "rollback_last";
    static public final String RESULT_ROLLBACK_START = "rollback_start";
    static public final String RESULT_ROLLBACK_DEFINE = "rollback_define";


    static public final String CALL_VALUE_KEY = "act_yw_call";

    static public final String NEXTPROCESS = "nextProcess";
    static public final String CATEGORY_FULL_TREE = "categoryTree";

    static public final String REASON_CANCEL = "子流程取消";


    static public final String ROLE_TYPE_ADMINISTARTOR = "1";
    static public final String ROLE_TYPE_TELLER = "3";
    static public final String ROLE_TYPE_DIRECTOR = "2";


    static public final String MODEL_EXPORT_FILE_EXT = ".DHCWF";
    static public final String MODEL_EXPORT_FILES_EXT = ".DHCWFS";
}
