package rkr.simplekeyboard.inputmethod;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.logger.Logger;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Process;

public class AppLoader extends Application implements Thread.UncaughtExceptionHandler {

  // For optimality maintain single Context & Apploader instance
  static AppLoader instance;
  static Context applicationContext;
  boolean isAtLeastAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    applicationContext = this;
    Thread.setDefaultUncaughtExceptionHandler(this);
    
    if (isAtLeastAndroid12) {
      DynamicColors.applyToActivitiesIfAvailable(this);
    }
    //checkGhostIDEAvailibility();
  }
  
   @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    Intent intent =
        new Intent(applicationContext, CrashActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra("error", ThrowableUtils.getFullStackTrace(throwable));

    PendingIntent pendingIntent =
        PendingIntent.getActivity(applicationContext, 11111, intent, PendingIntent.FLAG_IMMUTABLE);

    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

    Process.killProcess(Process.myPid());
    System.exit(1);
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

  public static Context getContext() {
    return applicationContext;
  }

  public static AppLoader getInstance() {
    return instance;
  }
}