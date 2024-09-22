package rkr.simplekeyboard.inputmethod;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.logger.Logger;
import android.content.Intent;
import android.os.Process;

public class AppLoader extends Application implements Thread.UncaughtExceptionHandler {

  // For optimality maintain single Apploader instance
  static AppLoader instance;
  static final long SCHEDULED_PROCESS_TERMINATION_TIME = 1500; //milliseconds
  static final boolean isAtLeastAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  
  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    Thread.setDefaultUncaughtExceptionHandler(this);
    
    if (isAtLeastAndroid12 && DynamicColors.isDynamicColorAvailable()) {
      DynamicColors.applyToActivitiesIfAvailable(this);
    }
    //checkGhostIDEAvailibility();
  }
  
  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    try {
      var intent = new Intent(this, CrashActivity.class);
      intent.putExtra("error", ThrowableUtils.getFullStackTrace(throwable));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
      scheduleProcessTermination();
    } catch (Throwable th) {
      th.printStackTrace();
    }
  }
  
  void checkGhostIDEAvailibility() {
    var isAvailable = false;
    var pkgName = "com.myapp";

    try {
      getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
      isAvailable = true;
    } catch (Exception e) {
      Logger.e("Error App Not Found ", e.getLocalizedMessage());
      isAvailable = false;
    }

    if (!isAvailable) {
      new MaterialAlertDialogBuilder(this)
          .setTitle("GhostIDE not installed")
          .setPositiveButton("Exit", (d, w) -> System.exit(0))
          .setNeutralButton("Continue anyway", null)
          .setCancelable(false)
          .show();
    }
  }
  
  public Context getContext() {
    return instance.getApplicationContext();
  }
  
  public static AppLoader getInstance() {
    return instance;
  }
  
  /**
   * Schedules process termination after a period of time.
   * <p> Not ideal unless you need to handle crash data before termination
   */
  private void scheduleProcessTermination() {
    new Thread(
            () -> {
              try {
                Thread.sleep(SCHEDULED_PROCESS_TERMINATION_TIME);
              } catch (InterruptedException ignore) {

              }
              Process.killProcess(Process.myPid());
              System.exit(1);
            })
        .start();
  }
}