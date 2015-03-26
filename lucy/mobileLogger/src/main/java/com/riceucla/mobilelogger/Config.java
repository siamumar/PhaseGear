package com.riceucla.mobilelogger;

/**
 * Configuration file for the core functionality of Lucy.
 * See the comments for each constant for configuration instructions. All fields are mandatory for the functionality of the application.
 *
 * @author Kevin Lin, Rice University
 * @since 1/5/2014
 */
public class Config {
    // Fully qualified URL of the Flask upload script on the server.
    public static final String UPLOAD_BASE_URL = "http://ec2-54-85-147-87.compute-1.amazonaws.com/upload";

    // Interval between upload attempts, in seconds
    public static final int UPLOAD_INTERVAL = 1000;

    // Components to be logged. Set to true to log the component; false otherwise.
    public static final boolean LOG_CALLS = true;
    public static final boolean LOG_SMS = true;
    public static final boolean LOG_WEB = true;
    public static final boolean LOG_LOCATION = true;
    public static final boolean LOG_APP = true;
    public static final boolean LOG_WIFI = true;
    public static final boolean LOG_CELLULAR = true;
    public static final boolean LOG_ACCELEROMETER = true;
    public static final boolean LOG_DEVICE = true;
    public static final boolean LOG_NETWORK = true;
    public static final boolean LOG_SCREEN_STATUS = true;
    public static final boolean LOG_STEPS = true;
}
