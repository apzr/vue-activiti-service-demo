package com.activiti.service.impl;

import com.activiti.service.ProcessDesignService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Author yiyoung
 * @date 2020/4/21
 */
@Service
public class ProcessDesignServiceImpl implements ProcessDesignService {
    
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 保存模型
     * @param key
     * @param name
     * @param category
     * @param descp
     * @throws UnsupportedEncodingException
     */
    @Override
    public void createModel(String key,String name, String category, String descp) throws UnsupportedEncodingException{
        //初始化一个空模型
        Model model = repositoryService.newModel();
        //设置一些默认信息
        String modelName = name;
        String description = descp;
        int revision = 1;
        String modelKey = key;
    
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME,modelName);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);
    
        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(modelNode.toString());
    
        repositoryService.saveModel(model);
        String id = model.getId();
    
        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://activiti.org/bpmn");
        editorNode.put("stencilset", stencilSetNode);
    
        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        return;
    }
    
    /**
     * 查询模型
     * @return
     */
    @Override
    public List<Model> listModel() {
        return repositoryService.createModelQuery().list();
    }
    
    /**
     * 删除模型
     * @param modelId
     */
    @Override
    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }
    
    /**
     * 部署流程
     * @param modelId
     */
    @Override
    public String deployModel(String modelId) throws Exception {
        // 获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if(null == bytes) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }
        JsonNode modelNode = objectMapper.readTree(bytes);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (model.getProcesses().size() == 0){
            return "数据模型不符合要求，请至少设计一条主线程流。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
    
        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return "success";
    }
}
