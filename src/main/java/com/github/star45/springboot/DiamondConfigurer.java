package com.github.star45.springboot;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Created by xulikang on 2017/6/8.
 */

public class DiamondConfigurer extends PropertyPlaceholderConfigurer implements
        ApplicationContextAware, ManagerListener {

    private static Logger logger = LoggerFactory.getLogger(DiamondConfigurer.class);

    private String group;
    private String dataId;

    private long timeout = 5000L;
    private DiamondManager diamondManager;
    private final Properties properties = new Properties();
    private ApplicationContext applicationContext;
    private final Set<String> propertyAwaresBeanNames = new HashSet<>();

    /**
     *  1、实例化时候执行
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.fetchPropertiesFromDiamondServer();



        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        String[] beanNameArray = registry.getBeanDefinitionNames();

        for (int i = 0; i < beanNameArray.length; i++) {
            String beanName = beanNameArray[i];
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);

            String beanClassName = beanDef.getBeanClassName();
            Class<?> beanClass = null;
            try {
                beanClass = cl.loadClass(beanClassName);
            } catch (Exception ex) {
                continue;
            }


            if (DiamondPropertyAware.class.isAssignableFrom(beanClass)) {
                this.propertyAwaresBeanNames.add(beanName);
                /**
                 * mpv.addPropertyValue(key,value)
                 * 使 DiamondPropertyAware接口的setDiamondProperties方法生效
                 * 方法：必须实现的是key的Get或者Set方法
                 * value:需要注入的属性值
                 * 不同的 *PropertyAware接口 会注入不同的value值。获取的都是自己当前的value
                 */
                MutablePropertyValues mpv = beanDef.getPropertyValues();
                mpv.addPropertyValue(DiamondPropertyAware.FIELD_DIAMOND_PROPERTIES, this.properties);
            }
        }

        super.postProcessBeanFactory(beanFactory);

    }

    /** 2、
     * 从diamond服务器中获取配置信息.
     */
    protected void fetchPropertiesFromDiamondServer() throws IllegalStateException {

        Environment environment = applicationContext.getEnvironment();
        this.setGroup(environment.getProperty("spring.diamond.group"));
        this.setDataId(environment.getProperty("spring.diamond.dataId"));
        this.diamondManager = new DefaultDiamondManager(group, dataId, this);
        String availableConfInfo = this.diamondManager.getAvailableConfigureInfomation(this.timeout);

        logger.info("diamond配置信息初始化.");
        this.processRecvConfigInfo(availableConfInfo);
    }

    /**
     *  3、将diamond属性放入到properties里
     * @param availableConfInfo
     */
    private void processRecvConfigInfo(String availableConfInfo) {
        InputStream input = null;
        try {
            Properties variables = new Properties();
            input = new ByteArrayInputStream(availableConfInfo.getBytes("UTF-8"));
            variables.load(input);
            this.properties.clear();
            this.properties.putAll(variables);
            this.setProperties(this.properties);
        } catch (UnsupportedEncodingException ex) {
            logger.error("获取diamond配置信息出错", ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            logger.error("获取diamond配置信息出错", ex);
            throw new RuntimeException(ex);
        } catch (RuntimeException ex) {
            logger.error("获取diamond配置信息出错", ex);
            throw ex;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    /**
     *  4、监听diamond变动
     * @param configInfo
     */
    @Override
    public void receiveConfigInfo(String configInfo) {
        logger.info("diamond配置信息发生变更.");
        try {
            this.processRecvConfigInfo(configInfo);
            this.notifyPropertyModified(this.properties);
        } catch (RuntimeException ex) {
            logger.error("接收配置处理失败!", ex);
        }
    }

    /**
     *  5、
     * @param properties
     */
    private synchronized void notifyPropertyModified(Properties properties) {
        Iterator<String> itr = this.propertyAwaresBeanNames.iterator();
        while (itr.hasNext()) {
            String beanName = itr.next();
            Object bean = this.applicationContext.getBean(beanName);
            if (DiamondPropertyAware.class.isAssignableFrom(bean.getClass())) {
                DiamondPropertyAware aware = (DiamondPropertyAware) bean;
                try {
                    aware.setDiamondProperties(properties);
                } catch (RuntimeException rex) {
                    logger.error(String.format("更新属性失败(beanName= %s)!", beanName));
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
