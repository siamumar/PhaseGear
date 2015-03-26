package edu.rice.moodreminder;

/**
 * Configuration file for the core functionality of Mood Reminder. We assume this application works within the framework of Lucy.
 * See the comments for each constant for configuration instructions. All fields are mandatory for the functionality of the application.
 *
 * @author Kevin Lin, Rice University
 * @since 12/12/2014
 */
public class Config {
    // Fully qualified URL of the Flask upload script on the server.
    public static final String UPLOAD_BASE_URL = "http://ec2-54-85-147-87.compute-1.amazonaws.com/upload";

    // Hour and minute representing the time at which the notification should be generated. Important: 24-hour format (i.e., 8 PM is hour 20 and minute 0)
    public static final int NOTIFICATION_HOUR = 20;
    public static final int NOTIFICATION_MINUTE = 0;

    // Title and message of the notification.
    public static final String NOTIFICATION_TITLE = "Mood and Activity Reminder";
    public static final String NOTIFICATION_MESSAGE = "How are you doing today?";

    // Name of the SQL table storing this data. This must match the name of the table on the server!
    public static final String TABLE_NAME = "mood";

    // String representations of the names of the parameters to be logged. Each parameter is allowed a 0-100 scale rating in the user interface.
    // These will be columns in the table TABLE_NAME above.
    public static String[] parameters = {"mood", "activity"};
}
