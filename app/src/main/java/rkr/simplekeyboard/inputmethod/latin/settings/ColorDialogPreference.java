package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import rkr.simplekeyboard.inputmethod.R;

public final class ColorDialogPreference extends Preference
    implements Slider.OnChangeListener {

  public interface ValueProxy {
    int readValue(final String key);

    void writeDefaultValue(final String key);

    void writeValue(final int value, final String key);
  }

  private TextView mValueView;
  private Slider sliderRed, sliderGreen, sliderBlue;
  private ValueProxy mValueProxy;

  public ColorDialogPreference(Context context) {
    super(context);
  }

  public ColorDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ColorDialogPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    super.onClick();

    LayoutInflater li = LayoutInflater.from((AppCompatActivity) getContext());
    View view = li.inflate(R.layout.color_dialog, null, false);

    sliderRed = (Slider) view.findViewById(R.id.slider_red);
    sliderGreen = (Slider) view.findViewById(R.id.slider_green);
    sliderBlue = (Slider) view.findViewById(R.id.slider_blue);
    mValueView = (TextView) view.findViewById(R.id.tv_value);

    sliderRed.addOnChangeListener(this);
    sliderGreen.addOnChangeListener(this);
    sliderBlue.addOnChangeListener(this);

    final var key = getKey();
    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(getTitle())
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              final int value =
                  Color.rgb(sliderRed.getValue(), sliderGreen.getValue(), sliderBlue.getValue());
              mValueProxy.writeValue(value, key);
            })
        .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
        .setNeutralButton(
            R.string.button_default,
            (d, w) -> {
              mValueProxy.writeDefaultValue(key);
            })
        .setView(view)
        .setCancelable(false)
        .show();

    final int color = mValueProxy.readValue(getKey());
    sliderRed.setValue(Color.red(color));
    sliderGreen.setValue(Color.green(color));
    sliderBlue.setValue(Color.blue(color));
    setHeaderText(color);
  }

  public void setInterface(final ValueProxy proxy) {
    mValueProxy = proxy;
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    int color = Color.rgb(sliderRed.getValue(), sliderGreen.getValue(), sliderBlue.getValue());
    setHeaderText(color);
  }

  private void setHeaderText(int color) {
    mValueView.setText(getValueText(color));
    boolean bright = Color.red(color) + Color.green(color) + Color.blue(color) > 128 * 3;
    mValueView.setTextColor(bright ? Color.BLACK : Color.WHITE);
    mValueView.setBackgroundColor(color);
  }

  private String getValueText(final int value) {
    String temp = Integer.toHexString(value);
    for (; temp.length() < 8; temp = "0" + temp);
    return temp.substring(2).toUpperCase();
  }
}
