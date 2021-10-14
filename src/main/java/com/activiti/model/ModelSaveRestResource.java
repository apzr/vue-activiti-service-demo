package com.activiti.model;

import com.activiti.utils.BpmnConverterUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.repository.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author yiyoung
 * @date 2020/4/20
 */

@Service
public class ModelSaveRestResource implements ModelDataJsonConstants {
    private static final Logger LOG = LoggerFactory
            .getLogger(ModelSaveRestResource.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;
    
    
    /**
     * 保存模型
     */
    public void saveModelXml(@PathVariable String modelId,
                             @RequestBody MultiValueMap<String, String> values) {
        ByteArrayOutputStream outStream = null;
        try {

            Model model = repositoryService.getModel(modelId);
            // 获取模型信息
            ObjectNode modelJson = (ObjectNode) objectMapper
                    .readTree(model.getMetaInfo());
            // 获取value第一个元素
            modelJson.put(MODEL_NAME, model.getName());
            modelJson.put(MODEL_DESCRIPTION, modelJson.get("description"));
            model.setMetaInfo(modelJson.toString());
            // 版本
            model.setVersion(model.getVersion() + 1);
            repositoryService.saveModel(model);
            String bpmnXml = BpmnConverterUtil
                    .converterXmlToJson(values.getFirst("bpmn_xml")).toString();
            repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes("utf-8"));
            repositoryService.addModelEditorSourceExtra(model.getId(),
                    values.getFirst("svg_xml").getBytes("utf-8"));
        } catch (Exception e) {
            LOG.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }
}
