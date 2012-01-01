/*
 * AboutActivity
 * Copyright (C) 2010 MarcoDuff.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.marcoduff.util.aboutactivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * AboutActivity è una utility per tutti gli sviluppatori Android per la gestione della finestra di about dei loro programmi.
 * 
 * @author Marco "Duff" Palermo - www.marcoduff.com
 * @version 1.3
 */
public class AboutActivity extends PreferenceActivity {
	private static final String EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME";
	private static final String EXTRA_AUTHOR_SITE = "EXTRA_AUTHOR_SITE";
	private static final String EXTRA_AUTHOR_EMAIL = "EXTRA_AUTHOR_EMAIL";
	private static final String EXTRA_AUTHOR_TWITTER = "EXTRA_AUTHOR_TWITTER";
	private static final String EXTRA_DONATE_URL = "EXTRA_DONATE_URL";
	private static final String EXTRA_REPORT_BUG_URL = "EXTRA_REPORT_BUG_URL";
	private static final String EXTRA_SHOW_IN_MARKET = "EXTRA_SHOW_IN_MARKET";
	private static final String EXTRA_AUTHOR_MARKET_NAME = "EXTRA_AUTHOR_MARKET_NAME";
	
	private static final String RAW_README = "readme";
	private static final String RAW_FAQ = "faq";
	private static final String RAW_CHANGELOG = "changelog";
	private static final String RAW_EULA = "eula";
	private static final String RAW_PRIVACY = "privacy";
	private static final String RAW_TODO = "todo";
	
	private static final String PREFS_ABOUT_ACTIVITY = "PREFS_ABOUT_ACTIVITY";
	private static final String PREF_EULA_KEY = "PREF_EULA_KEY";
	private static final String PREF_LAST_VERSION_KEY = "PREF_LAST_VERSION_KEY";
	
	/**
	 * Listener EULA.
	 * 
	 * @author Marco "Duff" Palermo - www.marcoduff.com
	 */
	public static interface OnStartupEulaListener {
		/**
		 * Richiamata quando l'utente interagisce con la dialog di accettazione dell'EULA.
		 * 
		 * @param isAccepted Ha valore <code>true</code> se l'utente accetta l'EULA, <code>false</code> altrimenti.
		 */
		public void onEulaAction(boolean isAccepted);
	}

	/**
	 * Listener ChangeLog.
	 * 
	 * @author Marco "Duff" Palermo - www.marcoduff.com
	 */
	public static interface OnStartupChangeLogListener {
		/**
		 * Richiamata quando viene rilevato un cambio di versione mentre si visualizza la dialog di ChangeLog in avvio.
		 * 
		 * @param oldVersion La vecchia versione installata.
		 * @param newVersion La nuova versione.
		 */
		public void onVersionChanged(String oldVersion, String newVersion);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
        ListView listView = new ListView(this);
        listView.setId(android.R.id.list);
        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
		layout.addView(listView);

        // START AboutActivity Copyright
        /* 
         * Ti chiedo di lasciare inalterata l'intera nota di copyright
         * compreso il link al sito android.marcoduff.com per rispettare
         * il mio lavoro, grazie!
         * 
         * I request you retain the full copyright notice below including
         * the link to android.marcoduff.com to gives respect to my work,
         * thanks!
         */
		TextView poweredView = new TextView(this);
		poweredView.setText("Powered by AboutActivity");
		poweredView.setGravity(Gravity.CENTER_HORIZONTAL);
		poweredView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AboutActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://android.marcoduff.com/")));
			}
		});
		layout.addView(poweredView);
        // END AboutActivity Copyright
        
        this.setContentView(layout);
        
        Preference preferenceVersion = getPreferenceVersion();
        Preference preferenceAuthorName = getPreferenceFromStringExtra(EXTRA_AUTHOR_NAME, R.string.aboutactivity_author);
        Preference preferenceAuthorSite = getPreferenceFromUrlExtra(EXTRA_AUTHOR_SITE, R.string.aboutactivity_authorsite, R.string.aboutactivity_authorsite_summary);
        Preference preferenceAuthorMail = getPreferenceFromMailExtra(EXTRA_AUTHOR_EMAIL, R.string.aboutactivity_authoremail, R.string.aboutactivity_authoremail_summary);
        Preference preferenceAuthorTwitter = getPreferenceFromTwitterExtra(EXTRA_AUTHOR_TWITTER, R.string.aboutactivity_authortwitter, R.string.aboutactivity_authortwitter_summary);
        Preference preferenceReadme = getPreferenceFromRaw(RAW_README, R.string.aboutactivity_readme, R.string.aboutactivity_readme_summary);
        Preference preferenceFaq = getPreferenceFromRaw(RAW_FAQ, R.string.aboutactivity_faq, R.string.aboutactivity_faq_summary);
        Preference preferenceChangeLog = getPreferenceFromRaw(RAW_CHANGELOG, R.string.aboutactivity_changelog, R.string.aboutactivity_changelog_summary);
        Preference preferenceLicense = getPreferenceFromRaw(RAW_EULA, R.string.aboutactivity_license, R.string.aboutactivity_license_summary);
        Preference preferencePrivacy = getPreferenceFromRaw(RAW_PRIVACY, R.string.aboutactivity_privacy, R.string.aboutactivity_privacy_summary);
        Preference preferenceTodo = getPreferenceFromRaw(RAW_TODO, R.string.aboutactivity_todo, R.string.aboutactivity_todo_summary);
        Preference preferenceDonate = getPreferenceFromUrlExtra(EXTRA_DONATE_URL, R.string.aboutactivity_donate, R.string.aboutactivity_donate_summary);
        Preference preferenceMarket = getPreferenceMarket(EXTRA_SHOW_IN_MARKET, R.string.aboutactivity_market, R.string.aboutactivity_market_summary);
        Preference preferenceAuthorMarket = getPreferenceAuthorMarket(EXTRA_AUTHOR_MARKET_NAME, R.string.aboutactivity_authormarket, R.string.aboutactivity_authormarket_summary);
        Preference preferenceReportBug = getPreferenceFromUrlExtra(EXTRA_REPORT_BUG_URL, R.string.aboutactivity_reportbug, R.string.aboutactivity_reportbug_summary);

        PreferenceScreen preferenceScreen = this.getPreferenceManager().createPreferenceScreen(this);
        addPreferenceCategory(preferenceScreen, R.string.aboutactivity_info_category, preferenceVersion, preferenceAuthorName, preferenceAuthorSite, preferenceAuthorMail, preferenceAuthorTwitter);
        addPreferenceCategory(preferenceScreen, R.string.aboutactivity_app_category, preferenceReadme, preferenceFaq, preferenceChangeLog, preferenceLicense, preferencePrivacy, preferenceTodo);
        addPreferenceCategory(preferenceScreen, R.string.aboutactivity_miscellaneous_category, preferenceDonate, preferenceMarket, preferenceAuthorMarket, preferenceReportBug);

        this.setPreferenceScreen(preferenceScreen);
	}

	/**
	 * Visualizza la dialog dell'EULA se questa non è mai stata accettata.
	 * 
	 * @param context Contesto di riferiemento.
	 * @return Restituisce <code>true</code> se l'utente ha già accettato l'EULA, <code>false</code> altrimenti.
	 */
	public static boolean showStartupEula(Context context) {
		return showStartupEula(context, false);
	}
	
	/**
	 * Visualizza la dialog dell'EULA se questa non è mai stata accettata.
	 * 
	 * @param context Contesto di riferiemento.
	 * @param forceShow Forza la visualizzazione.
	 * @return Restituisce <code>true</code> se l'utente ha già accettato l'EULA, <code>false</code> altrimenti.
	 */
	public static boolean showStartupEula(final Context context, boolean forceShow) {
		final SharedPreferences preferences = context.getSharedPreferences(PREFS_ABOUT_ACTIVITY,Context.MODE_PRIVATE);
		boolean isEulaAccepted = preferences.getBoolean(PREF_EULA_KEY, false);
		if(!isEulaAccepted||forceShow) {
			View dialogView = createDialogView(context, RAW_EULA);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.aboutactivity_license);
			builder.setView(dialogView);
			builder.setPositiveButton(R.string.aboutactivity_license_accept, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putBoolean(PREF_EULA_KEY, true).commit();
					if(context instanceof OnStartupEulaListener) ((OnStartupEulaListener)context).onEulaAction(true);
				}
			});
			builder.setNegativeButton(R.string.aboutactivity_license_refuse, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putBoolean(PREF_EULA_KEY, false).commit();
					if(context instanceof OnStartupEulaListener) ((OnStartupEulaListener)context).onEulaAction(false);
					if(context instanceof Activity) ((Activity)context).finish();
				}
			});
			builder.create().show();
			return false;
		}
		else return true;
	}

	public static boolean isEulaAccepted(Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(PREFS_ABOUT_ACTIVITY,Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_EULA_KEY, false);
	}

	
	/**
	 * Visualizza la dialog del ChangeLog se viene rilevato un cambio di verisone.
	 * 
	 * @param context Contesto di riferiemento.
	 * @return Restituisce <code>true</code> se non vi è stato cambio di versione, <code>false</code> altrimenti.
	 */
	public static boolean showStartupChangeLog(Context context) {
		return showStartupChangeLog(context, false);
	}

	/**
	 * Visualizza la dialog del ChangeLog se viene rilevato un cambio di verisone.
	 * 
	 * @param context Contesto di riferiemento.
	 * @param forceShow Forza la visualizzazione.
	 * @return Restituisce <code>true</code> se non vi è stato cambio di versione, <code>false</code> altrimenti.
	 */
	public static boolean showStartupChangeLog(Context context, boolean forceShow) {
		String currentVersion = getCurrentVersion(context);
		SharedPreferences preferences = context.getSharedPreferences(PREFS_ABOUT_ACTIVITY,Context.MODE_PRIVATE);
		String lastVersion = preferences.getString(PREF_LAST_VERSION_KEY, currentVersion);
		preferences.edit().putString(PREF_LAST_VERSION_KEY, currentVersion).commit();
		if(!lastVersion.equals(currentVersion)||forceShow) {
			View dialogView = createDialogView(context, RAW_CHANGELOG);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.aboutactivity_changelog);
			builder.setView(dialogView);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			if(context instanceof OnStartupChangeLogListener) ((OnStartupChangeLogListener)context).onVersionChanged(lastVersion, currentVersion);
			return false;
		}
		else return true;
	}

	/**
	 * Visualizza la dialog relativa al file asset README.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showReadmeDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_readme, RAW_README);}

	/**
	 * Visualizza la dialog relativa al file asset FAQ.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showFaqDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_faq, RAW_FAQ);}
	
	/**
	 * Visualizza la dialog relativa al file asset CHANGELOG.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showChangeLogDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_changelog, RAW_CHANGELOG);}

	/**
	 * Visualizza la dialog relativa al file asset EULA.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showEulaDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_license, RAW_EULA);}

	/**
	 * Visualizza la dialog relativa al file asset PRIVACY.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showPrivacyDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_privacy, RAW_PRIVACY);}

	/**
	 * Visualizza la dialog relativa al file asset TODO.
	 * 
	 * @param context Contesto di riferiemento.
	 */
	public static void showTodoDialog(Context context) {showGenericDialog(context, R.string.aboutactivity_todo, RAW_TODO);}

	/**
	 * Visualizza la dialog relativa al file asset in ingresso.
	 * 
	 * @param context Contesto di riferiemento.
	 * @param titleResId Risorsa per il titolo della dialog.
	 * @param filename Nome del file asset da visualizzare.
	 */
	public static void showGenericDialog(Context context, int titleResId, String filename) {
		View dialogView = createDialogView(context, filename);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(titleResId);
		builder.setView(dialogView);
		builder.create().show();
	}
	
	/**
	 * Restituisce l'intent da usare per richiamare l'activity.
	 * 
	 * @param context Contesto di riferiemento.
	 * @param authorName Nome dell'autore o null.
	 * @param authorSite Url del sito dell'autore o null.
	 * @param authorMail EMail dell'autore o null.
	 * @param twitter Username twitter dell'autore o null.
	 * @param donateUrl Url per effettuare donazioni o null.
	 * @param showInAndroidMarketUrl Se visualizzare o meno l'applicazione sul market android.
	 * @param authorAndroidMarketName Nome dell'autore registrato nel market android.
	 * @param reportBugUrl Url per sottomettere i bug.
	 * @return L'intent da usare per richiamare l'activity.
	 */
	public static Intent getAboutActivityIntent(
			Context context, String authorName, String authorSite, String authorMail, String twitter,
			String donateUrl, boolean showInAndroidMarketUrl, String authorAndroidMarketName, String reportBugUrl) {
    	Intent intent = new Intent(context, AboutActivity.class);
    	if(authorName!=null) intent.putExtra(AboutActivity.EXTRA_AUTHOR_NAME, authorName);
    	if(authorSite!=null) intent.putExtra(AboutActivity.EXTRA_AUTHOR_SITE, authorSite);
    	if(authorMail!=null) intent.putExtra(AboutActivity.EXTRA_AUTHOR_EMAIL, authorMail);
    	if(twitter!=null) intent.putExtra(AboutActivity.EXTRA_AUTHOR_TWITTER, twitter);
    	if(donateUrl!=null) intent.putExtra(AboutActivity.EXTRA_DONATE_URL, donateUrl);
    	if(showInAndroidMarketUrl) intent.putExtra(AboutActivity.EXTRA_SHOW_IN_MARKET, "true");
    	if(authorAndroidMarketName!=null) intent.putExtra(AboutActivity.EXTRA_AUTHOR_MARKET_NAME, authorAndroidMarketName);
    	if(reportBugUrl!=null) intent.putExtra(AboutActivity.EXTRA_REPORT_BUG_URL, reportBugUrl);
    	return intent;
	}
	
	private static View createDialogView(final Context context, String filename) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		String data = getRawData(context, filename);
		WebView webView = new WebView(context);
		webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
		webView.loadData(data, "text/html", "UTF-8");
		layout.addView(webView);
		
        // START AboutActivity Copyright
        /* 
         * Ti chiedo di lasciare inalterata l'intera nota di copyright
         * compreso il link al sito android.marcoduff.com per rispettare
         * il mio lavoro, grazie!
         * 
         * I request you retain the full copyright notice below including
         * the link to android.marcoduff.com to gives respect to my work,
         * thanks!
         */
		TextView poweredView = new TextView(context);
		poweredView.setText("Powered by AboutActivity");
		poweredView.setGravity(Gravity.CENTER_HORIZONTAL);
		poweredView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://android.marcoduff.com/")));
			}
		});
		layout.addView(poweredView);
        // END AboutActivity Copyright

		return layout;
	}
	
	private boolean addPreferenceCategory(PreferenceScreen preferenceScreen, int titleResId, Preference... preferences) {
		boolean addPreference = false;
		for(Preference preference : preferences) {
			if(preference!=null) addPreference = true;
		}
		if(addPreference) {
			PreferenceCategory preferenceCategory = new PreferenceCategory(this);
			preferenceCategory.setTitle(titleResId);
			preferenceScreen.addPreference(preferenceCategory);
			for(Preference preference : preferences) {
				if(preference!=null) preferenceCategory.addPreference(preference);
			}
			return true;
		}
		else return false;
	}
	
	private Preference getPreferenceVersion() {
		String appName = getApplicationName(this);
        String currentVersion = getCurrentVersion(this);
        Preference versionPreference = new Preference(this);
        versionPreference.setTitle(appName);
        versionPreference.setSummary(String.format(getString(R.string.aboutactivity_version), currentVersion));
        return versionPreference;
	}
	
	private static String getApplicationName(Context context) {
		ApplicationInfo applicationInfo = context.getApplicationInfo();
		return context.getString(applicationInfo.labelRes);
	}
	
	public static String getCurrentVersion(Context context) {
        try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
	    }
	    catch (NameNotFoundException e) {return "";}
	}
	
	private Preference getPreferenceFromStringExtra(String extra, int titleResId) {
        String extraValue = this.getIntent().getStringExtra(extra);
        if(extraValue!=null) {
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(extraValue);
            return preference;
        }
        else return null;
	}
	
	private Preference getPreferenceFromUrlExtra(String extra, int titleResId, int summaryResId) {
        String url = this.getIntent().getStringExtra(extra);
        if(url!=null) {
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(summaryResId);
            preference.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return preference;
        }
        else return null;
	}

	private Preference getPreferenceMarket(String extra, int titleResId, int summaryResId) {
        String flag = this.getIntent().getStringExtra(extra);
        if(flag!=null&&flag.equalsIgnoreCase("TRUE")) {
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(summaryResId);
            preference.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%1$s", this.getPackageName()))));
            return preference;
        }
        else return null;
	}

	private Preference getPreferenceAuthorMarket(String extra, int titleResId, int summaryResId) {
        String authorMarket = this.getIntent().getStringExtra(extra);
        if(authorMarket!=null) {
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(summaryResId);
            preference.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://search?q=pub:%1$s", URLEncoder.encode(authorMarket)))));
            return preference;
        }
        else return null;
	}
	
	private Preference getPreferenceFromTwitterExtra(String extra, int titleResId, int summaryResId) {
        String twitterUsername = this.getIntent().getStringExtra(extra);
        if(twitterUsername!=null) {
    		String url = String.format("http://www.twitter.com/%1$s", twitterUsername);
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(summaryResId);
            preference.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return preference;
        }
        else return null;
	}
	
	private Preference getPreferenceFromMailExtra(String extra, int titleResId, int summaryResId) {
        String email = this.getIntent().getStringExtra(extra);
        if(email!=null) {
    		String appName = getApplicationName(this);
    		String currentVersion = getCurrentVersion(this);
        	Intent intent = new Intent(Intent.ACTION_SEND);
        	intent.setType("plain/text");
        	intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        	intent.putExtra(Intent.EXTRA_SUBJECT, String.format("New mail from %1$s v%2$s", appName, currentVersion));
        	Preference preference = new Preference(this);
            preference.setTitle(titleResId);
            preference.setSummary(summaryResId);
            preference.setIntent(intent);
            return preference;
        }
        else return null;
	}
	
	private Preference getPreferenceFromRaw(String fileName, int titleResId, int summaryResId) {
		String data = getRawData(this, fileName);
		if(data!=null) {
	        DialogPreference dialogPreference = new RawDialogPreference(this, titleResId, data);
	        dialogPreference.setSummary(summaryResId);
	        dialogPreference.setNegativeButtonText(null);
            return dialogPreference;
		}
		else {
			return null;
		}
	}
	
	private static String getRawData(Context context, String fileName) {
		try {
			String packageName = context.getApplicationContext().getPackageName();
			Resources resources = context.getApplicationContext().getResources();
			int resourceIdentifier = resources.getIdentifier(fileName, "raw", packageName);	
			if(resourceIdentifier!=0) {
				InputStream inputStream = resources.openRawResource(resourceIdentifier);
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				String line;
				StringBuffer data = new StringBuffer();
				while((line=reader.readLine())!=null) {
					data.append(line);
				}
				reader.close();
				return data.toString();
			}
			else return null;
		}
		catch(IOException e) {
			return null;
		}
	}
	
	private class RawDialogPreference extends DialogPreference {
		private String data;
		
		public RawDialogPreference(Context context, int titleResId, String data) {
			super(context, null);
	        this.setTitle(titleResId);
	        this.setDialogTitle(titleResId);
	        this.data = data;
		}
		
		@Override
		protected void onPrepareDialogBuilder(Builder builder) {
			super.onPrepareDialogBuilder(builder);
			
			WebView webView = new WebView(this.getContext());
			webView.loadData(data, "text/html", "UTF-8");
			
			builder.setView(webView);
		}
	}
}
