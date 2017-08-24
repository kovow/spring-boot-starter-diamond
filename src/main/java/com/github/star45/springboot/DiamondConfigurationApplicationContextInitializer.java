package com.github.star45.springboot;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class DiamondConfigurationApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String group = env.getProperty("spring.diamond.group");
        String dataId = env.getProperty("spring.diamond.dataId");
        if ((group != null) &&(dataId != null)) {
            System.err.println(" is success , group : " + group+",dataId : "+dataId);
        }

    }


}
