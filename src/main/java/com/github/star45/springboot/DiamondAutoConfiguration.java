package com.github.star45.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DiamondProperties.class)
public class DiamondAutoConfiguration {

    /**
     * 因为使用了  DiamondConfigurer继承了PropertyPlaceholderConfigurer
     * 且使用@Bean将其放入spring容器中
     * 导致注入的DiamondProperties 没有值，
     * 具体原因还没查清楚：大致是因为自己定义的PropertyPlaceholderConfigurer
     * 覆盖了系统原生态的PropertyPlaceholderConfigurer
     */
    @Autowired
    private DiamondProperties diamondProperties;

    @Bean
    public DiamondConfigurer initDiamond(){

        DiamondConfigurer diamondConfigurer = new DiamondConfigurer();
//        diamondConfigurer.setGroup("item_diamond_group1");
//        diamondConfigurer.setDataId("item_dianmond_dataid1");
        diamondConfigurer.setOrder(2);
        diamondConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return diamondConfigurer;
    }

}
