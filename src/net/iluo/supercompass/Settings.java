package net.iluo.supercompass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings
{
  private static final String TAG = "Settings";

  public enum STYLE {
    ORIENTEERING,
    ORIENTEERING_WITH_MAP,
    NAVIGATOR,
  };

  public Settings(final Context context)
  {
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  STYLE GetStyle()
  {
    String value = preferences.getString("style", "orienteering");
    STYLE style = STYLE.ORIENTEERING;
    if (value.equals("orienteering_with_map")) style = STYLE.ORIENTEERING_WITH_MAP;
    else if (value.equals("navigator")) style = STYLE.NAVIGATOR;

    return style;
  }

  void SetStyle(STYLE style)
  {
    String value = "orienteering";
    if (style == STYLE.ORIENTEERING_WITH_MAP) value = "orienteering_with_map";
    else if (style == STYLE.NAVIGATOR) value = "navigator";

    SharedPreferences.Editor editor = preferences.edit();
    editor.putString("style", value);
    editor.commit();

    // Sanity check
    if (Build.debug) if (GetStyle() != style) Log.d(TAG, "SetStyle FAILED");
  }

  private SharedPreferences preferences;
}
