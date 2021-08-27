package it.chilledpanda.grocerypal.activities.scan;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageButton;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import it.chilledpanda.grocerypal.R;

public class ScanActivity extends Activity {
    DecoratedBarcodeView mDBV;
    private CaptureManager captureManager;

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        captureManager.onSaveInstanceState(outState);
    }

    boolean torch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);

        mDBV = findViewById(R.id.scanner_view);
        mDBV.setStatusText("");
        mDBV.getBarcodeView().getCameraSettings().setAutoFocusEnabled(true);

        ImageButton ab = findViewById(R.id.flash_butt);
        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!torch) {
                    mDBV.setTorchOn();
                    torch=true;
                }
                else{
                    mDBV.setTorchOff();
                    torch=false;
                }
            }
        });
        // important code, initial capture
        captureManager = new CaptureManager(this,mDBV);
        captureManager.initializeFromIntent(getIntent(),savedInstanceState);
        captureManager.decode();
    }




}

