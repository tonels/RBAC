package com.dxj.generator.util;

import cn.hutool.extra.template.*;
import cn.hutool.core.util.StrUtil;
import com.dxj.common.util.FileUtil;
import com.dxj.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import com.dxj.generator.domain.entity.GenConfig;
import com.dxj.generator.domain.vo.ColumnInfo;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成
 *
 * @author dxj
 * @date 2019-04-02
 */
@Slf4j
public class GenUtil {

    private static final String TIMESTAMP = "Timestamp";

    private static final String BIGDECIMAL = "BigDecimal";

    private static final String PK = "PRI";

    private static final String EXTRA = "auto_increment";

    /**
     * 获取后端代码模板名称
     *
     * @return
     */
    private static List<String> getAdminTemplateNames() {
        List<String> templateNames = new ArrayList<>();
        templateNames.add("Entity");
        templateNames.add("Dto");
        templateNames.add("Mapper");
        templateNames.add("Repository");
        templateNames.add("Service");
        templateNames.add("Query");
        templateNames.add("Controller");
        return templateNames;
    }

    /**
     * 获取前端代码模板名称
     *
     * @return
     */
    private static List<String> getFrontTemplateNames() {
        List<String> templateNames = new ArrayList<>();
        templateNames.add("api");
        templateNames.add("index");
        templateNames.add("eForm");
        return templateNames;
    }

    /**
     * 生成代码
     *
     * @param columnInfos 表元数据
     * @param genConfig   生成代码的参数配置，如包路径，作者
     */
    public static void generatorCode(List<ColumnInfo> columnInfos, GenConfig genConfig, String tableName) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("package", genConfig.getPack());
        map.put("moduleName", genConfig.getModuleName());
        map.put("author", genConfig.getAuthor());
        map.put("date", LocalDate.now().toString());
        map.put("tableName", tableName);
        String className = StringUtil.toCapitalizeCamelCase(tableName);
        String changeClassName = StringUtil.toCamelCase(tableName);

        // 判断是否去除表前缀
        if (StringUtil.isNotEmpty(genConfig.getPrefix())) {
            className = StringUtil.toCapitalizeCamelCase(StrUtil.removePrefix(tableName, genConfig.getPrefix()));
            changeClassName = StringUtil.toCamelCase(StrUtil.removePrefix(tableName, genConfig.getPrefix()));
        }
        map.put("className", className);
        map.put("upperCaseClassName", className.toUpperCase());
        map.put("changeClassName", changeClassName);
        map.put("hasTimestamp", false);
        map.put("hasBigDecimal", false);
        map.put("hasQuery", false);
        map.put("auto", false);

        List<Map<String, Object>> columns = new ArrayList<>();
        List<Map<String, Object>> queryColumns = new ArrayList<>();
        for (ColumnInfo column : columnInfos) {
            Map<String, Object> listMap = new HashMap<>();
            listMap.put("columnComment", column.getColumnComment());
            listMap.put("columnKey", column.getColumnKey());

            String colType = ColUtil.cloToJava(column.getColumnType().toString());
            String changeColumnName = StringUtil.toCamelCase(column.getColumnName().toString());
            String capitalColumnName = StringUtil.toCapitalizeCamelCase(column.getColumnName().toString());
            if (PK.equals(column.getColumnKey())) {
                map.put("pkColumnType", colType);
                map.put("pkChangeColName", changeColumnName);
                map.put("pkCapitalColName", capitalColumnName);
            }
            if (TIMESTAMP.equals(colType)) {
                map.put("hasTimestamp", true);
            }
            if (BIGDECIMAL.equals(colType)) {
                map.put("hasBigDecimal", true);
            }
            if (EXTRA.equals(column.getExtra())) {
                map.put("auto", true);
            }
            listMap.put("columnType", colType);
            listMap.put("columnName", column.getColumnName());
            listMap.put("isNullable", column.getIsNullable());
            listMap.put("columnShow", column.getColumnShow());
            listMap.put("changeColumnName", changeColumnName);
            listMap.put("capitalColumnName", capitalColumnName);

            if (!StringUtil.isBlank(column.getColumnQuery())) {
                listMap.put("columnQuery", column.getColumnQuery());
                map.put("hasQuery", true);
                queryColumns.add(listMap);
            }
            columns.add(listMap);
        }
        map.put("columns", columns);
        map.put("queryColumns", queryColumns);
        TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("template", TemplateConfig.ResourceMode.CLASSPATH));

        // 生成后端代码
        List<String> templates = getAdminTemplateNames();
        for (String templateName : templates) {
            Template template = engine.getTemplate("generator/admin/" + templateName + ".ftl");
            String filePath = getAdminFilePath(templateName, genConfig, className);

            assert filePath != null;
            File file = new File(filePath);
            getCodeTemplate(genConfig, map, template, file);

        }

        // 生成前端代码
        templates = getFrontTemplateNames();
        for (String templateName : templates) {
            Template template = engine.getTemplate("generator/front/" + templateName + ".ftl");
            String filePath = getFrontFilePath(templateName, genConfig, map.get("changeClassName").toString());

            assert filePath != null;
            File file = new File(filePath);

            // 如果非覆盖生成
            getCodeTemplate(genConfig, map, template, file);
        }
    }

    private static void getCodeTemplate(GenConfig genConfig, Map<String, Object> map, Template template, File file) throws IOException {
        // 如果非覆盖生成
        if (!genConfig.getCover()) {
            if (FileUtil.exist(file)) {
                return;
            }
        }
        // 生成代码
        genFile(file, template, map);
    }

    /**
     * 定义后端文件路径以及名称
     */
    private static String getAdminFilePath(String templateName, GenConfig genConfig, String className) {
        String ProjectPath = System.getProperty("user.dir") + File.separator + genConfig.getModuleName();
        String packagePath = ProjectPath + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
        if (!ObjectUtils.isEmpty(genConfig.getPack())) {
            packagePath += genConfig.getPack().replace(".", File.separator) + File.separator;
        }

        if ("Entity".equals(templateName)) {
            return packagePath + "entity" + File.separator + className + ".java";
        }

        if ("Controller".equals(templateName)) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if ("Service".equals(templateName)) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }


        if ("Dto".equals(templateName)) {
            return packagePath + "dto" + File.separator + className + "DTO.java";
        }

        if ("Query".equals(templateName)) {
            return packagePath + "dto" + File.separator + className + "Query.java";
        }

        if ("Mapper".equals(templateName)) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }

        if ("Repository".equals(templateName)) {
            return packagePath + "repository" + File.separator + className + "Repository.java";
        }

        return null;
    }

    /**
     * 定义前端文件路径以及名称
     */
    private static String getFrontFilePath(String templateName, GenConfig genConfig, String apiName) {
        String path = genConfig.getPath();

        if ("api".equals(templateName)) {
            return genConfig.getApiPath() + File.separator + apiName + ".js";
        }

        if ("index".equals(templateName)) {
            return path  + File.separator + "index.vue";
        }

        if ("eForm".equals(templateName)) {
            return path  + File.separator + File.separator + "form.vue";
        }
        return null;
    }

    private static void genFile(File file, Template template, Map<String, Object> map) throws IOException {
        // 生成目标文件
        Writer writer = null;
        try {
            FileUtil.touch(file);
            writer = new FileWriter(file);
            template.render(map, writer);
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            assert writer != null;
            writer.close();
        }
    }
}
