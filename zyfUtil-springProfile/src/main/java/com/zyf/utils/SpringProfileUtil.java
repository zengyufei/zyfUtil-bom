package com.zyf.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@Slf4j
public class SpringProfileUtil {
    private static final String key = "spring.profiles.active";

    public static String tryCopyConfsFromMapping() throws FileNotFoundException {
        return copyConfsFromMapping("local");
    }

    public static String tryCopyConfsFromMapping(String defaultProfile) throws FileNotFoundException {
        return copyConfsFromMapping(defaultProfile);
    }

    public static String tryCopyConfsFromNotMapping() throws FileNotFoundException {
        return copyConfsFromNotMapping("local");
    }

    public static String tryCopyConfsFromNotMapping(String defaultProfile) throws FileNotFoundException {
        return copyConfsFromNotMapping(defaultProfile);
    }

    private static String copyConfsFromMapping(String defaultProfile) throws FileNotFoundException {
        String activeProfile = SystemUtil.get(key);
        boolean isJar = ClassUtil.getClassPathURL().getProtocol().equalsIgnoreCase("jar");
        if (isJar) {
            return activeProfile;
        }
        activeProfile = getPomXmlDefaultActiveProfile(activeProfile, defaultProfile);
        activeProfile = StrUtil.blankToDefault(activeProfile, defaultProfile);
        judgeProfiles(activeProfile);

        log.info("拷贝 {} 环境所需文件...", activeProfile);
        String confsPath = getConfsPath();
        String confsProfilePath = getConfsProfileFiles(activeProfile, confsPath);
        String iniPath = getConfsMappingIni(confsPath);
        if (FileUtil.exist(iniPath)) {
            copyFromMappingIniFileToPath(activeProfile, confsProfilePath, iniPath);
        } else {
            log.debug("未找到 mapping.ini，将采用直接覆盖的方式！");
            copyConfsProfileFIlesToClassPath(confsPath, confsProfilePath);
        }
        return activeProfile;
    }

    private static String copyConfsFromNotMapping(String defaultProfile) throws FileNotFoundException {
        String activeProfile = SystemUtil.get(key);
        boolean isJar = ClassUtil.getClassPathURL().getProtocol().equalsIgnoreCase("jar");
        if (isJar) {
            return activeProfile;
        }
        activeProfile = getPomXmlDefaultActiveProfile(activeProfile, defaultProfile);
        activeProfile = StrUtil.blankToDefault(activeProfile, defaultProfile);
        judgeProfiles(activeProfile);

        log.info("直接覆盖的方式拷贝 {} 环境所需文件...", activeProfile);
        String confsPath = getConfsPath();
        String confsProfilePath = getConfsProfileFiles(activeProfile, confsPath);

        copyConfsProfileFIlesToClassPath(confsPath, confsProfilePath);
        return activeProfile;
    }

    private static void judgeProfiles(String activeProfile) {
        /*
         * 直接替换 confs 文件
         */
        if (!StrUtil.containsAnyIgnoreCase(activeProfile, "local", "dev", "test", "pro", "prod")) {
            log.warn("profile not in \"local\", \"dev\", \"test\", \"pro\", \"prod\" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private static String getPomXmlDefaultActiveProfile(String activeProfile, String defaultProfile) {
        if (StrUtil.isBlank(activeProfile)) {
            log.debug("未从属性系统内找到 'spring.profiles.active'，查找 pom.xml.");
            String pomXmlFilePath = getConfsProfileFiles("pom.xml", FileUtil.getWebRoot().getAbsolutePath());
            if (!FileUtil.exist(pomXmlFilePath)) {
                log.warn("非 maven 项目，文件 {} 不存在！", activeProfile);
            } else {
                Document pomXmlDocument = XmlUtil.readXML(pomXmlFilePath);
                NodeList nodeList = XmlUtil.getNodeListByXPath("//profiles/profile", pomXmlDocument);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node item = nodeList.item(i);
                    Node nodeByXPath = XmlUtil.getNodeByXPath("./activation/activeByDefault", item);
                    if (nodeByXPath != null && StrUtil.equalsIgnoreCase(nodeByXPath.getTextContent(), "true")) {
                        Node idNode = XmlUtil.getNodeByXPath("./id", item);
                        activeProfile = StrUtil.trim(idNode.getTextContent());
                        break;
                    }
                }
                if (StrUtil.isBlank(activeProfile)) {
                    log.warn("从 pom.xml 未找到 profile，使用默认 {}.", defaultProfile);
                }
            }
        } else {
            log.debug("从系统内找到属性 'spring.profiles.active' = {}", activeProfile);
        }
        return activeProfile;
    }


    private static void copyFromMappingIniFileToPath(String activeProfile, String confsProfilePath, String iniPath) throws FileNotFoundException {
        log.debug("读取 mapping.ini 配置，按照配置覆盖！");
        Map<String, String> items = IniReader.getItemsBySectionName(activeProfile, new File(iniPath));
        if (MapUtil.isNotEmpty(items)) {
            String[] pathEnum = new String[]{"src/main/java/", "src/main/resources/", "src/main/webapp/"};
            for (Map.Entry<String, String> stringStringEntry : items.entrySet()) {
                String key = StrUtil.trim(stringStringEntry.getKey());
                String value = StrUtil.trim(stringStringEntry.getValue());
                for (String s : pathEnum) {
                    if (StrUtil.containsIgnoreCase(value, s)) {
                        value = StrUtil.replaceIgnoreCase(value, s, "");
                        break;
                    }
                }
                String copy = getConfsProfileFiles(key, confsProfilePath);
                String classPath = getClassPath();
                String paste = getConfsProfileFiles(value, classPath);
                if (!FileUtil.exist(copy)) {
                    log.error("没有 {} 文件(文件夹)，拷贝停止.", copy);
                    throw new FileNotFoundException(StrUtil.format("没有 {} 文件(文件夹)，拷贝停止.", copy));
                }
                FileUtil.copy(copy, paste, true);
                log.debug("从 {} 读取文件(文件夹)覆盖到 {}", copy, paste);
            }
        } else {
            log.warn("没有 {} 文件夹环境配置，拷贝停止.", activeProfile);
            throw new FileNotFoundException(StrUtil.format("没有 {} 文件夹环境配置，拷贝停止.", activeProfile));
        }
    }

    private static void copyConfsProfileFIlesToClassPath(String confsPath, String confsProfilePath) throws FileNotFoundException {
        if (FileUtil.exist(confsPath)) {
            if (!FileUtil.exist(confsProfilePath)) {
                log.error("没有 {} 文件(文件夹)，拷贝停止.", confsProfilePath);
                throw new FileNotFoundException(StrUtil.format("没有 {} 文件(文件夹)，拷贝停止.", confsProfilePath));
            }
            String classPath = getClassPath();
            FileUtil.copyContent(new File(confsProfilePath), new File(classPath), true);
            log.debug("从 {} 读取文件(文件夹)覆盖到 {}", confsProfilePath, classPath);
        } else {
            log.warn("没有 confs 文件夹，拷贝停止.");
        }
    }

    private static String getConfsProfileFiles(String activeProfile, String confsPath) {
        return StrUtil.join(File.separator, confsPath, activeProfile);
    }

    private static String getConfsMappingIni(String confsPath) {
        return getConfsProfileFiles("mapping.ini", confsPath);
    }

    private static String getConfsPath() {
        return StrUtil.join(File.separator, FileUtil.getWebRoot().getAbsoluteFile(), "confs");
    }

    private static String getClassPath() {
        final String classPath = StrUtil.removeSuffix(ClassUtil.getClassPath(), "/");
        return StrUtil.trim(classPath);
    }



    private static class IniReader {
        private static Map<String, Map<String, String>> sectionsMap = MapUtil.newHashMap();
        private static Map<String, String> itemsMap = MapUtil.newHashMap();

        private static void loadData(File file) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                String currentSection = "";
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if ("".equals(line)) {
                        continue;
                    }
                    if (line.startsWith("[") && line.endsWith("]")) {
                        // Ends last section
                        /*
                        if (itemsMap.size() > 0 && !"".equals(currentSection.trim())) {
                            sectionsMap.put(currentSection, itemsMap);
                        }
                        */
                        itemsMap = null;

                        // Start new section initial
                        currentSection = line.substring(1, line.length() - 1);
                        itemsMap = MapUtil.newHashMap();
                        sectionsMap.put(currentSection, itemsMap);
                    } else {
                        int index = line.indexOf("=");
                        if (index != -1) {
                            String key = line.substring(0, index);
                            String value = line.substring(index + 1);
                            itemsMap.put(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static Map<String, String> getItemsBySectionName(String section, File file) {
            loadData(file);
            return sectionsMap.get(section);
        }

    }
}