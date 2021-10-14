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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.ActivityProcessor;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Tijs Rademakers
 */
public class CustomUserTaskJsonConverter extends BaseBpmnJsonConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomUserTaskJsonConverter.class);
    
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,

    Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }
    
    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_USER, CustomUserTaskJsonConverter.class);
    }
    
    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, CustomUserTaskJsonConverter.class);
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
            flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode));
            
            BpmnJsonConverterUtil.convertJsonToListeners(elementNode, flowElement);
            
            if (baseElement instanceof Activity) {
                Activity activity = (Activity) baseElement;
                activity.setAsynchronous(getPropertyValueAsBoolean(PROPERTY_ASYNCHRONOUS, elementNode));
                activity.setNotExclusive(!getPropertyValueAsBoolean(PROPERTY_EXCLUSIVE, elementNode));
                // 多实例类型 注意看 TODO
                String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode);
                // 通过权重
                String multiInstanceCondition = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION, elementNode);
                if (StringUtils.isNotEmpty(multiInstanceType) && !"none".equalsIgnoreCase(multiInstanceType)) {
                    String name = getPropertyValueAsString(PROPERTY_NAME, elementNode);
                    
                    MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
                    if ("sequential".equalsIgnoreCase(multiInstanceType)) {
                        multiInstanceObject.setSequential(true);
                    } else {
                        multiInstanceObject.setSequential(false);
                    }
                    
                    if (StringUtils.isNotEmpty(multiInstanceCondition)) {
                        try {
                            Integer.valueOf(multiInstanceCondition);
                        } catch (Exception ex) {
                            LOG.error(name + "配置成了会签，但通过权重不是一个整数");
                        }
                        multiInstanceObject.setCompletionCondition("${nextTaskEvaluator.isComplete(execution," + multiInstanceCondition + ")}");
                    } else {
                        LOG.error(name + "配置成了会签，但没有配置通过权重");
                    }
                    // usertaskassignment 这块报错
                    JsonNode assignmentNode = getProperty(PROPERTY_USERTASK_ASSIGNMENT, elementNode);
                    if (assignmentNode != null) {
                        JsonNode assignmentDefNode = assignmentNode.get("assignment");
                        List<String> candidateGroups = null;
                        try {
                            candidateGroups = getValueAsList(PROPERTY_USERTASK_CANDIDATE_GROUPS, assignmentDefNode);
                        } catch (Exception e) {
                            LOG.error(name + "配置成了会签，必须要配置岗位");
                        }
                        
                        StringBuilder inputItemsStr = new StringBuilder("${nextTaskEvaluator.getCandiateUser(execution,");
                        boolean isFirst = true;
                        for (String temp : candidateGroups) {
                            if (isFirst) {
                                isFirst = false;
                            } else {
                                inputItemsStr.append(",");
                            }
                            inputItemsStr.append("\"").append(temp).append("\"");
                        }
                        inputItemsStr.append(")}");
                        // 不确定这里是不是loopDataInputRef ? TODO
                        multiInstanceObject.setInputDataItem(inputItemsStr.toString());
                        multiInstanceObject.setElementVariable("assignee");
                        
                        activity.setLoopCharacteristics(multiInstanceObject);
                    }
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
        UserTask userTask = (UserTask) baseElement;
        String assignee = userTask.getAssignee();
        String owner = userTask.getOwner();
        
        if (StringUtils.isNotEmpty(assignee) || StringUtils.isNotEmpty(owner) || CollectionUtils.isNotEmpty(userTask.getCandidateUsers()) ||
                CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
            
            ObjectNode assignmentNode = objectMapper.createObjectNode();
            ObjectNode assignmentValuesNode = objectMapper.createObjectNode();
            
            if (StringUtils.isNotEmpty(assignee)) {
                assignmentValuesNode.put(PROPERTY_USERTASK_ASSIGNEE, assignee);
            }
            
            if (StringUtils.isNotEmpty(owner)) {
                assignmentValuesNode.put(PROPERTY_USERTASK_OWNER, owner);
            }
            
            if (CollectionUtils.isNotEmpty(userTask.getCandidateUsers())) {
                ArrayNode candidateArrayNode = objectMapper.createArrayNode();
                for (String candidateUser : userTask.getCandidateUsers()) {
                    ObjectNode candidateNode = objectMapper.createObjectNode();
                    candidateNode.put("value", candidateUser);
                    candidateArrayNode.add(candidateNode);
                }
                assignmentValuesNode.put(PROPERTY_USERTASK_CANDIDATE_USERS, candidateArrayNode);
            }
            
            if (CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
                ArrayNode candidateArrayNode = objectMapper.createArrayNode();
                for (String candidateGroup : userTask.getCandidateGroups()) {
                    ObjectNode candidateNode = objectMapper.createObjectNode();
                    candidateNode.put("value", candidateGroup);
                    candidateArrayNode.add(candidateNode);
                }
                assignmentValuesNode.put(PROPERTY_USERTASK_CANDIDATE_GROUPS, candidateArrayNode);
            }
            
            assignmentNode.put("assignment", assignmentValuesNode);
            propertiesNode.put(PROPERTY_USERTASK_ASSIGNMENT, assignmentNode);
        }
        
        if (userTask.getPriority() != null) { // getAttributes().get("weights").get(0).getValue()
            setPropertyValue(PROPERTY_USERTASK_PRIORITY, userTask.getPriority().toString(), propertiesNode);
        }
        
        if (StringUtils.isNotEmpty(userTask.getFormKey())) {
            setPropertyValue(PROPERTY_FORMKEY, userTask.getFormKey(), propertiesNode);
        }
        
        setPropertyValue(PROPERTY_USERTASK_DUEDATE, userTask.getDueDate(), propertiesNode);
        setPropertyValue(PROPERTY_USERTASK_CATEGORY, userTask.getCategory(), propertiesNode);
        // 添加用户任务的自定义属性  start
        if (userTask.getAttributes().get("multiinstance_type") != null && !"".equals(userTask.getAttributes().get("multiinstance_type").get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_TYPE, userTask.getAttributes().get("multiinstance_type").get(0).getValue(), propertiesNode);
        }
        
        if (userTask.getAttributes().get("multiinstance_cardinality") != null && !"".equals(userTask.getAttributes().get("multiinstance_cardinality").get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_CARDINALITY, userTask.getAttributes().get("multiinstance_cardinality").get(0).getValue(), propertiesNode);
        }
        
        if (userTask.getAttributes().get("multiinstance_collection") != null && !"".equals(userTask.getAttributes().get("multiinstance_collection").get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_COLLECTION, userTask.getAttributes().get("multiinstance_collection").get(0).getValue(), propertiesNode);
        }
        if (userTask.getAttributes().get("multiinstance_variable") != null && !"".equals(userTask.getAttributes().get("multiinstance_variable").get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_VARIABLE, userTask.getAttributes().get("multiinstance_variable").get(0).getValue(), propertiesNode);
        }
        if (userTask.getAttributes().get("multiinstance_condition") != null && !"".equals(userTask.getAttributes().get("multiinstance_condition").get(0).getValue())) {
            setPropertyValue(PROPERTY_MULTIINSTANCE_CONDITION, userTask.getAttributes().get("multiinstance_condition").get(0).getValue(), propertiesNode);
        }
        
        
        // 添加默认流程属性
        if (StringUtils.isNotEmpty(userTask.getDefaultFlow())) {
            setPropertyValue("default", userTask.getDefaultFlow(), propertiesNode);
        }
        // end
        addFormProperties(userTask.getFormProperties(), propertiesNode);
    }
    
    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        UserTask task = new UserTask();
        task.setPriority(getPropertyValueAsString(PROPERTY_USERTASK_PRIORITY, elementNode));
        String formKey = getPropertyValueAsString(PROPERTY_FORMKEY, elementNode);
        String name = getPropertyValueAsString(PROPERTY_NAME, elementNode);
        if (StringUtils.isNotEmpty(formKey)) {
            task.setFormKey(formKey);
        } else {
            LOG.error(name + "没有配置表单");
        }
        task.setDueDate(getPropertyValueAsString(PROPERTY_USERTASK_DUEDATE, elementNode));
        task.setCategory(getPropertyValueAsString(PROPERTY_USERTASK_CATEGORY, elementNode));
        
        JsonNode assignmentNode = getProperty(PROPERTY_USERTASK_ASSIGNMENT, elementNode);
        if (assignmentNode != null) {
            String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode);
            if (StringUtils.isNotEmpty(multiInstanceType) && !"none".equalsIgnoreCase(multiInstanceType)) {
                task.setAssignee("${assignee}");
                task.setCandidateUsers(new ArrayList<String>());
                task.setCandidateGroups(new ArrayList<String>());
            } else {
                JsonNode assignmentDefNode = assignmentNode.get("assignment");
                if (assignmentDefNode != null) {
                    
                    JsonNode assigneeNode = assignmentDefNode.get(PROPERTY_USERTASK_ASSIGNEE);
                    if (assigneeNode != null && !assigneeNode.isNull()) {
                        task.setAssignee(assigneeNode.asText());
                    }
                    
                    JsonNode ownerNode = assignmentDefNode.get(PROPERTY_USERTASK_OWNER);
                    if (ownerNode != null && !ownerNode.isNull()) {
                        task.setOwner(ownerNode.asText());
                    }
                    
                    task.setCandidateUsers(getValueAsList(PROPERTY_USERTASK_CANDIDATE_USERS, assignmentDefNode));
                    task.setCandidateGroups(getValueAsList(PROPERTY_USERTASK_CANDIDATE_GROUPS, assignmentDefNode));
                }
            }
            
            
        }
        
        // 添加多实例属性
        List<CustomProperty> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION, elementNode))) {
            CustomProperty customProperty = new CustomProperty();
            customProperty.setName("multiinstance_condition");
            customProperty.setSimpleValue(getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION, elementNode));
            list.add(customProperty);
        }
        
        
        if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode))) {
            CustomProperty customProperty = new CustomProperty();
            customProperty.setName("multiinstance_type");
            customProperty.setSimpleValue(getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE, elementNode));
            list.add(customProperty);
        }
        
        task.setCustomProperties(list);
        
        // 添加默认流程属性
        if (StringUtils.isNotEmpty(getPropertyValueAsString("default", elementNode))) {
            task.setDefaultFlow(getPropertyValueAsString("default", elementNode));
        }
        
        convertJsonToFormProperties(elementNode, task);
        return task;
    }
    
    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_TASK_USER;
    }
}
