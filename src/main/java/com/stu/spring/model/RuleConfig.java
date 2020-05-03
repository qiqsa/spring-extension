/*
 * Copyright (C) 2016-2018 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.stu.spring.model;


import java.io.Serializable;

/**
 * RuleConfig
 *
 * @author mycat
 */
public class RuleConfig implements Serializable {
    private  String column;
    private  String functionName;

    /**
     * @return unmodifiable, upper-case
     */
    public String getColumn() {
        return column;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
