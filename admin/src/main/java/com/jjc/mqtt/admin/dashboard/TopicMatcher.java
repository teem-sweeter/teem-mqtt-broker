package com.jjc.mqtt.admin.dashboard;

public final class TopicMatcher {

    private TopicMatcher() {}

    public static boolean matches(String topicFilter, String topicName) {
        if (topicFilter == null || topicName == null) {
            return false;
        }
        String[] filterLevels = topicFilter.split("/", -1);
        String[] topicLevels = topicName.split("/", -1);

        int fi = 0;
        int ti = 0;
        while (fi < filterLevels.length && ti < topicLevels.length) {
            String f = filterLevels[fi];
            if ("#".equals(f)) {
                return true;
            } else if ("+".equals(f)) {
                fi++;
                ti++;
            } else if (f.equals(topicLevels[ti])) {
                fi++;
                ti++;
            } else {
                return false;
            }
        }
        return fi == filterLevels.length && ti == topicLevels.length;
    }
}
