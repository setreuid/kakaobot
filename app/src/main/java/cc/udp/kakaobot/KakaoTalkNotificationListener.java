package cc.udp.kakaobot;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;
import cc.udp.httpjson.HttpJson;
import cc.udp.httpjson.HttpJsonObject;
import cc.udp.httpjson.HttpJsonTask;
import models.Action;

public class KakaoTalkNotificationListener extends NotificationListenerService {

    private static String TAG = "KTNL";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.kakao.talk")) {

            Bundle extras = sbn.getNotification().extras;
//            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
//            CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

            Log.d(TAG, "onNotificationPosted ~ " +
                    " packageName: " + sbn.getPackageName() +
                    " id: " + sbn.getId() +
                    " postTime: " + sbn.getPostTime() +
                    " text : " + text);

            String responseString = getResponseMessage(text.toString());

            if (!responseString.equals("")) {
                NotificationCompat.Action action = getWearReplyAction(sbn.getNotification());
                Action act = new Action(action, sbn.getPackageName(), true);

                try {
                    act.sendReply(getBaseContext(), responseString);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            } else if (text.toString().startsWith("!")) {
                sendReply(text.toString(),  sbn);
            }
        }
    }

    private static NotificationCompat.Action getWearReplyAction(Notification n) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(n);
        for (NotificationCompat.Action action : wearableExtender.getActions()) {
//            for (int x = 0; x < action.getRemoteInputs().length; x++) {
//                RemoteInput remoteInput = action.getRemoteInputs()[x];
//                if (remoteInput.getResultKey().toLowerCase().contains(REPLY_KEYWORD))
//                    return action;
//            }
            return action;
        }
        return null;
    }

    private String getResponseMessage(String text) {
        if (text.charAt(0) == '!') {
            String[] messages = text.substring(1).split(" ");

            switch (messages[0]) {
                case "핑": {
                    return "퐁!";
                }
            }
        }

        return "";
    }

    private void sendReply(final String text, final StatusBarNotification sbn) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("message", text);

        String url = "http://api2.udp.cc/kakaobot/message";
        new HttpJson(url, params, new HttpJsonTask() {
            @Override
            public void done(HttpJsonObject json) {
                if (json == null) {
                    return;
                }

                String value = json.getString("message");

                if (value != null && !value.equals("")) {
                    NotificationCompat.Action action = getWearReplyAction(sbn.getNotification());
                    Action act = new Action(action, sbn.getPackageName(), true);

                    try {
                        act.sendReply(getBaseContext(), value);
                    } catch (Exception e) {
//                    e.printStackTrace();
                    }
                }
            }
        }).post();
    }
}
