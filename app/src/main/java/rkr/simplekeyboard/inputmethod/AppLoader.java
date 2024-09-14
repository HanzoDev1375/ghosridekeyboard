package rkr.simplekeyboard.inputmethod;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.utilities.DynamicColor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.logger.Logger;

public class AppLoader extends Application {

  protected Context context;
  boolean isAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

  protected AppLoader newInstans() {
    var it = new AppLoader();
    if (it == null) {
      return it;
    }
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (isAndroid12) {
      DynamicColors.applyToActivitiesIfAvailable(this);
    }
    if (!appInstalledOrNot("com.myapp")) {
//      var dialog = new AlertDialog.Builder(this);
//      dialog.setTitle("AppNot Found");
//      dialog.setPositiveButton("exit", (i, c) -> System.exit(0));
//      dialog.show();
    }
  }

  private boolean appInstalledOrNot(String uri) {
    PackageManager pm = getPackageManager();
    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      Logger.e("Error App Not Found ", e.getLocalizedMessage());
    }
    return false;
  }

  public Context getContext() {
    return this.context;
  }
}
