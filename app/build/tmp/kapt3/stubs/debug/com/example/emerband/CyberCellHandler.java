package com.example.emerband;

/**
 * Utility class that handles reporting digital threats to a cyber cell
 * by initiating calls and sending alert SMS messages.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\r\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0018\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0004H\u0007J\u0018\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u0004H\u0007J\u0010\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0018\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/example/emerband/CyberCellHandler;", "", "()V", "DEFAULT_CYBER_ALERT_MESSAGE", "", "DEFAULT_CYBER_CELL_NUMBER", "KEY_CYBER_ALERT_MESSAGE", "KEY_CYBER_CELL_NUMBER", "PREFS_NAME", "TAG", "getCyberAlertMessage", "context", "Landroid/content/Context;", "getCyberCellNumber", "handleCyberCellAlert", "", "initiateCall", "saveCyberAlertMessage", "", "message", "saveCyberCellNumber", "number", "sendAlertSms", "showToast", "app_debug"})
public final class CyberCellHandler {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CyberCellHandler";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "EmerbandPrefs";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CYBER_CELL_NUMBER = "cyberCellNumber";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CYBER_ALERT_MESSAGE = "cyberAlertMessage";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String DEFAULT_CYBER_CELL_NUMBER = "1122334455";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String DEFAULT_CYBER_ALERT_MESSAGE = "I\'m under digital threat. Please help.";
    @org.jetbrains.annotations.NotNull
    public static final com.example.emerband.CyberCellHandler INSTANCE = null;
    
    private CyberCellHandler() {
        super();
    }
    
    /**
     * Handles the cyber cell alert by initiating a call and sending an SMS
     *
     * @param context Application context
     * @return true if at least one action (call or SMS) was successful
     */
    @kotlin.jvm.JvmStatic
    public static final boolean handleCyberCellAlert(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * Get the cyber cell phone number from SharedPreferences or use default
     */
    @kotlin.jvm.JvmStatic
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String getCyberCellNumber(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    /**
     * Get the cyber alert message from SharedPreferences or use default
     */
    @kotlin.jvm.JvmStatic
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String getCyberAlertMessage(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    /**
     * Save the cyber cell phone number to SharedPreferences
     */
    @kotlin.jvm.JvmStatic
    public static final void saveCyberCellNumber(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String number) {
    }
    
    /**
     * Save the cyber alert message to SharedPreferences
     */
    @kotlin.jvm.JvmStatic
    public static final void saveCyberAlertMessage(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    /**
     * Initiates a call to the cyber cell helpline
     */
    private final boolean initiateCall(android.content.Context context) {
        return false;
    }
    
    /**
     * Sends an alert SMS to the cyber cell helpline
     */
    private final boolean sendAlertSms(android.content.Context context) {
        return false;
    }
    
    /**
     * Show toast message
     */
    private final void showToast(android.content.Context context, java.lang.String message) {
    }
}