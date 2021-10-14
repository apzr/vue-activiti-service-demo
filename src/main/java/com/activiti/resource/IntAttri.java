/*
 * Copyright 2015-2017 the original author or authors.
 * 东华软件-马鞍山金融事业部
 * 工作流引擎模块
 * 为内蒙农信资金系统需求定制开发
 */

package com.activiti.resource;

/**
 * 功能：数值型常量
 * 层级：框架功能封装
 *
 * @Author Tao JinSong<taojinsong@dhcc.com.cn>
 * @Date: 2016/4/19 - 11:01
 */
public class IntAttri {


    public static final int START_VERSION = 1;
    public static final int UN_IMPORTART_NODE = 0;
    public static final int IMPORTART_NODE = 1;
    public static final int START_NODE = 2;

    //挂起，激活状态
    public static final int ACTIVE_STATUS = 1;
    public static final int SUSPEND_STATUS = 0;

    public static final int CMD_ACTIVE = 1;
    public static final int CMD_CANCEL = 0;
    public static final int CMD_NOTRUN = -1;
    public static final int CMD_COMPLETED = 2;

    public static final int CMD_TYPE_SELF = 0;
    public static final int CMD_TYPE_OTHER = 1;


    /*<TX_TYPE>*/
   public static final int APPLY_TASK = 1;
   public static final int CHECK_TASK = 2;
   public static final int TRIAL_TASK = 6;
   public static final int AUDIT_TASK = 3;
   public static final int APPROVE_TASK = 4;
   public static final int REACHED_TASK = 8;
   public static final int ACCEPTED_TASK = 9;
   public static final int AUTHENTICATE_TASK = 7;
   public static final int ACCOUNT_TASK = 5;
    /*</TX_TYPE>*/

    public static final int SINGLE_HANDLE = 0;
    public static final int BATCH_HANDLE = 1;

    public static final int MOLE_MODEL = 0;
    public static final int ATOM_MODEL = 1;
    public static final int FULL_MODEL = 2;

    public static final int GLOBAL_PRIORITY = 1;
    public static final int CALLACTIVITY_PRIORITY = 2;
    public static final int EXCLUSIVEGATEWAY_PRIORITY = 3;
    public static final int USERTASK_PRIORITY = 4;
    public static final int STARTEVENT_PRIORITY = 5;
    public static final int ENDEVENT_PRIORITY = 6;

    public static final int CANNOTDO = 0;
    public static final int CANDO = 1;

    public static final int YES = 1;
    public static final int NO = 0;

    public static final int ASSIGNEE = 0;
    public static final int CANDIDATEUSER = 1;
    public static final int CANDIDATEGROUP = 2;


    public static final int INTERNAL_FORM = 0;
    public static final int EXTERNAL_FORM = 1;
    public static final int FROM_SELF = 0;
    public static final int FROM_OTHER = 1;

    public static final int VIEW_SINGLE_PROCESS = 0;
    public static final int VIEW_FULL_PROCESS = 1;
    public static final int TRACE_SINGLE_INSTANCE = 2;
    public static final int TRACE_FULL_INSTANCE = 3;

    public static final int CALLELEMENT_SQL = 1;
    public static final int MULTIINSTANCE_INPUTITEM_SQL = 2;
    public static final int MULTIINSTANCE_COMPLETIONCONDITION = 3;


    public static final int INTERFIX_LENGTH = 4;


    public static final int WORKPANEL_NORMAL = 0;
    public static final int WORKPANEL_TODAY_TOTAL = 1;
    public static final int WORKPANEL_TODAY_CANCLAIM = 2;
    public static final int WORKPANEL_TODAY_CLAIMED = 3;
    public static final int WORKPANEL_TODAY_COMPLETED = 4;
    public static final int WORKPANEL_TODAY_SINGAL = 5;


    public static final int CALL_EXPRESS_TYPE_DEFAULT = 0;
    public static final int CALL_EXPRESS_TYPE_SQL = 1;
    public static final int CALL_EXPRESS_TYPE_TENANT = 2;


    public static final int CALLBACK_TYPE_CATEGORY=1;
    public static final int CALLBACK_TYPE_MOLE=2;
    public static final int CALLBACK_TYPE_ATOM=3;


    static public final int MODEL_TYPE_ACT = 1;
    static public final int MODEL_TYPE_DHC = 2;

    public static final int MSG_UNREAD=0;
    public static final int MSG_READ=1;
}
