package com.activiti.controller;

import com.activiti.model.ModelEditorJsonRestResource;
import com.activiti.model.ModelSaveRestResource;
import com.activiti.service.ProcessDesignService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.activiti.engine.repository.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Author yiyoung
 * @date 2020/4/21
 */
@RestController
@RequestMapping("/workflow")
public class ProcessDesignController {
    
    @Autowired
    private ProcessDesignService processDesignService;
    @Autowired
    private ModelSaveRestResource modelSaveRestResource;
    @Autowired
    private ModelEditorJsonRestResource modelEditorJsonRestResource;
    
    /**
     * 创建模型
     */
    @RequestMapping(value = "/model/insert", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void createModel(@RequestParam String key, @RequestParam String name, @RequestParam String category, @RequestParam String descp) throws UnsupportedEncodingException {
        processDesignService.createModel(key, name, category, descp);
    }
    
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public List<Model> listModel() {
        List<Model> listModel = processDesignService.listModel();
        return  listModel;
    }
    
    
    /**
     * 保存模型
     */
    @RequestMapping(value = "/model/{modelId}/xml/save", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModelXml(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        modelSaveRestResource.saveModelXml(modelId, values);
    }
    
    @ResponseBody
    @GetMapping(value = "/deleteModel")
    public void flowDelete(@RequestParam(name = "modelId") String modelId){
        processDesignService.deleteModel(modelId);
    }
    
    /**
     * 根据生成的ID获取模型流程编辑器
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/model/{modelId}/xml", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public JSONObject getEditorXml(@PathVariable String modelId) {
        return modelEditorJsonRestResource.getEditorXml(modelId);
    }
    
    @GetMapping(value = "/model/deploy")
    public String deploy(@RequestParam(name = "modelId") String modelId) throws Exception {
        return processDesignService.deployModel(modelId);
    }
}
