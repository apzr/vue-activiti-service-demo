package com.activiti.converter;



import org.activiti.bpmn.converter.BaseBpmnXMLConverter;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.IOParameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yiyoung
 * @date 2020/3/12
 */
public class CallActivityXMLConverter extends BaseBpmnXMLConverter {
    private static final Logger LOG = LoggerFactory.getLogger(CallActivityXMLConverter.class);
    
    /**
     * default attributes taken from bpmn spec and from activiti extension
     */
    protected static final List<ExtensionAttribute> defaultCallActivityAttributes = Arrays
            .asList(new ExtensionAttribute(null, ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT),
                    new ExtensionAttribute(null, ELEMENT_CALL_ACTIVITY_IN_PARAMETERS),
                    new ExtensionAttribute(null, ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS));
    
    protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();
    
    public CallActivityXMLConverter() {
        InParameterParser inParameterParser = new InParameterParser();
        childParserMap.put(inParameterParser.getElementName(), inParameterParser);
        OutParameterParser outParameterParser = new OutParameterParser();
        childParserMap.put(outParameterParser.getElementName(), outParameterParser);
    }
    
    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return CallActivity.class;
    }
    
    @Override
    protected String getXMLElementName() {
        return ELEMENT_CALL_ACTIVITY;
    }
    
    @Override
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
        // 保存流程会走到这里
        CallActivity callActivity = new CallActivity();
        BpmnXMLUtil.addXMLLocation(callActivity, xtr);
        String documentation = xtr.getAttributeValue(null, "documentation");
        callActivity.setDocumentation(documentation);
        // 子流程
        String calledElement = xtr.getAttributeValue(null, "calledElement");
        if(StringUtils.isNotEmpty(calledElement)) {
            callActivity.setCalledElement(calledElement);
        }
        // 全部的属性都在这里
        BpmnXMLUtil.addCustomAttributes(xtr, callActivity, defaultElementAttributes,
                defaultActivityAttributes, defaultCallActivityAttributes);
        parseChildElements(getXMLElementName(), callActivity, childParserMap, model, xtr);
        
        return callActivity;
    }
    
    @Override
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
        // 流程反显和部署流程时都会走此处
        CallActivity callActivity = (CallActivity) element;
        if (StringUtils.isNotEmpty(callActivity.getCalledElement())) {
            xtw.writeAttribute(ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT, callActivity.getCalledElement());
        }
        if (StringUtils.isNotEmpty(callActivity.getDocumentation())) {
            xtw.writeAttribute("activiti:documentation", callActivity.getDocumentation());
        }
    
        // 将documentation属性值置空，避免生成<documentation></documentation>标签
        callActivity.setDocumentation(null);
        
        // write custom attributes
        BpmnXMLUtil.writeCustomAttributes(callActivity.getAttributes().values(), xtw,
                defaultElementAttributes, defaultActivityAttributes,
                defaultCallActivityAttributes);
    }
    
    @Override
    protected boolean writeExtensionChildElements(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
        CallActivity callActivity = (CallActivity) element;
        didWriteExtensionStartElement = writeIOParameters(ELEMENT_CALL_ACTIVITY_IN_PARAMETERS, callActivity.getInParameters(), didWriteExtensionStartElement, xtw);
        didWriteExtensionStartElement = writeIOParameters(ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS, callActivity.getOutParameters(), didWriteExtensionStartElement, xtw);
        return didWriteExtensionStartElement;
    }
    
    @Override
    protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    }
    
    private boolean writeIOParameters(String elementName, List<IOParameter> parameterList, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
        if (parameterList.isEmpty()){
            return didWriteExtensionStartElement;
        }
        
        for (IOParameter ioParameter : parameterList) {
            if (didWriteExtensionStartElement == false) {
                xtw.writeStartElement(ELEMENT_EXTENSIONS);
                didWriteExtensionStartElement = true;
            }
            
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, elementName, ACTIVITI_EXTENSIONS_NAMESPACE);
            if (StringUtils.isNotEmpty(ioParameter.getSource())) {
                writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_SOURCE, ioParameter.getSource(), xtw);
            }
            if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
                writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION, ioParameter.getSourceExpression(), xtw);
            }
            if (StringUtils.isNotEmpty(ioParameter.getTarget())) {
                writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_TARGET, ioParameter.getTarget(), xtw);
            }
            
            xtw.writeEndElement();
        }
        
        return didWriteExtensionStartElement;
    }
    
    public class InParameterParser extends BaseChildElementParser {
        @Override
        public String getElementName() {
            return ELEMENT_CALL_ACTIVITY_IN_PARAMETERS;
        }
    
        @Override
        public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
            String source = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE);
            String sourceExpression = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION);
            String target = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_TARGET);
            if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && StringUtils.isNotEmpty(target)) {
                
                IOParameter parameter = new IOParameter();
                if(StringUtils.isNotEmpty(sourceExpression)) {
                    parameter.setSourceExpression(sourceExpression);
                } else {
                    parameter.setSource(source);
                }
                
                parameter.setTarget(target);
                
                ((CallActivity) parentElement).getInParameters().add(parameter);
            }
        }
    }
    
    public class OutParameterParser extends BaseChildElementParser {
    
        @Override
        public String getElementName() {
            return ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS;
        }
    
        @Override
        public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
            String source = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE);
            String sourceExpression = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION);
            String target = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_TARGET);
            if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && StringUtils.isNotEmpty(target)) {
                
                IOParameter parameter = new IOParameter();
                if(StringUtils.isNotEmpty(sourceExpression)) {
                    parameter.setSourceExpression(sourceExpression);
                } else {
                    parameter.setSource(source);
                }
                
                parameter.setTarget(target);
                
                ((CallActivity) parentElement).getOutParameters().add(parameter);
            }
        }
    }
}
