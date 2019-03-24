package hawerner.browser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.InterruptedByTimeoutException;

import im.delight.android.webview.AdvancedWebView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements AdvancedWebView.Listener {
    private AdvancedWebView mWebView;

    private SwipeRefreshLayout mySwipeRefreshLayout;
    private EditText urlBar;
    private ImageView favicon;
    private FloatingActionButton passmanagerButton;

    public static int screenXResolution;

    private boolean loadPasswordManager = true;

    //WebView variables
    protected Boolean locationEnabled = false;
    protected Boolean desktopModeEnabled = false;
    protected Boolean fullscreenEnabled = false;
    protected int Fullscreen = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    protected int fullscreen = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideNavigationBar();
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                int screenHeight = getWindow().getDecorView().getRootView().getHeight();

                int keypadHeight = screenHeight - r.bottom;

                //Log.d(TAG, "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) {
                    //Keyboard is opened
                    //hideNavigationBar();
                    showNavigationBar();
                }
                else {
                    // keyboard is closed
                    hideNavigationBar();
                }
            }
        });
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        screenXResolution = metrics.widthPixels;
        checkPermission();
        String loadingUrl = "https://google.com/";
        if (getIntent().getExtras() != null)
            loadingUrl = getIntent().getStringExtra("url");

        favicon = findViewById(R.id.favicon);
        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        urlBar = (EditText) findViewById(R.id.urlBar);
        mWebView.setListener(this, this);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
//webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.setLongClickable(true);
       /* mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AdvancedWebView mWebview = (AdvancedWebView) view;
                return super.;
            }
        });*/
        mWebView.setOnTouchListener(new OnSwipeTouchListener(){
            @Override
            public boolean onSwipeRight() {
                if (mWebView.canGoBack()){
                    mWebView.goBack();
                }
                return super.onSwipeRight();
            }

            @Override
            public boolean onSwipeLeft() {
                LinearLayout bar = findViewById(R.id.bar);
                HorizontalScrollView downBar = findViewById(R.id.downBar);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bar.getLayoutParams();
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) downBar.getLayoutParams();
                if (params.height != 0){
                    params.height = 0;
                    params1.height = params.height;
                    bar.setLayoutParams(params);
                    downBar.setLayoutParams(params1);
                }
                else {
                    final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    params.height = (int) (50 * scale + 0.5f);
                    params1.height = params.height;
                    bar.setLayoutParams(params);
                    downBar.setLayoutParams(params1);
                }
                Log.i("Swipe", String.valueOf(params.height));
                return super.onSwipeLeft();
            }
        });
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("Refresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        mWebView.reload();
                    }
                }
        );

        passmanagerButton = (FloatingActionButton) this.findViewById(R.id.myFAB);
        passmanagerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callPasswordManager();
            }
        });
        passmanagerButton.hide();

        urlBar.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    loadUrl(urlBar.getText().toString());
                    //Toast.makeText(MainActivity.this, mWebView.getUrl(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });


        mWebView.addJavascriptInterface(new JSInterface(), "JSInterface");
        mWebView.loadUrl(loadingUrl);
        // ...
    }

    private void callPasswordManager() {
        loadPasswordManager = false;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.hawerner.passmanager", "com.hawerner.passmanager.getUsernameAndPassword"));
        startActivityForResult(intent, 1);
    }

    private void loadUrl(String url) {
        if (!url.startsWith("http")){
            if (url.contains(" ")) {
                url = "google.com/search?q=" + url;
                url.replace(" ", "+");
            }
            url = "https://" + url;
        }
        mWebView.loadUrl(url);
        Log.i("T", url);
    }

    //@SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

        hideNavigationBar();

        mWebView.onResume();

        urlBar.setText(mWebView.getUrl());
        // ...
    }

    private void hideNavigationBar(){
        //enable fullscreen mode
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(this.fullscreen);

    }

    private void showNavigationBar(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
    }

    //@SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause(); // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy(); // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent); // ...
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && intent != null){
            String username = intent.getStringExtra("username");
            String password = intent.getStringExtra("password");
            mWebView.evaluateJavascript(JSInterface.fillInputBoxes(username, password), null);
            loadPasswordManager = true;
            Log.i("T", username);
        }
        else{
            if (intent == null){
                Log.i("T", "Intent je null");
            }
            Log.i("T", String.valueOf(resultCode));
        }
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) { return; } // ...
        super.onBackPressed();
    }
    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        if (!url.startsWith("http")){
            mWebView.stopLoading();
            //mWebView.goBack();
            mySwipeRefreshLayout.setRefreshing(false);
            urlBar.setText(mWebView.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            }catch (Exception e){
                //TODO: obavesti korisnika da treba da instalira app ili da upali play store ili da ponudi da upali play store
            }
            return;
        }
        mySwipeRefreshLayout.setRefreshing(true);
        urlBar.setText(url);
        this.favicon.setImageBitmap(favicon);
        Log.i("T", mWebView.getUrl());
    }
    @Override
    public void onPageFinished(String url) {
        mySwipeRefreshLayout.setRefreshing(false);
        this.favicon.setImageBitmap(mWebView.getFavicon());
        MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(mWebView.getTitle(), mWebView.getFavicon(), getResources().getColor(R.color.colorPrimary)));
        mWebView.evaluateJavascript(JSInterface.checkIfLoginPage(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.i("T", s);
                if (s.toLowerCase().equals("true") && loadPasswordManager){
                    //callPasswordManager();
                    passmanagerButton.show();
                }
                else{
                    passmanagerButton.hide();
                }
            }
        });
    }
    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        //mWebView.loadUrl("file:///android_asset/index.html");
        Log.i("URL", String.valueOf(errorCode));
        /*if (errorCode == -10){ //unknown url scheme
            mWebView.stopLoading();
            //mWebView.goBack();
            mySwipeRefreshLayout.setRefreshing(false);
            urlBar.setText(mWebView.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(failingUrl));
            startActivity(intent);
        }*/
    }
    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        if (AdvancedWebView.handleDownload(this, url, suggestedFilename)) {
            // download successfully handled
        }
        else {
            // download couldn't be handled because user has disabled download manager app on the device
            // TODO show some notice to the user
        }

    }

    @Override
    public void onExternalPageRequest(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }


    public void urlSet(View view) {

        String input = urlBar.getText().toString();
        loadUrl(input);
    }


    public void dummyButton(View view) {
        Toast.makeText(MainActivity.this, "dummyButton", Toast.LENGTH_LONG);
        Log.i("DummyButton", "Clicked");
    }

    private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(),
                LoadUrl.class);

        shortcutIntent.setAction(Intent.ACTION_VIEW);
        shortcutIntent.setData(Uri.parse(mWebView.getUrl()));

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mWebView.getTitle());
        //addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, mWebView.getFavicon());
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
        getApplicationContext().sendBroadcast(addIntent);
    }

    public void addShortcut(View view) {
        this.addShortcut();
    }


    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Write external storage permission is required for downloading files");
                    builder.setTitle("Write external storage");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    0
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0
                    );
                }
            }else {
                // Permission already granted
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case 0:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission granted
                }else {
                    // Permission denied
                }
            }
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    findViewById(R.id.locationButton).callOnClick();
                }else {
                    // Permission denied
                }
            }
        }
    }

    public void locationAccessChange(View view) {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Location permission is required to use location services online");
                    builder.setTitle("Location permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    1
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1
                    );
                }
            } else {
                Drawable icon;
                if (!this.locationEnabled) {
                    icon = ContextCompat.getDrawable(this, R.drawable.button_location_on);
                }
                else{
                    icon = ContextCompat.getDrawable(this, R.drawable.button_location_off);
                }

                locationEnabled = !locationEnabled;
                // Permission already granted
                mWebView.setGeolocationEnabled(locationEnabled);
                ((Button) view).setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
            }
        }
    }


    public void desktopModeToggle(View view) {
        Drawable icon;

        if (desktopModeEnabled){
            icon = ContextCompat.getDrawable(this, R.drawable.button_phone);
        }
        else{

            icon = ContextCompat.getDrawable(this, R.drawable.button_computer);
        }
        desktopModeEnabled = !desktopModeEnabled;
        ((Button) view).setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
        mWebView.setDesktopMode(desktopModeEnabled);
        mWebView.reload();
    }

    public void fullscreenEnter(View view) {
        Drawable icon;
        if (fullscreenEnabled){
            icon = ContextCompat.getDrawable(this, R.drawable.fullscreen_enter);
            this.fullscreen = 0;
        }
        else{
            icon = ContextCompat.getDrawable(this, R.drawable.fullscreen_exit);
            this.fullscreen = this.Fullscreen;
        }

        this.hideNavigationBar();

        fullscreenEnabled = !fullscreenEnabled;
        ((Button) view).setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
    }
}
