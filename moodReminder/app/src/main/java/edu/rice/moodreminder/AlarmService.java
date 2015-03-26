package edu.rice.moodreminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Handles notification generation behavior. Called via intent by the alarm wakelock.
 *
 * @author Kevin Lin
 * @since 10/23/2014
 */
public class AlarmService extends IntentService {

    public AlarmService() {
        super("MoodReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Generate a notification when the intent is received.
        // Can customize title and message as need be.
        generateMoodNotification(Config.NOTIFICATION_TITLE, Config.NOTIFICATION_MESSAGE);
    }

    /**
     * Generates a notification with the specified parameters.
     * It starts MoodReminderActivity.class on tap.
     *
     * @param title: title of notification
     * @param message: message of notification
     */
    private void generateMoodNotification(String title, String message) {
        Context context = this;
        int icon = R.drawable.ic_launcher; //TODO: Change this icon
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);
    }
}
