barcodescanner-for-dialogs
==========================

A version of barcodescanner fine-tuned to work with DialogFragments

Origin
==========================

I needed to add a QR scanner to an app I'm working on, and as I searched for a suitable library I found [dm77's barcodescanner repo](https://github.com/dm77/barcodescanner). After testing it out, I liked what I saw but was dismayed by the fact that it only worked on full-screen Activities or Fragments. I spent the next couple of days tweaking the libary to support scanning from a DialogFragment - ideally I could pop up up a dialog window, scan a QR code, and then hand off the scanned information back to whatever Activity or Fragment the scan was initiated from.

Due to the extensive changes I made to the original barcodescanner library, I decided to make this its own repo.

Usage
==========================

As of right now you can display the scanner in a dialog of variable size, and can scan from either the front-facing or rear-facing cameras. A demo activity is included in the code, but right now it's only set up to scan QR codes. Check out _handleResult()_ in **DialogScanner.java** to specify the types of barcodes that you want results from.

This scanner setup has all the same capabilities as dm77/barcodescanner, but at the moment it only supports the ZBar scanner library. I may add in support for ZXing at a later time. Check out [the ZBar section of the barcodescanner README](https://github.com/dm77/barcodescanner/blob/master/README.md#zbar) for a briefing on its capabilities before proceeding.

First, add the following Maven repos your app's **build.gradle**:
```
compile 'me.dm7.barcodescanner:zbar:1.0'
```

To scan from a dialog, the **DialogFragment** needs to implement an interface to pass the results back to the activity:

```java
public interface OnQRCodeScanListener
{
	public void onQRCodeScan(String contents);
}
```

Implement this in your parent **Activity** or **Fragment**:
```java
@Override
public void onQRCodeScan(String contents)
{
	// Do something here with contents
}
```

Back in your **DialogFragment**, include a *handleResult(Result result)* method to handle scan results:
```java
@Override
public void handleResult(me.dm7.barcodescanner.zbar.Result result)
{
	if(result.getBarcodeFormat() == me.dm7.barcodescanner.zbar.BarcodeFormat.QRCODE)
	{
		scanListener.onQRCodeScan(result.getContents());
		getDialog().dismiss();
	}
}
```
In this example I only handle QR code results, but you can use any of the [barcode formats specified here](https://github.com/dm77/barcodescanner/blob/master/README.md#advanced-usage-1).

Once you've done that, set up the scanner in your **DialogFragment**'s *onCreateView()*:

```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
{
	mScannerView = new ZBarScannerView(getActivity(), cameraFace);
	mScannerView.setResultHandler(this);
	mScannerView.setAutoFocus(false);
    // Hide the titlebar so the scanner can take up the entire dialog
	getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

	return mScannerView;
}
```

You have several ways of creating your **ZBarScannerView**:
```java
public ZBarScannerView(Context context, CameraFace cameraFacing, int width, int height, int padding)
public ZBarScannerView(Context context, CameraFace cameraFacing)
public ZBarScannerView(Context context)
```

If you don't specify the dialog's **width**, **height**, and the scan preview's **padding**, they'll default to a scanner window 640 x 480 with a scanner padding of 70. Whatever **width** and **height** you choose, **they should be in a 4:3 aspect ratio due to limitations in the preview sizes offered by the onboard camera**.

If you don't specify a **cameraFacing**, the scanner will default to using the device's rear camera. You have the option of specifying *CameraFace.FRONT* or *CameraFace.BACK*.

Screenshots
==========================
![Front-facing camera](http://i.imgur.com/blHfvpL.png)
![Rear-facing camera](http://i.imgur.com/4ODl3JP.png)
![QR Scanner Demo](http://i.imgur.com/YCswtnF.png)
