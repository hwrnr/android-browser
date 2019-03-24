package hawerner.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LoadUrl extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        String url;
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            url = intent.getData().toString();
            Log.i("url", intent.getData().toString());
        }
        else {
            url = "https://google.com";
        }

        Intent newIntet = new Intent(LoadUrl.this, MainActivity.class);
        newIntet.putExtra("url", url);
        newIntet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivity(newIntet);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }
}
