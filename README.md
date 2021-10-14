# 前端：vue+bpmn-js实现activiti的流程设计器，后端Springboot+Activiti开发工作流

> springboot + activiti 项目

鉴于广大程序员们不知道怎么解析自定义属性的问题，于是我就开发了这个基础版本的demo，供广大程序员们学习用，如果有问题请在issue中提问👏👏👏

广大程序员们对activit肯定比我熟悉，项目中有什么问题欢迎指出，也欢迎大家帮我一起完善demo

看我这么辛苦为你们整理demo，不给个star你们肯定都不好意思😄😄😄


## 一 启动项目🌟

启动类：VueActivitiServiceDemoApplication.java

## 二 详细介绍🌟

可以结合这篇文章进行阅读 ：https://juejin.im/post/5e7330c36fb9a07cd248ef00

实际业务中，有很多我们自定义的流程属性，用官方的代码已经没法满足场景了，这时候就需要我们自定义解析器

### 1 引入相关依赖🌟 

``` bash
<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-engine</artifactId>
  <version>5.22.0</version>
</dependency>

<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-spring</artifactId>
  <version>5.22.0</version>
</dependency>

<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-modeler</artifactId>
  <version>5.22.0</version>
</dependency>

<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-diagram-rest</artifactId>
  <version>5.22.0</version>
</dependency>
```

### 2 扩展节点解析器🌟 

可以结合这篇文章进行阅读 ：https://juejin.im/post/5e7330c36fb9a07cd248ef00

拿用户任务为例，分为json解析和xml解析分别为CustomUserTaskJsonConverter.java文件和UserTaskXMLConverter.java

后端在接收到前端传的xml文件时需要将xml转换成BpmnModel(这里可能会丢失自定义属性) 之后再转换成json(这里也可能会丢失属性)，所以需要我们扩展上面提到的两个类我文件

代码请见java文件

关于怎么使用请参考ModelEditorJsonRestResource.java和ModelSaveRestResource.java文件

> Q: 我这里为什么还要添加一个WebCustomUserTaskJsonConverter.java文件 ？

因为部署等执行需要解析xml，并且不能有自定义的属性，但反显到前端却需要自定义的属性，所以这里扩展这个类只为反显

### 结语🌟 

如果你有好的解决方案欢迎私我，让我学习学习🤔️

针对前端开发程序员们，我也整理了vue + bpmn-js的demo，请从项目中找📒

针对react + bpmn-js 的项目请从我的github上找

针对前后端没有分离，可以参考我之前写的activiti项目，从我的github上找

加油呀💪💪💪💪


