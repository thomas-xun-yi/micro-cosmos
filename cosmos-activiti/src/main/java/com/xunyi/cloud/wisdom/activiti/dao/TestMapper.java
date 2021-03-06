package com.xunyi.cloud.wisdom.activiti.dao;

import com.xunyi.cloud.wisdom.activiti.model.Test;

import java.util.List;

public interface TestMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Test record);

    int insertSelective(Test record);

    Test selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Test record);

    int updateByPrimaryKey(Test record);

    List<Test> selectAll();
}