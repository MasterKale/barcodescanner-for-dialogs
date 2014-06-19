package com.barcodescannerfordialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barcodescannerfordialogs.helpers.CameraFace;


public class ActivityStartScreen extends Activity implements DialogScanner.OnQRCodeScanListener
{
	static final String TAG = Activity.class.getSimpleName();

	TextView tvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);
		tvResults = (TextView) findViewById(R.id.qr_code_scan_results);
    }

	public void launchScanBack(View view)
	{
		DialogScanner dialog = DialogScanner.newInstance(CameraFace.BACK);
		dialog.show(getFragmentManager(), "dialogScanner");
	}

	public void launchScanFront(View view)
	{
		DialogScanner dialog = DialogScanner.newInstance(CameraFace.FRONT);
		dialog.show(getFragmentManager(), "dialogScanner");
	}

	@Override
	public void onQRCodeScan(String contents)
	{
		if(tvResults != null)
		{
			tvResults.setText(contents);
		}
	}
}
