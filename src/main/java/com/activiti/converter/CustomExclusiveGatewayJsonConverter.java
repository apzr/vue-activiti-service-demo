package com.activiti.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author yiyoung  2020/2/26.
 */
public class CustomExclusiveGatewayJsonConverter extends BaseBpmnJsonConverter {
    private static final Logger LOG = LoggerFactory.getLogger(CustomExclusiveGatewayJsonConverter.class);
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }
    
    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_GATEWAY_EXCLUSIVE, CustomExclusiveGatewayJsonConverter.class);
    }
    
    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(ExclusiveGateway.class, CustomExclusiveGatewayJsonConverter.class);
    }
    
    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_GATEWAY_EXCLUSIVE;
    }
    
    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        // 将default属性加上json中
        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) baseElement;
        if (StringUtils.isNotEmpty(exclusiveGateway.getDefaultFlow())) {
            propertiesNode.put("default", exclusiveGateway.getDefaultFlow());
        }
    }
    
    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        ExclusiveGateway gateway = new ExclusiveGateway();
        if (StringUtils.isNotEmpty(getPropertyValueAsString("default", elementNode))) {
            // 将default属性赋值到gateway类中
            gateway.setDefaultFlow(getPropertyValueAsString("default", elementNode));
        }
        return gateway;
    }
}
