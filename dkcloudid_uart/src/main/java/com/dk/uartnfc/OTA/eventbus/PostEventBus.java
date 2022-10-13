package com.dk.uartnfc.OTA.eventbus;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Gh0st on 2017/7/5.
 */

public class PostEventBus {

    private PostEventBus() {
    }

    public static void post(String str) {
        EventBus.getDefault().post(new MessageEvent(str));
    }
}
