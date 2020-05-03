package com.stu.spring.namespace.parser;

import com.stu.spring.model.RuleConfig;
import com.stu.spring.model.TableRuleConfig;
import com.stu.spring.namespace.constants.ShardingRuleBeanDefinitionParserTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class ShardingTableRuleBeanDefinitonParser extends AbstractBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TableRuleConfig.class);
        factory.addConstructorArgValue(element.getAttribute("name"));
        factory.addConstructorArgValue(parseRuleConfiguration(element));
        return factory.getBeanDefinition();
    }

    private AbstractBeanDefinition parseRuleConfiguration(Element element){
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(RuleConfig.class);
        factory.addPropertyValue("column",element.getAttribute("column"));
        factory.addPropertyValue("functionName",element.getAttribute("functionName"));
        return factory.getBeanDefinition();
    }
}
