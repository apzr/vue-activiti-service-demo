package com.activiti.model;

import com.activiti.utils.BpmnConverterUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author crh
 */
@Service
public class ModelEditorJsonRestResource implements ModelDataJsonConstants {
    private static final Logger LOG = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);
    
    @Autowired
    private RepositoryService repositoryService;
    
    /**
     * 根据生成的ID获取模型流程编辑器
     * @param modelId
     * @return
     */
    public JSONObject getEditorXml(@PathVariable String modelId) {
        JSONObject jsonObject = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    jsonObject = JSON.parseObject(model.getMetaInfo());
                } else {
                    jsonObject = new JSONObject();
                    jsonObject.put(MODEL_NAME, model.getName());
                }
                jsonObject.put(MODEL_ID, model.getId());
                JSONObject editorJsonNode = JSON.parseObject(new String(repositoryService.getModelEditorSource(model.getId())));
                //将json流程转为标准xml流程图
                String bpmnXml = BpmnConverterUtil.converterJsonToWebXml(editorJsonNode.toJSONString());
                jsonObject.put("bpmnXml", bpmnXml);
            } catch (Exception e) {
                LOG.error("创建model的json串失败", e);
                throw new ActivitiException("无法读取model信息", e);
            }
        } else {
            LOG.error("创建model的json串失败[{}]", modelId);
            throw new ActivitiException("未找到对应模型信息");
        }
        return jsonObject;
    }
}
