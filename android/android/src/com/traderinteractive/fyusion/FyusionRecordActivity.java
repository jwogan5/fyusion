package com.traderinteractive.fyusion;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import org.appcelerator.titanium.TiApplication;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import org.appcelerator.kroll.KrollDict;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.FileOutputStream;
import android.app.Application;
import android.content.Context;

import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.ext.carmodeflow.CarSession;
import com.fyusion.sdk.ext.carmodeflow.CarSessionActivity;
import 	com.fyusion.sdk.common.ext.util.FyuseUtils;

public class FyusionRecordActivity extends AppCompatActivity {

   private static final String LCAT = "AfyusionModule";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LCAT, "Called Create in Car Session");

        if(savedInstanceState == null){
          Intent i = getIntent();
          Bundle extras = i.getExtras();
          String fyuseId = extras.getString("fyuseId");

          if (fyuseId.equals("new")) {
              CarSession.init(this).asNormalCaptureFlow().withTaggingFlow(true).withShowOnWeb(false).withStaticImageFlow(false).withPublishing(false).withRecordingOnly(false).startForResult(123);
          } else {
              Log.e(LCAT, "Trying to load a saved session: " + fyuseId);
              CarSession.init(this).asNormalCaptureFlow().withMasterFyuse(fyuseId).withTaggingFlow(true).withShowOnWeb(false).withStaticImageFlow(false).withPublishing(false).withRecordingOnly(false).startForResult(123);
          }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(LCAT, "got result back from Car Session");
        super.onActivityResult(requestCode, resultCode, data);
        String repMessage = "Cancelled Session";

        KrollDict dict = new KrollDict();
    		if (requestCode == 123) {
    			dict.put("requestCode", requestCode);
    			dict.put("resultCode", resultCode);

    				if (resultCode == Activity.RESULT_OK) {
                repMessage = "Saved Session";
    						if (data != null) {
    								Log.e(LCAT, "Result OK with data");
    								dict.put("url", data.getStringExtra(CarSessionActivity.RESULT_EXTRA_URL));
    								dict.put("session", data.getStringExtra(CarSessionActivity.RESULT_EXTRA_SESSION_PATH));
    								dict.put("updated", data.getBooleanExtra(CarSessionActivity.RESULT_EXTRA_SESSION_UPDATED, false));
    								dict.put("tags", data.getIntExtra(CarSessionActivity.RESULT_EXTRA_TAGS, 0));
    								dict.put("images", data.getIntExtra(CarSessionActivity.RESULT_EXTRA_STATIC_IMAGES, 0));

                    String session = data.getStringExtra(CarSessionActivity.RESULT_EXTRA_SESSION_PATH);
                    String sessionThumb = data.getStringExtra(CarSessionActivity.RESULT_EXTRA_SESSION_PATH).toString();

                    // Get the id from the fyuse url
                    String[] imPath = sessionThumb.split("crs_");
                    String[] imPath1 = imPath[1].split("/");
                    sessionThumb = imPath1[0] + ".jpg";
                    Log.e(LCAT, "Saving the Thumbnail to path: " + sessionThumb);
                    Log.e(LCAT, "Saving the Thumbnail session: " + session);
                    File file = new File(session);
                    Bitmap bm = FyuseUtils.getThumbnail(file);
                    Log.e(LCAT, "The bitmap is of size: " + bm.getAllocationByteCount());

                    Context context = TiApplication.getInstance().getApplicationContext();
                    String FilePathToThumb = context.getFilesDir().toString().replace("files", "app_appdata");
                    File theThumb = new File(FilePathToThumb, sessionThumb);
                    Log.e(LCAT, "The path to the saved thumb is finally going to be: " + FilePathToThumb + "/" + sessionThumb);

                    if (!theThumb.exists()) {
                      try {
                            FileOutputStream out = new FileOutputStream(theThumb);
                            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                            dict.put("thumbPath", sessionThumb);
                            Log.e(LCAT, "Thumb Save Successful");
                        } catch (Exception e) {
                            Log.e(LCAT, "Thumb Save Failed");
                            dict.put("thumbPath", "Missing Thumb");
                        }
                    } else {
                      Log.e(LCAT, "The thumb file already exists");
                    }
    						} else {
    								Log.e(LCAT, "Result OK but no data");
    						}
    				} else {
    						if (resultCode == CarSessionActivity.RESULT_ERROR) {
    							dict.put("errorCode", "There was an error. Check LOG.");
    							Log.e(LCAT, "There was an error. The 360 might not exist.");
    						} else if (resultCode == CarSessionActivity.RESULT_CAMERA_UNSUPPORTED) {
    							dict.put("errorCode", "Camera Not Supported.");
    							Log.e(LCAT, "Camera Not Supported.");
    						} else if (resultCode == CarSessionActivity.RESULT_PHONE_UNSUPPORTED) {
    							dict.put("errorCode", "Phone Not Supported.");
    							Log.e(LCAT, "Phone Not Supported.");
    						} else {
    							dict.put("sessionTags", "Result: " + resultCode);
    							Log.e(LCAT, "Result: " + resultCode);
    						}
    				}
    		}

        dict.put("message", repMessage);
        AfyusionModule.getFyusionModule().fireEvent("response", dict);
        setResult(Activity.RESULT_OK);
        finish();
        return;
    }
}
