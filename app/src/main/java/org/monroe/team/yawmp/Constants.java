package org.monroe.team.yawmp;

public class Constants {

    public static final String WATCHAPP_UUID = "a6ac390b-3a42-4b50-9e63-c3cf33b34958";

    public static final int
                KEY_VERSION = 1,
                KEY_EVENT_TYPE=3,
                KEY_AGENT_STATUS = 12,
                KEY_AGENT_ACTIVE = 13;

    public static final int EVENT_SYSTEM_TYPE_VERSION = 0,
                            EVENT_TYPE_AGENT_STATUS_GET = 10,
                            EVENT_TYPE_AGENT_STATUS_UPDATE =11,
                            EVENT_TYPE_AGENT_STATUS_CONTROL =12,
                            EVENT_TYPE_AGENT_ALARM =13;

    public static final int VALUE_AGENT_ACTIVATE_ON = 1,
                            VALUE_AGENT_ACTIVATE_OFF = 2;

}
