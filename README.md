## 扩展spring标签

某些场景下我们需要扩展spring标签，让spring可以识别我们自定义的标签，实现自定义bean；比如dubbo中定义`dubbo:service` 等，shardingsphere在适配spring，在spring配置文件中定义分库分表策略等，今天主要分析如何扩展spring自定义标签以及实现demo。为后边自研分库分表中间件实现spring配置做铺垫。

首先需要简单了解以下信息  

### xsd 与DTD

- XML Schema 是基于 XML 的 DTD 替代者。


- XML Schema 描述 XML 文档的结构。


- XML Schema 语言也称作 XML Schema 定义（XML Schema Definition，XSD）。


- XML Schema 比 DTD 更强大。



#### XSD - <schema> 元素

<schema> 元素是每一个 XML Schema 的根元素：

```xml
<?xml version="1.0"?>

<xs:schema>

...
...

</xs:schema>
```

<schema> 元素可包含属性。一个 schema 声明往往看上去类似这样：

```xml
<?xml version="1.0"?>
 
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
targetNamespace="http://www.w3school.com.cn"
xmlns="http://www.w3school.com.cn"
elementFormDefault="qualified">

...
...
</xs:schema>
```

代码解释：

下面的片断：

```xml
xmlns:xs="http://www.w3.org/2001/XMLSchema"
```

显示 schema 中用到的元素和数据类型来自命名空间 "http://www.w3.org/2001/XMLSchema"。同时它还规定了来自命名空间 "http://www.w3.org/2001/XMLSchema" 的元素和数据类型应该使用前缀 xs：

这个片断：

```xml
targetNamespace="http://www.w3school.com.cn" 
```

显示被此 schema 定义的元素 (note, to, from, heading, body) 来自命名空间： "http://www.w3school.com.cn"。

这个片断：

```xml
xmlns="http://www.w3school.com.cn" 
```

指出默认的命名空间是 "http://www.w3school.com.cn"。

这个片断：

```xml
elementFormDefault="qualified" 
```

指出任何 XML 实例文档所使用的且在此 schema 中声明过的元素必须被命名空间限定。

### spring IOC容器

控制反转或者依赖倒置，对象之间有引用或者依赖关系，由spring 容器来完成，IOC容器实现了对bean管理，所以就涉及到了spring是如何加载bean？以及将bean注册到容器中？

spring对bean的定义接口

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement{}
```

每一个bean会构建一个BeanDefinition，构建完成后注册到容器中，实际就是spring维护的HashMap中，主要介绍如何加载BeanDefinition

#### BeanDefinition载入和解析

FileSystemXmlApplicationContext初始化入口

```java
public FileSystemXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {

		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}
```

refresh方法启动容器，包含加载bean，即loadBeanDefinitions

```java
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			beanFactory.setSerializationId(getId());
			customizeBeanFactory(beanFactory);
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

```

没有在loadBeanDefinitions方法直接解析xml，构建beanDefinition，而是通过对应的reader获取，起到解耦作用

```
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

```

获取所有资源加载，比如项目中可能存在多个xml文件

```java
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}

```

加载bean 并返回bean总数

```java
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (String location : locations) {
			counter += loadBeanDefinitions(location);
		}
		return counter;
	}
```



```java
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		int countBefore = getRegistry().getBeanDefinitionCount();
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
```

BeanDefinitionParserDelegate中完成对beanDefinition解析

```java
	public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
		String namespaceUri = getNamespaceURI(ele);
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
			return null;
		}
		return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
	}
```

上面代码中可以发现解析xml是对应handler处理即接口NamespaceHandler，这个handler可以扩展，后边我们就是基于这个扩展spring标签，并实现bean注册，同时通过PropertyValues可以对BeanDefinition设置属性。

```java
public interface PropertyValues {
    PropertyValue[] getPropertyValues();

    PropertyValue getPropertyValue(String var1);

    PropertyValues changesSince(PropertyValues var1);

    boolean contains(String var1);

    boolean isEmpty();
}
```

看懂以上原理，下面实现spring扩展标签比较容易

## 扩展spring标签实现

### 实现自定义标签

spring注册bean的形式如下

```xml
 <bean class="com.stu.code.aspect.AcctService"></bean>
```

现实现一个与bean作用类似的注解自动将属性注入到tableConfig中，这里只是一个介绍原理，并带有一个简单的demo

```xml
<table name="acct" type="sharding" />
```

1、定义TableConfig

```java
/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class TableConfig {

    private String name;

    private String type;

   //省略get set方法
}

```

```java
/**
 * @author Qi.qingshan
 * @date 2020/5/3
 */
public class ShardingConfiguration {

    private TableConfig tableConfig;

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }
}

```

2、编写XML schema文件，即 .xsd文件，是对xml文件的描述,文件位置可自定义

```xml
<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns="http://code.stu.com/schema/sharding"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:beans="http://www.springframework.org/schema/beans"
            targetNamespace="http://code.stu.com/schema/sharding"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsd:element name="table">
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="type" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>

</xsd:schema>
```

其中注意，后边注册需要用

```
xmlns="http://code.stu.com/schema/sharding
```

3、实现NamespaceHandler

继承NamespaceHandlerSupport即可，在handler中注册parser

```java
/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class TableNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
    	/** table为xsd中定义的element*/
        registerBeanDefinitionParser("table", new TableBeanDefinitonParser());
    }
}
```

4、实现BeanDefinitionParser

在parser中完成构建BeanDefinition，并注册到容器中

```java
/**
 * @author Qi.qingshan
 * @date 2020/5/2
 */
public class TableBeanDefinitonParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        AbstractBeanDefinition definition = SpringBeanExtension.getBeanDefinitionByElement(element);
        parserContext.getRegistry().registerBeanDefinition("shardingConfiguration", definition);
        return definition;
    }
}

```

```java
public final class SpringBeanExtension {

    public static AbstractBeanDefinition getBeanDefinitionByElement(Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingConfiguration.class);
       //ShardingConfiguration 设置tableConfig属性，这里是set方法名
        factory.addPropertyValue("tableConfig", parseTableDefinine(element));
        return factory.getBeanDefinition();
    }

    private static BeanDefinition parseTableDefinine(Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TableConfig.class);
       //设置属性
        factory.addPropertyValue("name", element.getAttribute("name"));
        factory.addPropertyValue("type", element.getAttribute("type"));
        return factory.getBeanDefinition();
    }
}

```

5、注册Handler和XML schema

在META-INF下新建spring.handlers和sping.schemas文件，内容如下

```xml
http\://code.stu.com/schema/sharding=com.stu.spring.handler.TableNamespaceHandler
```

```xml
http\://code.stu.com/schema/sharding/spring-table.xsd=META-INF/spring-table.xsd
```

6、在spring文件中引入xsd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:sharding="http://code.stu.com/schema/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://code.stu.com/schema/sharding
       http://code.stu.com/schema/sharding/spring-table.xsd">

    <sharding:table name="acct" type="global"></sharding:table>

</beans>
```

### 场景验证

以xml为例，通过ClassPathXmlApplicationContext加载spring配置

```java
  @Test
    public void testSringExtension(){
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        ShardingConfiguration shardingConfiguration = context.getBean(ShardingConfiguration.class);
        System.out.println(shardingConfiguration.getTableConfig().getName());

    }
```



本文章对扩展spring标签，实现自定义bean流程做了介绍，可按章节中spring IOC容器分析spring中的实现，或者学习dubbo 或者shardingsphere源码，个人建议看shardingsphere中sharding-spring模块，代码很具有代表性。