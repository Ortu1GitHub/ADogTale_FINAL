package com.pmdm.adogtale.push_notification;

import com.pmdm.adogtale.model.User;

public class PushNotificationData {

    private final String title;
    private final String body;
    private final User originUser;
    private final User targetUser;

    public PushNotificationData(String title, String body, User originUser, User targetUser) {
        this.title = title;
        this.body = body;
        this.originUser = originUser;
        this.targetUser = targetUser;
    }

    public String getTitle() {
        return title;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public User getOriginUser() {
        return originUser;
    }

    public String getBody() {
        return body;
    }
}
