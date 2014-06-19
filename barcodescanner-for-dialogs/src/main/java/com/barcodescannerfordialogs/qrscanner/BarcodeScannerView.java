package com.barcodescannerfordialogs.qrscanner;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.barcodescannerfordialogs.helpers.CameraFace;

import me.dm7.barcodescanner.core.CameraUtils;
import me.dm7.barcodescanner.core.DisplayUtils;

public class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback
{
	static final String TAG = BarcodeScannerView.class.getSimpleName();

	private Camera mCamera;
	private CameraPreview mPreview;
	private ViewFinderView mViewFinderView;
	private Rect mFramingRectInPreview;

	CameraFace mCameraFace;

	// Size of scanner window
	int mScannerWidth;
	int mScannerHeight;
	int mScannerPadding;

	public BarcodeScannerView(Context context, CameraFace cameraFacing, int width, int height, int padding)
	{
		super(context);

		mCameraFace = cameraFacing;
		mScannerWidth = width;
		mScannerHeight = height;
		mScannerPadding = padding;

		setupLayout();
	}

	public BarcodeScannerView(Context context, AttributeSet attributeSet, CameraFace cameraFacing, int width, int height, int padding)
	{
		super(context, attributeSet);

		mCameraFace = cameraFacing;
		mScannerWidth = width;
		mScannerHeight = height;
		mScannerPadding = padding;

		setupLayout();
	}

	@Override
	public void onPreviewFrame(byte[] bytes, Camera camera)
	{

	}

	public void setupLayout()
	{
		if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT && mScannerWidth > mScannerHeight)
		{
			int tmp = mScannerHeight;
			mScannerHeight = mScannerWidth;
			mScannerWidth = tmp;
		}

		mPreview = new CameraPreview(getContext(), mScannerWidth, mScannerHeight);
		mViewFinderView = new ViewFinderView(getContext(), mScannerWidth, mScannerHeight, mScannerPadding);
		addView(mPreview);
		addView(mViewFinderView);
	}

	public void startCamera()
	{
		mCamera = getCameraInstance();
		if(mCamera != null) {
			mViewFinderView.setupViewFinder();
			mPreview.setCamera(mCamera, this);
			mPreview.initCameraPreview();
		}
	}

	public void stopCamera()
	{
		if(mCamera != null) {
			mPreview.stopCameraPreview();
			mPreview.setCamera(null, null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Camera getCameraInstance()
	{
		int numCameras = Camera.getNumberOfCameras();
		Camera c = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

		// If we have more than one camera and the user has requested the front camera, try and find the front camera
		if(numCameras > 1 && mCameraFace == CameraFace.FRONT)
		{
			for(int i = 0; i < numCameras; i++)
			{
				Camera.getCameraInfo(i, cameraInfo);
				if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				{
					try
					{
						c = Camera.open(i); // attempt to get a Camera instance
					}
					catch (Exception e){
						// Camera is not available (in use or does not exist)
					}
				}
			}
		}
		else
		{
			c = Camera.open();
		}

		return c; // returns null if camera is unavailable
	}

	public synchronized Rect getFramingRectInPreview(int width, int height) {
		if (mFramingRectInPreview == null) {
			Rect framingRect = mViewFinderView.getFramingRect();
			if (framingRect == null) {
				return null;
			}
			Rect rect = new Rect(framingRect);
			Point screenResolution = DisplayUtils.getScreenResolution(getContext());
			Point cameraResolution = new Point(width, height);

			if (cameraResolution == null || screenResolution == null) {
				// Called early, before init even finished
				return null;
			}

			rect.left = rect.left * cameraResolution.x / screenResolution.x;
			rect.right = rect.right * cameraResolution.x / screenResolution.x;
			rect.top = rect.top * cameraResolution.y / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;

			mFramingRectInPreview = rect;
		}
		return mFramingRectInPreview;
	}

	public void setFlash(boolean flag) {
		if(CameraUtils.isFlashSupported(getContext()) && mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			if(flag) {
				if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
					return;
				}
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			} else {
				if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
					return;
				}
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			}
			mCamera.setParameters(parameters);
		}
	}

	public boolean getFlash() {
		if(CameraUtils.isFlashSupported(getContext()) && mCamera != null)
		{
			Camera.Parameters parameters = mCamera.getParameters();
			return parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
		}
		return false;
	}

	public void toggleFlash() {
		if(CameraUtils.isFlashSupported(getContext()) && mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			} else {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			}
			mCamera.setParameters(parameters);
		}
	}

	public void setAutoFocus(boolean state) {
		if(mPreview != null) {
			mPreview.setAutoFocus(state);
		}
	}
}
