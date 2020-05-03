package com.stu.spring.namespace.handler;

import com.stu.spring.namespace.parser.ShardingTableRuleBeanDefinitonParser;
import com.stu.spring.namespace.constants.ShardingRuleBeanDefinitionParserTag;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class ShardingNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        registerBeanDefinitionParser(ShardingRuleBeanDefinitionParserTag.RULE_CONFIG_TAG, new ShardingTableRuleBeanDefinitonParser());
    }
}
