package com.barcodescannerfordialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.barcodescannerfordialogs.helpers.CameraFace;
import com.barcodescannerfordialogs.qrscanner.ZBarScannerView;

import me.dm7.barcodescanner.core.DisplayUtils;

public class DialogScanner extends DialogFragment implements ZBarScannerView.ResultHandler
{
	static final String TAG = DialogScanner.class.getSimpleName();

	CameraFace cameraFace;

	ZBarScannerView mScannerView;

	// Maintain a 4:3 ratio
	int mWindowWidth = 800;
	int mWindowHeight = 600;
	int mViewFinderPadding = 50;

	static final String BUNDLE_CAMERA_FACE = "cameraFace";

	OnQRCodeScanListener scanListener;

	public interface OnQRCodeScanListener
	{
		public void onQRCodeScan(String contents);
	}

	public static DialogScanner newInstance(CameraFace camera)
	{
		DialogScanner dialog = new DialogScanner();
		Bundle args = new Bundle();
		args.putInt(BUNDLE_CAMERA_FACE, camera.ordinal());
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		try
		{
			scanListener = (OnQRCodeScanListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.getClass().getSimpleName() + " must implement OnQRCodeScanListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if(args != null)
		{
			cameraFace = CameraFace.values()[args.getInt(BUNDLE_CAMERA_FACE)];
		}
		else
		{
			cameraFace = CameraFace.BACK;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//View view = inflater.inflate(R.layout.dialog_scanner, container, false);

		mScannerView = new ZBarScannerView(getActivity(), cameraFace);
		mScannerView.setResultHandler(this);
		mScannerView.setAutoFocus(false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		return mScannerView;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(DisplayUtils.getScreenOrientation(getActivity()) == Configuration.ORIENTATION_PORTRAIT && mWindowWidth > mWindowHeight)
		{
			getDialog().getWindow().setLayout(mWindowHeight, mWindowWidth);
		}
		else
		{
			getDialog().getWindow().setLayout(mWindowWidth, mWindowHeight);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		mScannerView.stopCamera();
	}

	private void playSound(int notificationType)
	{
		Uri notification = RingtoneManager.getDefaultUri(notificationType);
		Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
		r.play();
	}

	@Override
	public void handleResult(me.dm7.barcodescanner.zbar.Result result)
	{
		if(result.getBarcodeFormat() == me.dm7.barcodescanner.zbar.BarcodeFormat.QRCODE)
		{
			playSound(RingtoneManager.TYPE_NOTIFICATION);
			scanListener.onQRCodeScan(result.getContents());
			getDialog().dismiss();
		}
	}
}
