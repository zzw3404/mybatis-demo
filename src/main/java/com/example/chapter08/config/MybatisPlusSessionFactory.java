package com.example.chapter08.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.MybatisXMLConfigBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.example.chapter08.mapper.StudentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;

/** Builds an isolated MyBatis-Plus SqlSessionFactory using the framework's own builder. */
public final class MybatisPlusSessionFactory {

    private static final String CONFIG_RESOURCE = "chapter08/mybatis-plus-config.xml";

    private MybatisPlusSessionFactory() {
    }

    public static SqlSessionFactory build() throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_RESOURCE)) {
            MybatisXMLConfigBuilder parser = new MybatisXMLConfigBuilder(inputStream);
            MybatisConfiguration configuration = (MybatisConfiguration) parser.parse();

            // Keep this order: parse -> interceptors -> GlobalConfig -> mapper -> build.
            configuration.addInterceptor(MybatisPlusConfig.buildInterceptor());

            GlobalConfig globalConfig = GlobalConfigUtils.defaults();
            globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
            GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

            // Register BaseMapper only after GlobalConfig to prevent the default global
            // configuration from being cached before the fill handler is attached.
            configuration.addMapper(StudentMapper.class);

            return new MybatisSqlSessionFactoryBuilder().build(configuration);
        }
    }
}
