/*
 * Copyright 2015-2017 the original author or authors.
 * 东华软件-马鞍山金融事业部
 * 工作流引擎模块
 * 为内蒙农信资金系统需求定制开发
 */

package com.activiti.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.editor.language.json.converter.ActivityProcessor;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Tao JinSong
 */
public class CustomSequenceFlowJsonConverter extends BaseBpmnJsonConverter {
    private static final Logger LOG = LoggerFactory.getLogger(CustomSequenceFlowJsonConverter.class);
    
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
 
    Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }
    
    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_SEQUENCE_FLOW, CustomSequenceFlowJsonConverter.class);
    }
    
    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(SequenceFlow.class, CustomSequenceFlowJsonConverter.class);
    }
    
    @Override
    public void convertToJson(BaseElement baseElement, ActivityProcessor processor,
                              BpmnModel model, FlowElementsContainer container, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
        
        SequenceFlow sequenceFlow = (SequenceFlow) baseElement;
        ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(sequenceFlow.getId(), STENCIL_SEQUENCE_FLOW, 172, 212, 128, 212);
        ArrayNode dockersArrayNode = objectMapper.createArrayNode();
        ObjectNode dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getSourceRef()).getWidth() / 2.0);
        dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getSourceRef()).getHeight() / 2.0);
        dockersArrayNode.add(dockNode);
        
        if (model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() > 2) {
            for (int i = 1; i < model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() - 1; i++) {
                GraphicInfo graphicInfo = model.getFlowLocationGraphicInfo(sequenceFlow.getId()).get(i);
                dockNode = objectMapper.createObjectNode();
                dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX());
                dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY());
                dockersArrayNode.add(dockNode);
            }
        }
        
        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getTargetRef()).getWidth() / 2.0);
        dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getTargetRef()).getHeight() / 2.0);
        dockersArrayNode.add(dockNode);
        flowNode.set("dockers", dockersArrayNode);
        ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
        flowNode.set("outgoing", outgoingArrayNode);
        flowNode.set("target", BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
        
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put(PROPERTY_OVERRIDE_ID, sequenceFlow.getId());
        if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
            propertiesNode.put(PROPERTY_NAME, sequenceFlow.getName());
        }
        
        if (StringUtils.isNotEmpty(sequenceFlow.getDocumentation())) {
            propertiesNode.put(PROPERTY_DOCUMENTATION, sequenceFlow.getDocumentation());
        }
        // 添加流程条件
        if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
            // 为了兼容部署时流程条件判断这里添加空格
            String condition = sequenceFlow.getConditionExpression();
            if (!condition.contains(" ")) {
                condition = condition.replace("{", "{ ").replace("}", " }").replace("==", " == ");
            }
            
            propertiesNode.put(PROPERTY_SEQUENCEFLOW_CONDITION, condition);
        }
        
        if (sequenceFlow.getExecutionListeners().size() > 0) {
            BpmnJsonConverterUtil.convertListenersToJson(sequenceFlow.getExecutionListeners(), true, propertiesNode);
        }
        
        flowNode.set(EDITOR_SHAPE_PROPERTIES, propertiesNode);
        shapesArrayNode.add(flowNode);
    }
    
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        // nothing to do
    }
    
    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        SequenceFlow flow = new SequenceFlow();
        
        String sourceRef = BpmnJsonConverterUtil.lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
        if (sourceRef != null) {
            flow.setSourceRef(sourceRef);
            JsonNode targetNode = elementNode.get("target");
            if (targetNode != null && !targetNode.isNull()) {
                String targetId = targetNode.get(EDITOR_SHAPE_ID).asText();
                if (shapeMap.get(targetId) != null) {
                    flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
                }
            }
        }
        // 流程条件判断
        JsonNode conditionNode = getProperty(PROPERTY_SEQUENCEFLOW_CONDITION, elementNode);
        if (conditionNode != null) {
            
            if (conditionNode.isTextual() && !conditionNode.isNull()) {
                flow.setConditionExpression(conditionNode.asText());
                
            } else if (conditionNode.get("expression") != null) {
            
            } else if (conditionNode.isArray() && conditionNode.size() > 0) {
                // 老代码
                StringBuilder conditionExpression = new StringBuilder();
                
                conditionExpression.append("${");
                for (int i = 0; i < conditionNode.size(); i++) {
                    conditionExpression.append(conditionNode.get(i).get("field").get("VALUE").toString().replaceAll("\"", " "));
                    conditionExpression.append(conditionNode.get(i).get("operator").get("VALUE").toString().replaceAll("\"", " "));
                    
                    if (conditionNode.get(i).get("value").get("VALUE") == null && "\"date\"".equals(conditionNode.get(i).get("field").get("type").toString())) { //日期
                        conditionExpression.append(conditionNode.get(i).get("value").get("TEXT").toString().replaceAll("\"", "\\\"").replaceAll("-", ""));
                    } else if (conditionNode.get(i).get("value").get("VALUE") == null && "\"number\"".equals(conditionNode.get(i).get("field").get("type").toString())) { //数字
                        conditionExpression.append(conditionNode.get(i).get("value").get("TEXT").toString().replaceAll("\"", ""));
                    } else if (conditionNode.get(i).get("value").get("VALUE") == null && "\"text\"".equals(conditionNode.get(i).get("field").get("type").toString())) { //普通输入框
                        conditionExpression.append(conditionNode.get(i).get("value").get("TEXT").toString().replaceAll("\"", "\\\""));
                    } else if (conditionNode.get(i).get("value").get("VALUE") != null) {
                        conditionExpression.append(conditionNode.get(i).get("value").get("VALUE").toString().replaceAll("\"", "\\\""));
                    } else {
                        LOG.error("解析流程条件出错" + conditionNode.get(i).get("field").get("TEXT").toString());
                    }
                    
                    conditionExpression.append(conditionNode.get(i).get("logic").get("VALUE").toString().replaceAll("\"", " "));
                }
                conditionExpression.append("}");
                flow.setConditionExpression(conditionExpression.toString().replaceAll("  ", " "));
            }
        }
        
        return flow;
    }
    
    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_SEQUENCE_FLOW;
    }
    
    protected void setFieldConditionExpression(SequenceFlow flow, JsonNode expressionNode) {
        String fieldId = null;
        if (expressionNode.get("fieldId") != null && !expressionNode.get("fieldId").isNull()) {
            fieldId = expressionNode.get("fieldId").asText();
        }
        
        String operator = null;
        if (expressionNode.get("operator") != null && !expressionNode.get("operator").isNull()) {
            operator = expressionNode.get("operator").asText();
        }
        
        String value = null;
        if (expressionNode.get("value") != null && !expressionNode.get("value").isNull()) {
            value = expressionNode.get("value").asText();
        }
        
        if (fieldId != null && operator != null && value != null) {
            flow.setConditionExpression("${" + fieldId + " " + operator + " " + value + "}");
            addExtensionElement("conditionFieldId", fieldId, flow);
            addExtensionElement("conditionOperator", operator, flow);
            addExtensionElement("conditionValue", value, flow);
        }
    }
    
    protected void setOutcomeConditionExpression(SequenceFlow flow, JsonNode expressionNode) {
        Long formId = null;
        if (expressionNode.get("outcomeFormId") != null && !expressionNode.get("outcomeFormId").isNull()) {
            formId = expressionNode.get("outcomeFormId").asLong();
        }
        
        String operator = null;
        if (expressionNode.get("operator") != null && !expressionNode.get("operator").isNull()) {
            operator = expressionNode.get("operator").asText();
        }
        
        String outcomeName = null;
        if (expressionNode.get("outcomeName") != null && !expressionNode.get("outcomeName").isNull()) {
            outcomeName = expressionNode.get("outcomeName").asText();
        }
        
        if (formId != null && operator != null && outcomeName != null) {
            flow.setConditionExpression("${form" + formId + "outcome " + operator + " " + outcomeName + "}");
            addExtensionElement("conditionFormId", String.valueOf(formId), flow);
            addExtensionElement("conditionOperator", operator, flow);
            addExtensionElement("conditionOutcomeName", outcomeName, flow);
        }
    }
    
    protected void addExtensionElement(String name, String value, SequenceFlow flow) {
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(NAMESPACE);
        extensionElement.setNamespacePrefix("modeler");
        extensionElement.setName(name);
        extensionElement.setElementText(value);
        flow.addExtensionElement(extensionElement);
    }
}
