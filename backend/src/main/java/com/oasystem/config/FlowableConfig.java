package com.oasystem.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable 工作流引擎配置
 */
@Configuration
public class FlowableConfig {

    /**
     * 配置流程引擎
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> engineConfigurationConfigurer() {
        return engineConfiguration -> {
            // 设置数据库表更新策略
            engineConfiguration.setDatabaseSchemaUpdate("true");
            // 设置异步执行器
            engineConfiguration.setAsyncExecutorActivate(true);
            // 设置字符集
            engineConfiguration.setDatabaseTablePrefix("");
            // 设置流程图字体
            engineConfiguration.setActivityFontName("宋体");
            engineConfiguration.setLabelFontName("宋体");
            engineConfiguration.setAnnotationFontName("宋体");
        };
    }
}
