package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import rkr.simplekeyboard.inputmethod.R;

/**
 * @author EUP
 */
public class SubTextPreference extends Preference {

  private CharSequence subText;

  public SubTextPreference(@NonNull Context context) {
    this(context, null);
  }

  public SubTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubTextPreference(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public SubTextPreference(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    final TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.SubTextPreference, defStyleAttr, defStyleRes);

    try {
      subText = a.getString(R.styleable.SubTextPreference_subText);
    } finally {
      a.recycle();
    }

    setLayoutResource(R.layout.subtext_preference);
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    View itemView = holder.itemView;

    final TextView subTextView = (TextView) holder.findViewById(R.id.subtext);
    if (subTextView != null) {
      final CharSequence subText = getSubText();
      if (!TextUtils.isEmpty(subText)) {
        subTextView.setText(subText);
        subTextView.setVisibility(View.VISIBLE);
      } else {
        subTextView.setVisibility(View.GONE);
      }
    }
  }

  public void setSubText(CharSequence subText) {
    if (!TextUtils.equals(subText, this.subText)) {
      this.subText = subText;
      notifyChanged();
    }
  }

  public void setSubText(int titleResId) {
    setSubText(getContext().getString(titleResId));
  }

  public java.lang.CharSequence getSubText() {
    return subText;
  }
}
