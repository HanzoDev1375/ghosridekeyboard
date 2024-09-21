package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.R;
import com.google.android.material.slider.Slider;

/**
 * @author EUP
 */
public class SliderDialogPreference extends Preference
    implements Slider.OnChangeListener, Slider.OnSliderTouchListener {

  public interface ValueProxy {
    int readValue(final String key);

    int readDefaultValue(final String key);

    void writeValue(final int value, final String key);

    void writeDefaultValue(final String key);

    String getValueText(final int value);

    void feedbackValue(final int value);
  }

  private int min, max, stepSize;
  private ValueProxy proxy;
  private TextView sliderValue;
  private Slider slider;

  public SliderDialogPreference(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, R.styleable.SliderDialogPreference, defStyleAttr, defStyleRes);

    min = a.getInteger(R.styleable.SliderDialogPreference_minValue, 0);
    max = a.getInteger(R.styleable.SliderDialogPreference_maxValue, 100);
    stepSize = a.getInteger(R.styleable.SliderDialogPreference_stepValue, 0);
    a.recycle();
  }

  
  public SliderDialogPreference(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  
  public SliderDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, android.R.attr.preferenceStyle);
  }

  
  public SliderDialogPreference(@NonNull Context context) {
    this(context, null);
  }

  public void setInterface(final ValueProxy proxy) {
    this.proxy = proxy;
    final int value = this.proxy.readValue(getKey());
    setSummary(this.proxy.getValueText(value));
  }

  private int getProgressFromValue(final int value) {
    return value - min;
  }

  private int getValueFromProgress(final int progress) {
    return progress + min;
  }

  private int clipValue(final int value) {
    final int clippedValue = Math.min(max, Math.max(min, value));
    if (stepSize <= 1) {
      return clippedValue;
    }
    return clippedValue - (clippedValue % stepSize);
  }

  private int getClippedValueFromProgress(final int progress) {
    return clipValue(getValueFromProgress(progress));
  }

  @Override
  protected void onClick() {
    super.onClick();

    LayoutInflater li = LayoutInflater.from((AppCompatActivity) getContext());
    View view = li.inflate(R.layout.slider_preference_layout, null, false);

    slider = (Slider) view.findViewById(R.id.slider);
    sliderValue = (TextView) view.findViewById(R.id.slider_value);

    slider.setValueFrom(min);
    slider.setValueTo(max);
    slider.setStepSize(stepSize);
    slider.addOnChangeListener(this);
    slider.addOnSliderTouchListener(this);

    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(getTitle())
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              final String key = getKey();
              final int value = getClippedValueFromProgress((int) slider.getValue());
              setSummary(proxy.getValueText(value));
              proxy.writeValue(value, key);
            })
        .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
        .setNeutralButton(
            R.string.button_default,
            (d, w) -> {
              final String key = getKey();
              final int defaultValue = proxy.readDefaultValue(key);
              setSummary(proxy.getValueText(defaultValue));
              proxy.writeDefaultValue(key);
            })
        .setView(view)
        .setCancelable(false)
        .show();

    final int value = proxy.readValue(getKey());
    sliderValue.setText(proxy.getValueText(value));
    slider.setValue(getProgressFromValue(clipValue(value)));
  }

  @Override
  public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
    var clipValue = getClippedValueFromProgress((int) value);
    sliderValue.setText(proxy.getValueText(clipValue));
  }

  @Override
  public void onStartTrackingTouch(@NonNull Slider slider) {
    // No-op
  }

  @Override
  public void onStopTrackingTouch(@NonNull Slider slider) {
    proxy.feedbackValue(getClippedValueFromProgress((int) slider.getValue()));
  }
}
