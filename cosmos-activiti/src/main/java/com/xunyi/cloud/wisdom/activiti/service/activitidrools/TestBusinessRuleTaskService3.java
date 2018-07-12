package com.xunyi.cloud.wisdom.activiti.service.activitidrools;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.xunyi.cloud.wisdom.activiti.service.BaseService;
import com.xunyi.cloud.wisdom.activiti.util.ActivitiUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.conf.MaxThreadsOption;
import org.drools.impl.adapters.KnowledgeBaseConfigurationAdapter;
import org.drools.io.ResourceFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试：
 *  1. 测试drools引入java Map等类，为什么规则没有触发执行？？ 在TestBusinessRuleTaskService2 试过
 *  2. 多个规则节点 BusinessRuleTask ，多服务分布式执行时，彼此间规则执行的结果 update(map);后，
 *  值是否会传递？（即：第一个规则节点的规则决策，作为第二个规则节点的规则条件）
 *
 *  //?? 为什么这条规则引入java后，Map 规则没有匹配，命中执行？？？
 //答：BusinessRuleTaskActivityBehavior 会执行fact的插入
 //test.drl 如果 rule1 需要执行，必须插入Map实例
 //因为 rule1 的when条件使用的map
 //BusinessRuleTaskActivityBehavior

 2.同一个服务(同一个进程)，同一个流程第一个节点执行的决策结果可以作为第二个节点的规则条件 （参照 BusinessRuleTaskActivityBehavior，同一个Knowledge） 【OK】
3.分布式 不同服务（不同的进程），同一个流程，第一个节点执行的决策结果可以作为第二个节点的规则条件??  【OK】
4. 为什么 BusinessRuleTask 的入参需要是 ${xxxx} 形式

 List<String> inputVariables = new ArrayList<>();
 //test.drl 如果 rule1 需要执行，必须插入Map实例
 //因为 rule1 的when条件使用的map
 inputVariables.add("${map}");
 businessRuleTask.setInputVariables(inputVariables);

 ----------------------------------------------------------------------------------------------------------


 */
@Service
public class TestBusinessRuleTaskService3 extends BaseService {

    private static final String deploy_name_prefix = "deploy_name_";
    private static final String proc_def_name_prefix = "proc_def_name_";
    private static final String proc_def_id_prefix = "proc_def_id_";
    private static final String bpmn_model_name_prefix = "bpmn_model_name_";


    /**
     *
     *
     * @param strategyname
     */
    public void createDeployment(String strategyname){
        //1. 对旧的部署文件已经删除
        //分布式锁

        List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentName(deploy_name_prefix+strategyname).list();
        if(CollectionUtils.isNotEmpty(deployments)){
            logger.info("deployname={},部署文件已经存在，需删除，重新生成",deploy_name_prefix+strategyname);

            //注意删除失败的情况
            //存在进行中的任务或历史用例及任务
            //可以先暂停该流程图，在此基础上创建新的流程
            try{
                //删除前需要先挂起，不允许发起这个流程定义的流程实例
//                repositoryService.suspendProcessDefinitionById(deployment.getId());
                //挂起流程定义及级联挂起流程下面的实例
//                repositoryService.suspendProcessDefinitionById(deployment.getId(),true,null);

//                repositoryService.deleteDeployment(deployment.getId());
                //级联删除流程
                for(Deployment deployment: deployments){
                    repositoryService.deleteDeployment(deployment.getId(),true);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            logger.info("流程文件不存在。名称：{}",deploy_name_prefix+strategyname);
        }

        logger.warn("准备流程的重新部署.................");

        // 1. Build up the model from scratch
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        model.addProcess(process);
        process.setId(proc_def_id_prefix+strategyname);
        process.setName(proc_def_name_prefix+strategyname);

        process.addFlowElement(ActivitiUtils.createStartEvent());
        process.addFlowElement(ActivitiUtils.createUserTask("uid","userTask","ts"));
        process.addFlowElement(ActivitiUtils.createUserTask("uid2","userTask","ts"));

        List<String> ruleNames = ruleContextList().stream().map(map->
             String.valueOf(map.get("ruleName"))
        ).collect(Collectors.toList());
        logger.info("ruleNames:{}",JSON.toJSONString(ruleNames));
        process.addFlowElement(ActivitiUtils.businessRuleTask("bis_id","RuleTaskNode",ruleNames));

        List<String> ruleNames_4 = ruleContextList_4().stream().map(map->
                String.valueOf(map.get("ruleName"))
        ).collect(Collectors.toList());
        logger.info("ruleNames:{}",JSON.toJSONString(ruleNames_4));
        process.addFlowElement(ActivitiUtils.businessRuleTask("bis_id2","RuleTaskNode_2",ruleNames_4));





        process.addFlowElement(ActivitiUtils.createEndEvent());

        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "uid"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("uid", "bis_id"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("bis_id", "uid2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("uid2", "bis_id2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("bis_id2", "end"));

        // 2. Generate graphical information
        new BpmnAutoLayout(model).execute();

        /*Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(bpmn_model_name_prefix+ strategyname + ".bpmn", model).name(deploy_name_prefix+strategyname).deploy();*/

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        List<Map<String, Object>> ruleContextList = ruleContextList();
        if (!CollectionUtils.isEmpty(ruleContextList)) {
            for (Map<String, Object> ruleMap : ruleContextList) {
                logger.info("加载规则：{}",String.valueOf(ruleMap.get("ruleName")) + ".drl");
                logger.info("加载规则内容：{}",String.valueOf(ruleMap.get("drlContext")));

                //后缀名 .drl 由 RulesDeployer 加载解析
                deploymentBuilder
                                .addString(String.valueOf(ruleMap.get("ruleName")) + ".drl",String.valueOf(ruleMap.get("drlContext")));
            }
        }

        List<Map<String, Object>> ruleContextList_4 = ruleContextList_4();
        if (!CollectionUtils.isEmpty(ruleContextList_4)) {
            for (Map<String, Object> ruleMap : ruleContextList_4) {
                logger.info("加载规则：{}",String.valueOf(ruleMap.get("ruleName")) + ".drl");
                logger.info("加载规则内容：{}",String.valueOf(ruleMap.get("drlContext")));

                //后缀名 .drl 由 RulesDeployer 加载解析
                deploymentBuilder
                        .addString(String.valueOf(ruleMap.get("ruleName")) + ".drl",String.valueOf(ruleMap.get("drlContext")));
            }
        }

        //后缀名 .bpmn 由 BpmnDeployer 加载解析
        deploymentBuilder
                        .addBpmnModel(bpmn_model_name_prefix+ strategyname + ".bpmn", model)
                        .name(deploy_name_prefix+strategyname)
                        .deploy();

//      拓展点：
//        RulesDeployer
//        BusinessRuleTaskActivityBehavior
        logger.warn("流程的重新部署................[完成].");

      /*  if (deployment != null) {

            //====================== 出现空指针异常！！！=================================================
            DeploymentEntityManager deploymentEntityManager =
                    Context
                            .getCommandContext()
                            .getDeploymentEntityManager();

            DeploymentEntity deploymentEntity = deploymentEntityManager.findDeploymentById(deployment.getId());

            List<Map<String, Object>> ruleContextList = ruleContextList();
            if (!org.springframework.util.CollectionUtils.isEmpty(ruleContextList)) {
                for (Map<String, Object> ruleMap : ruleContextList) {
                    ResourceEntity resourceEntity = new ResourceEntity();
                    resourceEntity.setId(SUID.getUUID());
                    resourceEntity.setName(String.valueOf(ruleMap.get("ruleName")) + ".drl");
                    resourceEntity.setBytes(String.valueOf(ruleMap.get("drlContext")).getBytes());
                    resourceEntity.setDeploymentId(deployment.getId());

                    deploymentEntity.addResource(resourceEntity);
                }
            }

            RulesDeployer rulesDeployer = new RulesDeployer();
            rulesDeployer.deploy(deploymentEntity, null);



        }*/

        //加载关联规则 == 可以用于检查规则是否编辑有误
        KnowledgeBase knowledgeBase = buildComplexFlowProcess(ruleContextList());
        logger.warn("流程的<规则>部署................[完成].");

    }


    /**
     * 测试变量
     * 引入java、工具类
     * 问题：引入java包 Map的操作，结果 bpm_flowgroup_1 不执行？？
     *  ** 流程启动时，需要进行传参
     *  参照 第16章 集成规则引擎
     * @return
     */
    public static List<Map<String, Object>> ruleContextList(){
        List<Map<String, Object>> ruleContextList = new ArrayList<>();
        Map<String,Object> rule1Map = new HashMap<>();
        Map<String,Object> rule2Map = new HashMap<>();
        Map<String,Object> rule3Map = new HashMap<>();
//        BusinessRuleTaskActivityBehavior


        //?? 为什么这条规则引入java后，Map 规则没有匹配，命中执行？？？
        //答：BusinessRuleTaskActivityBehavior 会执行fact的插入
        //test.drl 如果 rule1 需要执行，必须插入Map实例
        //因为 rule1 的when条件使用的map
        //BusinessRuleTaskActivityBehavior
        String rule1 = "package com.sample;\n" +
                "\n" +
                "\n" +
                "import java.util.Map;\n" +
                "dialect \"java\" \n" +
                "\n" +
                "\n" +
                "rule \"biz_rule_1\"  \n" +
                "salience 0 \n" +
                "no-loop true  \n" +
                "lock-on-active true  \n" +
                "when \n" +
//                " \teval(true)\n" +
                " \tmap:Map()\n" +
//                "map:Map(this.get(\"yysContactLoanOrg\") == null)" +
                " \t\t\n" +
                "then \n" +
                "\tmap.put(\"result\",\"pass\");\t\n" +
                "\tSystem.out.println(\"===*******=======\"+drools.getRule());\n" +
                "\tSystem.out.println(map);\n" +
                " \tupdate(map);\n" +
                "\t\n" +
                "end ";

        rule1Map.put("drlContext",rule1);//rule1.getBytes(Charset.forName("UTF-8"))
        rule1Map.put("ruleName","biz_rule_1");

        String rule2 = "package bpm;\n" +
                "rule \"biz_rule_2\"\n" +
                "no-loop true \n" +
                "lock-on-active true\n" +
                "salience 63\n" +
//                "ruleflow-group \"test1\" \n" + //规则组：使用jbpm 的businessRuleTask ，
//                                                  属性规则组需要绑定drools里面的规则组；使用Activiti的，直接绑定规则名即可；
//                "activation-group \"activation-group1\"\n" +  //测试独占执行，只是在规则中添加属性 activation-group
                "    when\n" +
                "        eval(true)\n" +
                "    then\n" +
                "        System.out.println(\"flowgroup_2   执行\");\n" +
                "        System.out.println(drools.getRule());\n" +
                "        System.out.println(\"------------222-------------------\");\n" +
                "        System.out.println(\"-------------222------------------\");\n" +
                "        \n" +
                "end";
        rule2Map.put("drlContext",rule2);
        //rule2.getBytes(Charset.forName("UTF-8"))
        rule2Map.put("ruleName","biz_rule_2");



        String rule3 = "package bpm;\n" +
                "rule \"biz_rule_3\"\n" +
                "no-loop true \n" +
                "lock-on-active true\n" +
                "salience 66\n" +
//                "ruleflow-group \"test1\" \n" + //规则组：使用jbpm 的businessRuleTask ，
//                                                  属性规则组需要绑定drools里面的规则组；使用Activiti的，直接绑定规则名即可；
//                "activation-group \"activation-group1\"\n" +  //测试独占执行，只是在规则中添加属性 activation-group
                "    when\n" +
                "        eval(true)\n" +
                "    then\n" +
                "        System.out.println(\"flowgroup_3   执行\");\n" +
                "        System.out.println(drools.getRule());\n" +
                "        System.out.println(\"------------33333-------------------\");\n" +
                "        System.out.println(\"-------------3333333------------------\");\n" +
                "        \n" +
                "end";
        rule3Map.put("drlContext",rule3);
        //rule2.getBytes(Charset.forName("UTF-8"))
        rule3Map.put("ruleName","biz_rule_3");


        ruleContextList.add(rule1Map);
        ruleContextList.add(rule2Map);
        ruleContextList.add(rule3Map);//规则名不能相同，流程内，全局唯一
        return ruleContextList;
    }


    public static List<Map<String, Object>> ruleContextList_4(){
        List<Map<String, Object>> ruleContextList = new ArrayList<>();

        Map<String,Object> rule4Map = new HashMap<>();

        String rule4 = "package bpm;\n" +
                "\n" +
                "\n" +
                "import java.util.Map;\n" +
                "dialect \"java\" \n" +
                "\n" +
                "\n" +
                "rule \"biz_rule_4\"\n" +
                "no-loop true \n" +
                "lock-on-active true\n" +
                "salience 66\n" +
//                "ruleflow-group \"test1\" \n" + //规则组：使用jbpm 的businessRuleTask ，
//                                                  属性规则组需要绑定drools里面的规则组；使用Activiti的，直接绑定规则名即可；
//                "activation-group \"activation-group1\"\n" +  //测试独占执行，只是在规则中添加属性 activation-group
                "    when\n" +
                "       map:Map(this.get(\"result\") == \"pass\") \n" +
                "    then\n" +
                "        System.out.println(\"flowgroup_4   执行\");\n" +
                "        System.out.println(drools.getRule());\n" +
                "        System.out.println(\"------------44444-------------------\");\n" +
                "        System.out.println(\"-------------44444------------------\");\n" +
                        "\tmap.put(\"name\",\"thomas\");\t\n" +
                        "\tSystem.out.println(\"===*******=======\"+drools.getRule());\n" +
                        "\tSystem.out.println(map);\n" +
                        " \tupdate(map);\n" +
                "        \n" +
                "end";
        rule4Map.put("drlContext",rule4);
        //rule2.getBytes(Charset.forName("UTF-8"))
        rule4Map.put("ruleName","biz_rule_4");

        ruleContextList.add(rule4Map);
        return ruleContextList;
    }



    public void createResource(String name, byte[] bytes, DeploymentEntity deploymentEntity) {
        ResourceEntity resource = new ResourceEntity();
        resource.setName(name);
        resource.setBytes(bytes);
        resource.setDeploymentId(deploymentEntity.getId());

        Context
                .getCommandContext()
                .getDbSqlSession()
                .insert(resource);
    }

    public DeploymentEntity getDeployment(String deploymentId) {
        DeploymentEntity deployment = Context
                .getCommandContext()
                .getDeploymentEntityManager()
                .findDeploymentById(deploymentId);
        return deployment;
    }

    public ResourceEntityManager getResourceEntityManager(){
        return  Context
                .getCommandContext()
                .getResourceEntityManager();
    }



    public KnowledgeBase buildComplexFlowProcess(List<Map<String, Object>> ruleContextList) {
        try{
            logger.info("===加载流程规则。ruleContextList={}", JSON.toJSONString(ruleContextList));
            //2.生成规则KnowledgeBuilder、KnowledgeBase
            //加载规则，构建知识库
            KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            //构建知识库配置信息
            KnowledgeBaseConfigurationAdapter config = (KnowledgeBaseConfigurationAdapter) KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
            config.setOption(MaxThreadsOption.get(32));//设置执行线程数目

            KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(config);

            //3. 根据已启用的版本号id查询规则内容
//            List<Map<String, String>> ruleContextList = buildFlowProcessDao.getRuleContextListByVersionId(versionId);
            //获取规则内容


            CompositeKnowledgeBuilder ckb = knowledgeBuilder.batch().type(ResourceType.DRL);
            if (!org.springframework.util.CollectionUtils.isEmpty(ruleContextList)) {
                for (Map<String, Object> ruleContextMap : ruleContextList) {
                    String drlContext = String.valueOf(ruleContextMap.get("drlContext"));
                    System.out.println(drlContext);
                    ckb.add(ResourceFactory.newByteArrayResource(drlContext.getBytes("utf-8")));
                }
            }
            //批量构建、编译
            ckb.build();
            //检查规则编译是否有误
            boolean hasErrors = knowledgeBuilder.hasErrors();
            logger.info("*****,检查规则文件编译是否有错:hasErrors={}", hasErrors);
            if (hasErrors) {
                StringBuilder errorMsgBuilder = new StringBuilder();
                errorMsgBuilder.append(knowledgeBuilder.getResults());
                logger.info("*****Invalid knowledge base!!系统规则加载错误 ，原因 reason={}", knowledgeBuilder.getResults());
                //如果有错，批量移除
                KnowledgeBuilderErrors errors = knowledgeBuilder.getErrors();
                for (KnowledgeBuilderError error : errors) {
                    logger.error("***************error.message={}", error.getMessage());
                    errorMsgBuilder.append(error.getMessage());
                }
                //先打印错误信息，再进行移除；否则，先移除的话，错误信息也会一并消失
                knowledgeBuilder.undo();
                //系统生成规则，加载规则引擎有误，拒绝开启版本状态
                logger.error("。失败原因：" + errorMsgBuilder.toString());
            }

            knowledgeBase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());
//            KnowledgeBaseImpl intKbase = (KnowledgeBaseImpl) knowledgeBase;
            //[权宜之计]方便构建流程中使用，动态加载规则
//            KnowledgeStore.addKnowledgeBase(versionId, intKbase);

            //有流程，需引入JBPM
            //构建规则流程
//            RuleFlowProcess ruleFlowProcess = this.processDefinition(versionId);
//            RuleFlowProcess ruleFlowProcess = null;
//            if (ruleFlowProcess != null) {
//                logger.debug("将规则流程[ruleFlowProcess]添加到[intKbase]");
            //model 1: split不起效果[权宜之计：将split条件以规则形式加载到知识仓库中]
//                intKbase.addProcess(ruleFlowProcess);
            //model 2:ActionNode 判断逻辑需要写在consequence 中，否则NullPointerException
//                XmlBPMNProcessDumper dumper = XmlBPMNProcessDumper.INSTANCE;
//                String xml = dumper.dump(ruleFlowProcess);
//                knowledgeBuilder.add(ResourceFactory.newByteArrayResource(xml.getBytes()), ResourceType.BPMN2);
//            }

            //系统生成规则，加载规则引擎成功，开启版本状态
            logger.info("knowledgeBase....... success");

            return knowledgeBase;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("规则系统生成有误，加载失败，版本发布失败。",e);
        }
        logger.info("knowledgeBase....... failed");
        return null;
    }

}
