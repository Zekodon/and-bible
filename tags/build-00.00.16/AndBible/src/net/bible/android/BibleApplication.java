package net.bible.android;

import net.bible.android.activity.R;
import net.bible.android.device.ProgressNotificationManager;
import net.bible.android.view.activity.base.Dialogs;
import net.bible.service.common.CommonUtils;

import org.apache.commons.lang.StringUtils;
import org.crosswire.common.util.Reporter;
import org.crosswire.common.util.ReporterEvent;
import org.crosswire.common.util.ReporterListener;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class BibleApplication extends Application{

	private static final String TEXT_SIZE_PREF = "text_size_pref";
	private static BibleApplication singleton;
	private static final String TAG = "BibleApplication";
	
	private Activity currentActivity;
	
	@Override
	public void onCreate() {
		super.onCreate();

		// save to a singleton to allow easy access from anywhere
		singleton = this;

		Log.i(TAG, "OS:"+System.getProperty("os.name")+" ver "+System.getProperty("os.version"));
		Log.i(TAG, "Java:"+System.getProperty("java.vendor")+" ver "+System.getProperty("java.version"));
		Log.i(TAG, "Java home:"+System.getProperty("java.home"));
		Log.i(TAG, "User dir:"+System.getProperty("user.dir")+" Timezone:"+System.getProperty("user.timezone"));
		
		installJSwordErrorReportListener();

		// some changes may be required for different versions
		upgradePersistentData();
		
        //initialise link to Android progress control display in Notification bar
       ProgressNotificationManager.getInstance().initialise();
	}

	public static BibleApplication getApplication() {
		return singleton;
	}

	private void upgradePersistentData() {
		SharedPreferences prefs = CommonUtils.getSharedPreferences();
		if (prefs.getInt("version", -1) < CommonUtils.getApplicationVersionNumber()) {
			Log.d(TAG, "*** Upgrading preference");
			Editor editor = prefs.edit();
			String textSize = "16";
			if (prefs.contains(TEXT_SIZE_PREF)) {
				Log.d(TAG, "*** text size pref exists");
				textSize = prefs.getString(TEXT_SIZE_PREF, "16");
				Log.d(TAG, "*** existing value:"+textSize);
				editor.remove(TEXT_SIZE_PREF);
			}
			int textSizeInt = Integer.parseInt(textSize);
			editor.putInt(TEXT_SIZE_PREF, textSizeInt);
			
			editor.putInt("version", CommonUtils.getApplicationVersionNumber());
			editor.commit();
			Log.d(TAG, "*** Finished Upgrading preference");
		}
	}
	
    /** JSword calls back to this listener in the event of some types of error
     * 
     */
    private void installJSwordErrorReportListener() {
        Reporter.addReporterListener(new ReporterListener() {
			@Override
			public void reportException(final ReporterEvent ev) {
				showMsg(ev);
			}

			@Override
			public void reportMessage(final ReporterEvent ev) {
				showMsg(ev);
			}
			
			private void showMsg(ReporterEvent ev) {
				String msg = null;
				if (ev==null) {
					msg = getString(R.string.error_occurred);
				} else if (!StringUtils.isEmpty(ev.getMessage())) {
					msg = ev.getMessage();
				} else if (ev.getException()!=null && StringUtils.isEmpty(ev.getException().getMessage())) {
					msg = ev.getException().getMessage();
				} else {
					msg = getString(R.string.error_occurred);
				}
				
				Dialogs.getInstance().showErrorMsg(msg);
			}
        });
    }
    
	@Override
	public void onTerminate() {
		Log.i(TAG, "onTerminate");
		super.onTerminate();
	}
	
}
