package com.errorhandling;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.errorhandling.utils.CallAPI;
import com.errorhandling.utils.LogFile;

public class CrashActivity extends AppCompatActivity {
  public static final String KeyDebugGroups = "CrashActivity-debug-groups";
  public static final String KeyCallBackActivity = "CrashActivity-call-back-activity";
  public static final String KeyInfo = "CrashActivity-info";
  public static final String KeyTrace = "CrashActivity-trace";
  public static final String KeyUrlApi = "CrashActivity-url_api";


  private Class<?> clsRunner = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crash);

    AppCompatButton btnRun = findViewById(R.id.activity_crash_run);
    AppCompatButton btnExit = findViewById(R.id.activity_crash_exit);
    AppCompatButton btnShare = findViewById(R.id.activity_crash_share);

    Intent intent = getIntent();


    if (intent == null && !intent.hasExtra(KeyInfo) && !intent.hasExtra(KeyTrace)) {
      finish();
    }

    try {
      if (intent.getStringExtra(KeyCallBackActivity).length() > 0) {
        clsRunner = Class.forName(intent.getStringExtra(KeyCallBackActivity));
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    report(intent.getStringExtra(KeyUrlApi), intent.getStringExtra(KeyInfo), intent.getStringExtra(KeyTrace));

    btnRun.setOnClickListener((View v) -> {
      report(intent.getStringExtra(KeyUrlApi), intent.getStringExtra(KeyInfo), intent.getStringExtra(KeyTrace));
      if (clsRunner != null) startActivity(new Intent(this, clsRunner));
    });

    btnExit.setOnClickListener((View v) -> android.os.Process.killProcess(android.os.Process.myPid()));

    btnShare.setOnClickListener((View v) -> {
      Intent i = new Intent(Intent.ACTION_SEND);
      i.setType("text/plain");
      i.putExtra(Intent.EXTRA_SUBJECT, "?????????? ??????");
      i.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(KeyInfo) + "\n\n" + intent.getStringExtra(KeyTrace));

      startActivity(i);
    });

    btnShare.setVisibility(intent.getBooleanExtra(KeyDebugGroups, false) ? View.VISIBLE : View.GONE);
  }


  private void report(String urlApi, String info, String trace) {
    Map<String, String> map = new LinkedHashMap<>();

    map.put("url", urlApi);
    map.put("info", info);
    map.put("error", trace);

    Log.i("info", "report: " + map);


    try {
      File logFile = (new LogFile(getApplicationContext())).logE("crash", info + "\n" + trace)
        .lastLogFile();
      if(logFile.exists()){
        map.put("file", logFile.getPath());
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }

    (new CallAPI()).execute(map);
  }
}
