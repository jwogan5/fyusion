/**
 * Android Fyusion Module
 * Copyright TraderInteractive.com
 */
package com.traderinteractive.fyusion;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.TiConfig;
import android.app.Application;
import android.app.Activity;
import android.content.Intent;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import android.util.Log;
import java.io.File;
import 	com.fyusion.sdk.common.ext.util.FyuseUtils;

import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.share.exception.FyuseShareException;
import com.fyusion.sdk.ext.carmodeflow.CarSession;
import com.fyusion.sdk.ext.carmodeflow.share.TagSessionShare;
import com.fyusion.sdk.ext.carmodeflow.CarSessionActivity;

@Kroll.module(name="Afyusion", id="com.traderinteractive.fyusion")
public class AfyusionModule extends KrollModule implements TiActivityResultHandler
{
	// Standard Debugging variables
	private static final String LCAT = "AfyusionModule";
	private static final boolean DBG = TiConfig.LOGD;
	private static final String fyusionModuleVersion = "0.0.1";
	private String fyuseId;
	private Integer REQCODE;
	private static AfyusionModule module;

	public AfyusionModule()
	{
		super();
		module = this;
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.e(LCAT, "Calling Fyuse Init in Creation of Module");
		FyuseSDK.init(app.getInstance().getApplicationContext(), "vgjN_pN5Twoz8EKVe69yOJ", "4oFb5XT3X2gr27NU7On5sILcluG3gZrf");
	}

	// Methods
	@Kroll.method
	public String getVersion()
	{
		return fyusionModuleVersion;
	}

	public static AfyusionModule getFyusionModule()
	{
		return module;
	}

	@Kroll.method
	public void startSession(KrollDict options)
	{
		fyuseId = (String) options.get("id");
		Activity activity = TiApplication.getAppRootOrCurrentActivity();
		TiActivitySupport support = (TiActivitySupport) activity;
		Intent fyusionIntent = new Intent();
		fyusionIntent.setClass(activity, FyusionRecordActivity.class);
		fyusionIntent.putExtra("fyuseId", fyuseId);
		support.launchActivityForResult(fyusionIntent, 123, this);
	}

	@Kroll.method
	public void uploadSessionWithId(KrollDict options)
	{
		fyuseId = (String) options.get("id");
		Log.e(LCAT, "Starting to Upload: " + fyuseId);
		File file = new File(fyuseId);
		try {
			TagSessionShare tagSessionShare = new TagSessionShare(file).withUploadListener(new TagSessionShare.UploadListener() {
	        @Override
	        public void onFailure(String reason) {
							KrollDict dict = new KrollDict();
							dict.put("message", "Upload Session Failed");
			        AfyusionModule.getFyusionModule().fireEvent("response", dict);
	        }

	        @Override
	        public void onProgress(final int progress) {
								if (progress == 60 || progress == 70 || progress == 80 || progress == 90 || progress == 100)
								{
										// progress during upload
										 KrollDict dict = new KrollDict();
										 dict.put("message", "Upload Progress");
										 dict.put("progress", progress);
										 AfyusionModule.getFyusionModule().fireEvent("response", dict);
								}
	        }

	        @Override
	        public void onSuccess(String fyuseId) {
	            // fyuseId referencing a fyuse if available, if no fyuse is available this equals the `sessionId`
							KrollDict dict = new KrollDict();
							dict.put("message", "Upload No Session Success");
							dict.put("fyuseId", fyuseId);
			        AfyusionModule.getFyusionModule().fireEvent("response", dict);
	        }

	        @Override
	        public void onSessionSuccess(String sessionId) {
	            // sessionId referencing the uploaded car tagging session
							KrollDict dict = new KrollDict();
							dict.put("message", "Upload Session Success");
							dict.put("remoteId", sessionId);
			        AfyusionModule.getFyusionModule().fireEvent("response", dict);
	        }
	    }).startUpload();
		} catch (FyuseShareException e) {
			KrollDict dict = new KrollDict();
			dict.put("message", "Upload Session Failed");
			AfyusionModule.getFyusionModule().fireEvent("response", dict);
		}
	}

	@Kroll.method
	public void viewFyuseWithId(KrollDict options)
	{
		String fyuseLocation = (String) options.get("location");
		fyuseId = (String) options.get("id");
		Activity activity = TiApplication.getAppRootOrCurrentActivity();
		TiActivitySupport support = (TiActivitySupport) activity;
		Intent fyusionIntent = new Intent();
		fyusionIntent.setClass(activity, FyusionViewActivity.class);
		fyusionIntent.putExtra("fyuseId", fyuseId);
		support.launchActivityForResult(fyusionIntent, 123, this);
	}

	@Kroll.method
	public void deleteLocalSessionWithId(KrollDict options)
	{
		fyuseId = (String) options.get("id");
		Log.e(LCAT, "Going to Delete: " + fyuseId);
		File file = new File(fyuseId);

		FyuseUtils.delete(file);
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e)
	{
		Log.i(LCAT, "onError Called from activity result handler some how");
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data)
	{
		Log.i(LCAT, "onResult Called from activity result handler some how");
	}

}
