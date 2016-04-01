package org.itstep.pastukhov.qr_vin_scanner;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button qrScanner;
    Button vinScanner;
    public TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrScanner = (Button)findViewById(R.id.btnQRScanner);
        vinScanner = (Button)findViewById(R.id.btnVinScanner);
        result = (TextView)findViewById(R.id.result);

        qrScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR();
            }
        });

        vinScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanVin();
            }
        });
    }

    public void scanVin() {

        Log.d("tag", "scanVin()");

        Intent intent = new Intent(this, ZbarScanCodeActivity.class);

        intent.putExtra(Globals.TYPE_OF_SCANNER, Globals.REQUEST_CODE_VIN_SCANNER);

        Log.d("tag11", intent.toString() + "  " + Globals.REQUEST_CODE_VIN_SCANNER);

        startActivityForResult(intent, Globals.REQUEST_CODE_VIN_SCANNER);

    }

    public void scanQR() {

        Log.d("tag", "scanQR()");

        Intent intent = new Intent(this, ZbarScanCodeActivity.class);

        intent.putExtra(Globals.TYPE_OF_SCANNER, Globals.REQUEST_CODE_QR_SCANNER);

        startActivityForResult(intent, Globals.REQUEST_CODE_QR_SCANNER);

    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("tag", "onActivityResult");

        Log.d("tag", "requestCode ­ " + requestCode);

        Log.d("tag", "resultCode ­ " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == Globals.REQUEST_CODE_VIN_SCANNER) {



                String tempBar = data.getStringExtra(Globals.KEY_SCAN_RESULT);

                if (tempBar != null) {

                    if (tempBar.length() > 17) {

                        tempBar = tempBar.substring(tempBar.length() - 17, tempBar.length());

                    }

                    result.setText(tempBar);

                }

            }

        } else {

            Log.d("tag", "resultCode != Activity.RESULT_OK");

        }

        if (requestCode == Globals.REQUEST_CODE_QR_SCANNER) {

            String tempBar = data.getStringExtra(Globals.KEY_SCAN_RESULT);

            if (tempBar != null) {

                if (tempBar.length() > 17) {

                    tempBar = tempBar.substring(tempBar.length() - 17, tempBar.length());

                }

                Log.d("tag", "onActivityResult ­ editDealer.setText(tempBar);");

                result.setText(tempBar);

            }

        } else {

        Log.d("tag", "resultCode != Activity.RESULT_OK");

    }

    }
}
