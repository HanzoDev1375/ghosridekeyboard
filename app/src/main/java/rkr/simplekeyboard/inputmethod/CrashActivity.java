package rkr.simplekeyboard.inputmethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * @author EUP
 */
public class CrashActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (intent == null) return;
    String errorMessage = intent.getStringExtra("error");
    showErrorDialog(errorMessage);
  }

  void showErrorDialog(String errorMessage) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("App crashed due to an error!");
    builder.setMessage(errorMessage);
    builder.setPositiveButton("Exit", (d, which) -> finish());
    builder.setNegativeButton(
        "Share error",
        (d, which) -> {
          shareText(errorMessage);
        });
    builder.setCancelable(false);

    final AlertDialog dialog = builder.create();
    dialog.show();
    ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
  }

  void shareText(final String mText) {
    Intent shareTextIntent = new Intent();
    shareTextIntent.setAction(Intent.ACTION_SEND);
    shareTextIntent.putExtra(Intent.EXTRA_TEXT, mText);
    shareTextIntent.setType("text/plain");
    startActivity(
        Intent.createChooser(shareTextIntent, "__"));
  }
}
