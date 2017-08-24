package com.github.star45.springboot;

import java.util.Properties;

/**
 * Created by xulikang on 2017/6/8.
 */
public interface DiamondPropertyAware {

    String FIELD_DIAMOND_PROPERTIES = "diamondProperties";

    void setDiamondProperties(Properties properties);
}
