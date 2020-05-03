/*
* Copyright (C) 2016-2018 ActionTech.
* based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
* License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
*/
package com.stu.spring.model;

import java.io.Serializable;

/**
 * @author mycat
 */
public class TableRuleConfig implements Serializable {
    //maybe become a list in feature
    private final RuleConfig rule;

    private String name;

    public TableRuleConfig(String name, RuleConfig rule) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (rule == null) {
            throw new IllegalArgumentException("no rule is found");
        }
        this.rule = rule;
        this.name = name;
    }

    /**
     * @return unmodifiable
     */
    public RuleConfig getRule() {
        return rule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
