package com.yichen.cosmos.cloud.platform.bean.decision;

import java.io.Serializable;
import java.util.Date;

/**
 * 规则条件 when
 * Created by thomas on 2017/3/7.
 */
public class WisdomRuleCondition implements Serializable {

    private static final long serialVersionUID = -7215740172004816442L;
    /**
     * 规则具体条件、决策id
     */
    private String conditionId;
    /**
     * 规则id
     */
    private String wisdomRuleId;
    /**
     * 规则、表单关联id
     */
    private String ruleRelationId;
    /**
     * 表单字段名称
     */
    private String field;
    private String fieldId;
    private String formId;

    //决策表中使用，表单名称
    private String formName;
    /**
     * 运算符：=，>,<等
     */
    private String operator;
    /**
     * 比较值
     */
    private String judgeValue;
    /**
     * 连接符：and，or，等
     */
    private String connector;

    private Integer flag;
    private Date createTime;
    private Date updateTime;

    private String creatorId;
    private String updaterId;
    private String orgId;
    //同一条规则下，条件排列序号，值越小越靠前.默认值为0
    private Integer cindex;

    /**
     * 公式
     **/
    private String formula;

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public Integer getCindex() {
        return cindex;
    }

    public void setCindex(Integer cindex) {
        this.cindex = cindex;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getUpdaterId() {
        return updaterId;
    }

    public void setUpdaterId(String updaterId) {
        this.updaterId = updaterId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getWisdomRuleId() {
        return wisdomRuleId;
    }

    public void setWisdomRuleId(String wisdomRuleId) {
        this.wisdomRuleId = wisdomRuleId;
    }

    public String getRuleRelationId() {
        return ruleRelationId;
    }

    public void setRuleRelationId(String ruleRelationId) {
        this.ruleRelationId = ruleRelationId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getJudgeValue() {
        return judgeValue;
    }

    public void setJudgeValue(String judgeValue) {
        this.judgeValue = judgeValue;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
