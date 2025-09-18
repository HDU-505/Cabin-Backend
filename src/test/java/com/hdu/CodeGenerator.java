package com.hdu;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;

/**
 * @author
 * @since 2018/12/13
 */
public class CodeGenerator {

    @Test
    void contextLoads() {

        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/neurostudent", "root", "123321")
                .globalConfig(builder -> {
                    builder.author("DZL") // 设置作者
                            // 开启 swagger 模式 默认不开启
                            //.enableSwagger()
                            .outputDir("E:\\doing\\neuroStudent\\NeuroStudent-WorkStation-Admin\\src\\main\\java"); // 指定输出目录
                })
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT) {
                        // 自定义类型转换
                        return DbColumnType.INTEGER;
                    }
                    return typeRegistry.getColumnType(metaInfo);

                }))
                .packageConfig(builder -> {
                    // 设置父包名
                    builder.parent("com.hdu")
                            // 设置父包模块名
                            //.moduleName("mapper")
                            // 设置mapperXml生成路径
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "E:\\doing\\neuroStudent\\NeuroStudent-WorkStation-Admin\\src\\main\\resources\\mapper"));
                })
                .strategyConfig(builder -> {
                    // 设置需要生成的表名
                    builder.addInclude("tbl_eeg")
                    // 设置过滤表前缀
                    .addTablePrefix("tbl_", "c_");
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

    }
}
