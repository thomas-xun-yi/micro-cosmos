package com.xunyi.cloud.wisdom.activiti.service;

import com.xunyi.cloud.wisdom.activiti.model.Test;

import java.util.List;

/**
 * Created by thomas.su on 2018/2/27 17:22.
 */
public interface ITestService {

    public Test getTestById(Integer id);

    public void testName();

    public List<Test> selectAll();
}
