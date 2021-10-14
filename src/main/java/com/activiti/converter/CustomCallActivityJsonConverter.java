/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.converter;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.IOParameter;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.editor.language.json.converter.ActivityProcessor;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tijs Rademakers
 */
public class CustomCallActivityJsonConverter extends BaseBpmnJsonConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomCallActivityJsonConverter.class);

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,

    Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_CALL_ACTIVITY, CustomCallActivityJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(CallActivity.class, CustomCallActivityJsonConverter.class);
    }

    @Override
    public void convertToBpmnModel(JsonNode elementNode, JsonNode modelNode, ActivityProcessor processor, BaseElement parentElement,
                                   Map<String, JsonNode> shapeMap, BpmnModel bpmnModel) {

        this.processor = processor;
        this.model = bpmnModel;

        BaseElement baseElement = convertJsonToElement(elementNode, modelNode, shapeMap);
        baseElement.setId(BpmnJsonConverterUtil.getElementId(elementNode));

        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            flowElement.setName(getPropertyValueAsString(PROPERTY_NAME, elementNode));
            // 获取documentation属性
            flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode));

            BpmnJsonConverterUtil.convertJsonToListeners(elementNode, flowElement);

            if (baseElement instanceof Activity) {
                Activity activity = (Activity) baseElement;
                activity.setAsynchronous(getPropertyValueAsBoolean(PROPERTY_ASYNCHRONOUS, elementNode));
                activity.setNotExclusive(!getPropertyValueAsBoolean(PROPERTY_EXCLUSIVE, elementNode));
                // 多实列 注意看
                String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode);
                //String multiInstanceCardinality = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode);
                //String multiInstanceCollection = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_COLLECTION, elementNode);
                String multiInstanceCondition = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION, elementNode);

                if (StringUtils.isNotEmpty(multiInstanceType) && !"none".equalsIgnoreCase(multiInstanceType)) {
                    String name = getPropertyValueAsString(PROPERTY_NAME, elementNode);
                    //String multiInstanceVariable = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_VARIABLE, elementNode);

                    MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
                    if ("sequential".equalsIgnoreCase(multiInstanceType)) {
                        multiInstanceObject.setSequential(true);
                    } else {
                        multiInstanceObject.setSequential(false);
                    }

                    JsonNode collectionNode = getProperty(PROPERTY_MULTIINSTANCE_COLLECTION, elementNode);
                    if (collectionNode != null) {
                        if (collectionNode.isObject() && collectionNode.get("sqlId") != null && !collectionNode.get("sqlId").toString().equals("") && !collectionNode.get("sqlId").toString().equals("\"\"")) {
                            multiInstanceObject.setInputDataItem("${nextProcessEvaluator.getMultiList(" + collectionNode.get("sqlId") + ",execution)}");
                        } else {
                            LOG.error(name + "配置成了多实例，但集合属性配置有误");
                        }
                    } else {
                        LOG.error(name + "配置成了多实例，但没有配置集合属性");
                    }
                    if ("one".equalsIgnoreCase(multiInstanceCondition)) {
                        multiInstanceObject.setCompletionCondition("${nrOfCompletedInstances==1}");
                    } else {
                        multiInstanceObject.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");
                    }

                    // multiInstanceObject.setLoopCardinality(multiInstanceCardinality);
                    multiInstanceObject.setElementVariable("nextProcess");
                    activity.setLoopCharacteristics(multiInstanceObject);
                }

            } else if (baseElement instanceof Gateway) {
                // 网关流程顺序设置
                JsonNode flowOrderNode = getProperty(PROPERTY_SEQUENCEFLOW_ORDER, elementNode);
                if (flowOrderNode != null) {
                    flowOrderNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(flowOrderNode);
                    JsonNode orderArray = flowOrderNode.get("sequenceFlowOrder");
                    if (orderArray != null && orderArray.size() > 0) {
                        for (JsonNode orderNode : orderArray) {
                            ExtensionElement orderElement = new ExtensionElement();
                            orderElement.setName("EDITOR_FLOW_ORDER");
                            orderElement.setElementText(orderNode.asText());
                            flowElement.addExtensionElement(orderElement);
                        }
                    }
                }
            }
        }

        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            if (flowElement instanceof SequenceFlow) {
                ExtensionElement idExtensionElement = new ExtensionElement();
                idExtensionElement.setName("EDITOR_RESOURCEID");
                idExtensionElement.setElementText(elementNode.get(EDITOR_SHAPE_ID).asText());
                flowElement.addExtensionElement(idExtensionElement);
            }

            if (parentElement instanceof Process) {
                ((Process) parentElement).addFlowElement(flowElement);

            } else if (parentElement instanceof SubProcess) {
                ((SubProcess) parentElement).addFlowElement(flowElement);

            } else if (parentElement instanceof Lane) {
                Lane lane = (Lane) parentElement;
                lane.getFlowReferences().add(flowElement.getId());
                lane.getParentProcess().addFlowElement(flowElement);
            }

        } else if (baseElement instanceof Artifact) {
            Artifact artifact = (Artifact) baseElement;
            if (parentElement instanceof Process) {
                ((Process) parentElement).addArtifact(artifact);

            } else if (parentElement instanceof SubProcess) {
                ((SubProcess) parentElement).addArtifact(artifact);

            } else if (parentElement instanceof Lane) {
                Lane lane = (Lane) parentElement;
                lane.getFlowReferences().add(artifact.getId());
                lane.getParentProcess().addArtifact(artifact);
            }
        }
    }

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        // 保存流程图时用到
        CallActivity callActivity = (CallActivity) baseElement;
        if (StringUtils.isNotEmpty(callActivity.getCalledElement())) {
            propertiesNode.put("calledElement", callActivity.getCalledElement());
        }
        

        addJsonParameters(PROPERTY_CALLACTIVITY_IN, "inParameters", callActivity.getInParameters(), propertiesNode);
        addJsonParameters(PROPERTY_CALLACTIVITY_OUT, "outParameters", callActivity.getOutParameters(), propertiesNode);
    }

    private void addJsonParameters(String propertyName, String valueName, List<IOParameter> parameterList, ObjectNode propertiesNode) {
        ObjectNode parametersNode = objectMapper.createObjectNode();
        ArrayNode itemsNode = objectMapper.createArrayNode();
        for (IOParameter parameter : parameterList) {
            ObjectNode parameterItemNode = objectMapper.createObjectNode();
            if (StringUtils.isNotEmpty(parameter.getSource())) {
                parameterItemNode.put(PROPERTY_IOPARAMETER_SOURCE, parameter.getSource());
            } else {
                parameterItemNode.putNull(PROPERTY_IOPARAMETER_SOURCE);
            }
            if (StringUtils.isNotEmpty(parameter.getTarget())) {
                parameterItemNode.put(PROPERTY_IOPARAMETER_TARGET, parameter.getTarget());
            } else {
                parameterItemNode.putNull(PROPERTY_IOPARAMETER_TARGET);
            }
            if (StringUtils.isNotEmpty(parameter.getSourceExpression())) {
                parameterItemNode.put(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, parameter.getSourceExpression());
            } else {
                parameterItemNode.putNull(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION);
            }

            itemsNode.add(parameterItemNode);
        }

        parametersNode.put(valueName, itemsNode);
        propertiesNode.put(propertyName, parametersNode);
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        // 部署流程时会用到此处
        CallActivity callActivity = new CallActivity();
        String name = getPropertyValueAsString(PROPERTY_NAME, elementNode);
        if (StringUtils.isEmpty(name)) {
            LOG.error("节点名称不能为空");
        }
        String atomCategory = getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode);
        if (StringUtils.isEmpty(atomCategory)) {
            LOG.error(name + "的文档属性值被清空了");
        }
    
        JSONObject callElementNode = JSON.parseObject(getPropertyValueAsString("calledElement", elementNode));
        
        if (callElementNode != null) {
            if (callElementNode.get("sqlId") != null && !callElementNode.get("sqlId").toString().equals("") && !callElementNode.get("sqlId").toString().equals("\"\"")) {
                if (callElementNode.get("type") != null && (callElementNode.get("type").toString().equals("tenantId") || callElementNode.get("type").toString().equals("\"tenantId\""))) {
                    callActivity.setCalledElement("${nextProcessEvaluator.returnTenantProcessDefinitionToCall(\"" + callElementNode.get("sqlId") + "\",\"" + atomCategory + "\",execution)}");
                } else {
                    callActivity.setCalledElement("${nextProcessEvaluator.returnSqlProcessDefinitionToCall(\"" + callElementNode.get("sqlId") + "\",\"" + atomCategory + "\",execution)}");
                }

            } else {
                callActivity.setCalledElement("${nextProcessEvaluator.returnDefaultProcessDefinitionToCall(\"" + atomCategory + "\",execution)}");
            }
        } else {
            callActivity.setCalledElement("${nextProcessEvaluator.returnDefaultProcessDefinitionToCall(\"" + atomCategory + "\",execution)}");
        }

        callActivity.getInParameters().addAll(convertToIOParameters(PROPERTY_CALLACTIVITY_IN, "inParameters", elementNode));
        callActivity.getOutParameters().addAll(convertToIOParameters(PROPERTY_CALLACTIVITY_OUT, "outParameters", elementNode));

        return callActivity;
    }

    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_CALL_ACTIVITY;
    }

    private List<IOParameter> convertToIOParameters(String propertyName, String valueName, JsonNode elementNode) {
        List<IOParameter> ioParameters = new ArrayList<IOParameter>();
        JsonNode parametersNode = getProperty(propertyName, elementNode);
        if (parametersNode != null) {
            parametersNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(parametersNode);
            JsonNode itemsArrayNode = parametersNode.get(valueName);
            if (itemsArrayNode != null) {
                for (JsonNode itemNode : itemsArrayNode) {
                    JsonNode sourceNode = itemNode.get(PROPERTY_IOPARAMETER_SOURCE);
                    JsonNode sourceExpressionNode = itemNode.get(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION);
                    if ((sourceNode != null && StringUtils.isNotEmpty(sourceNode.asText()))
                            || (sourceExpressionNode != null && StringUtils.isNotEmpty(sourceExpressionNode.asText()))) {

                        IOParameter parameter = new IOParameter();
                        if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_SOURCE, itemNode))) {
                            parameter.setSource(getValueAsString(PROPERTY_IOPARAMETER_SOURCE, itemNode));
                        } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, itemNode))) {
                            parameter.setSourceExpression(getValueAsString(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, itemNode));
                        }
                        if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_TARGET, itemNode))) {
                            parameter.setTarget(getValueAsString(PROPERTY_IOPARAMETER_TARGET, itemNode));
                        }
                        ioParameters.add(parameter);
                    }
                }
            }
        }
        return ioParameters;
    }
}
