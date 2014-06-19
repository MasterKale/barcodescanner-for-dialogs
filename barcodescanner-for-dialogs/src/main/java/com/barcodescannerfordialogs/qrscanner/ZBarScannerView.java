package com.barcodescannerfordialogs.qrscanner;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.barcodescannerfordialogs.helpers.CameraFace;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;

public class ZBarScannerView extends BarcodeScannerView
{
	static final int DEFAULT_SCANNER_WIDTH = 640;
	static final int DEFAULT_SCANNER_HEIGHT = 480;
	static final int DEFAULT_VIEWFINDER_PADDING = 70;

	private ImageScanner mScanner;
	private List<BarcodeFormat> mFormats;
	private ResultHandler mResultHandler;

	public ZBarScannerView(Context context, CameraFace cameraFacing, int width, int height, int padding)
	{
		super(context, cameraFacing, width, height, padding);
		setupScanner();
	}

	public ZBarScannerView(Context context, AttributeSet attributeSet, CameraFace cameraFacing, int width, int height, int padding)
	{
		super(context, attributeSet, cameraFacing, width, height, padding);
		setupScanner();
	}

	// Default cameraFacing to BACK if none is specified
	public ZBarScannerView(Context context, AttributeSet attributeSet)
	{
		this(context, attributeSet, CameraFace.BACK, DEFAULT_SCANNER_WIDTH, DEFAULT_SCANNER_HEIGHT, DEFAULT_VIEWFINDER_PADDING);
	}

	public ZBarScannerView(Context context)
	{
		this(context, CameraFace.BACK, DEFAULT_SCANNER_WIDTH, DEFAULT_SCANNER_HEIGHT, DEFAULT_VIEWFINDER_PADDING);
	}

	public ZBarScannerView(Context context, CameraFace facing)
	{
		this(context, facing, DEFAULT_SCANNER_WIDTH, DEFAULT_SCANNER_HEIGHT, DEFAULT_VIEWFINDER_PADDING);
	}

	public interface ResultHandler {
		public void handleResult(Result rawResult);
	}

	static {
		System.loadLibrary("iconv");
	}


	public void setFormats(List<BarcodeFormat> formats) {
		mFormats = formats;
		setupScanner();
	}

	public void setResultHandler(ResultHandler resultHandler) {
		mResultHandler = resultHandler;
	}

	public Collection<BarcodeFormat> getFormats() {
		if(mFormats == null) {
			return BarcodeFormat.ALL_FORMATS;
		}
		return mFormats;
	}

	public void setupScanner() {
		mScanner = new ImageScanner();
		mScanner.setConfig(0, Config.X_DENSITY, 3);
		mScanner.setConfig(0, Config.Y_DENSITY, 3);

		mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
		for(BarcodeFormat format : getFormats()) {
			mScanner.setConfig(format.getId(), Config.ENABLE, 1);
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		Camera.Size size = parameters.getPreviewSize();
		int width = size.width;
		int height = size.height;

		if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
			byte[] rotatedData = new byte[data.length];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					rotatedData[x * height + height - y - 1] = data[x + y * width];
			}
			int tmp = width;
			width = height;
			height = tmp;
			data = rotatedData;
		}

		Image barcode = new Image(width, height, "Y800");
		barcode.setData(data);

		int result = mScanner.scanImage(barcode);

		if (result != 0) {
			stopCamera();
			if(mResultHandler != null) {
				SymbolSet syms = mScanner.getResults();
				Result rawResult = new Result();
				for (Symbol sym : syms) {
					String symData = sym.getData();
					if (!TextUtils.isEmpty(symData)) {
						rawResult.setContents(symData);
						rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
						break;
					}
				}
				mResultHandler.handleResult(rawResult);
			}
		} else {
			camera.setOneShotPreviewCallback(this);
		}
	}
}
