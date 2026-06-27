package com.jjc.mqtt.bridge.route;

import java.util.regex.Pattern;

/**
 * MQTT 主题通配符匹配器
 * <p>
 * 支持 +（单层匹配）和 #（多层匹配）通配符
 *
 * @author sweeter
 */
public class TopicMatcher {

    private TopicMatcher() {
    }

    /**
     * 判断 MQTT 主题是否匹配主题过滤器
     *
     * @param filter 主题过滤器（可含通配符 + #）
     * @param topic  实际主题
     * @return 是否匹配
     */
    public static boolean matches(String filter, String topic) {
        if (filter == null || topic == null) {
            return false;
        }
        if (filter.equals(topic)) {
            return true;
        }
        // 转换为正则表达式
        String regex = toRegex(filter);
        try {
            return Pattern.matches(regex, topic);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将 MQTT 主题过滤器转换为正则表达式
     */
    private static String toRegex(String filter) {
        StringBuilder sb = new StringBuilder("^");
        String[] levels = filter.split("/", -1);
        for (int i = 0; i < levels.length; i++) {
            if (i > 0) {
                sb.append("/");
            }
            if ("#".equals(levels[i])) {
                sb.append(".*");
                break;
            } else if ("+".equals(levels[i])) {
                sb.append("[^/]*");
            } else {
                sb.append(Pattern.quote(levels[i]));
            }
        }
        if (!filter.endsWith("#")) {
            sb.append("$");
        }
        return sb.toString();
    }

    /**
     * 从源主题中提取通配符匹配的部分，用于变量替换
     * 例如：filter="sensor/+/data", topic="sensor/temp/data" -> ["temp"]
     *
     * @param filter 主题过滤器
     * @param topic  实际主题
     * @return 匹配的各层值，如果不匹配返回 null
     */
    public static String[] extractWildcards(String filter, String topic) {
        if (filter == null || topic == null) {
            return null;
        }
        String[] filterLevels = filter.split("/", -1);
        String[] topicLevels = topic.split("/", -1);

        // # 通配符可以匹配零层或多层
        if (filter.endsWith("#")) {
            if (topicLevels.length < filterLevels.length - 1) {
                return null;
            }
        } else {
            if (filterLevels.length != topicLevels.length) {
                return null;
            }
        }

        java.util.List<String> wildcards = new java.util.ArrayList<>();
        for (int i = 0; i < filterLevels.length; i++) {
            if ("#".equals(filterLevels[i])) {
                // 将剩余所有层拼接
                StringBuilder rest = new StringBuilder();
                for (int j = i; j < topicLevels.length; j++) {
                    if (j > i) rest.append("/");
                    rest.append(topicLevels[j]);
                }
                wildcards.add(rest.toString());
                break;
            } else if ("+".equals(filterLevels[i])) {
                wildcards.add(topicLevels[i]);
            } else if (!filterLevels[i].equals(topicLevels[i])) {
                return null;
            }
        }
        return wildcards.toArray(new String[0]);
    }

    /**
     * 对目的主题进行变量替换
     * ${1} 替换为第一个通配符匹配值，${2} 替换为第二个，依此类推
     *
     * @param destTopic 目的主题模板，如 "remote/${1}/data"
     * @param wildcards 通配符匹配值
     * @return 替换后的主题
     */
    public static String replaceVariables(String destTopic, String[] wildcards) {
        if (destTopic == null || wildcards == null) {
            return destTopic;
        }
        String result = destTopic;
        for (int i = 0; i < wildcards.length; i++) {
            result = result.replace("${" + (i + 1) + "}", wildcards[i]);
        }
        return result;
    }
}
