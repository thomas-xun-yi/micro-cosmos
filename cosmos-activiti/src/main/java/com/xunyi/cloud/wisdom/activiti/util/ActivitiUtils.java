package com.xunyi.cloud.wisdom.activiti.util;

import org.activiti.bpmn.model.*;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:thomas
 * @Date: 2018/7/5 13:53
 * @Description: fluent 构建实现
 */
public class ActivitiUtils {

    public static ServiceTask createServiceTask(String id,String name){
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId(id);
        serviceTask.setName(name);

        return serviceTask;
    }



    //监听器，动态添加
    public static  UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);

        //监听器的动态注入
        List<String> taskListenerList = new ArrayList<>();
        taskListenerList.add("com.xunyi.cloud.wisdom.activiti.service.activitidrools.listeners.TaskLisnter");

        List<ActivitiListener> list = new ArrayList<>();
        for (String taskListener : taskListenerList) {
            ActivitiListener listener = new ActivitiListener();
            listener.setEvent(TaskListener.EVENTNAME_ALL_EVENTS);
            //1. Spring配置以变量形式调用写入
            //2. 通过继承TaskListener方法（其它方法可参考 ImplementationType）
            listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
            listener.setImplementation(taskListener);
            list.add(listener);
        }

        userTask.setTaskListeners(list);


        return userTask;
    }

    public static SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    public static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    public static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");

        return endEvent;
    }

    public  static BusinessRuleTask businessRuleTask(String busId,String name,List<String> ruleNames){
        BusinessRuleTask businessRuleTask = new BusinessRuleTask();
        businessRuleTask.setId(busId);
        businessRuleTask.setName(name);
        if(CollectionUtils.isNotEmpty(ruleNames)){
            businessRuleTask.setRuleNames(ruleNames);
        }
        List<String> inputVariables = new ArrayList<>();
        //test.drl 如果 rule1 需要执行，必须插入Map实例
        //因为 rule1 的when条件使用的map
        //BusinessRuleTaskActivityBehavior
//        inputVariables.add("${yysContactLoanOrg}");
//        inputVariables.add("yysContactLoanOrg");
        inputVariables.add("${map}");
//        inputVariables.add("map");
        businessRuleTask.setInputVariables(inputVariables);

//        输出对象存储到一个流程变量中。注意，结果变量将包含一个对象列表。如果没有指定结果的变量名，默认使用
//        org.activiti.engine.rules.OUPUT。
//        rulesOutput
        businessRuleTask.setResultVariableName("rulesOutput");
        //监听器的动态注入(待测试，似乎没效果)
/*        List<String> taskListenerList = new ArrayList<>();
        taskListenerList.add("com.xunyi.cloud.wisdom.activiti.service.activitidrools.listeners.TaskLisnter");

        List<ActivitiListener> list = new ArrayList<>();
        for (String taskListener : taskListenerList) {
            ActivitiListener listener = new ActivitiListener();
            listener.setEvent(TaskListener.EVENTNAME_ALL_EVENTS);
            //1. Spring配置以变量形式调用写入
            //2. 通过继承TaskListener方法（其它方法可参考 ImplementationType）
            listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
            listener.setImplementation(taskListener);
            list.add(listener);
        }

        businessRuleTask.setExecutionListeners(list);*/
        return businessRuleTask;
    }


}
