package com.stu.spring;

import com.stu.spring.model.RuleConfig;
import com.stu.spring.model.TableRuleConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Qi.qingshan
 * @date 2020/5/3
 */
public class ShardingRuleTestMain {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        TableRuleConfig bean = context.getBean(TableRuleConfig.class);
        System.out.println(bean.getRule().getColumn());
    }
}
