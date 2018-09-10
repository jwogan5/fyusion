package com.traderinteractive.fyusion;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.appcelerator.kroll.KrollDict;
import android.support.design.widget.Snackbar;
import com.traderinteractive.fyusion.R;
import com.fyusion.sdk.common.util.UidUtil;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.view.RemoteFyuseView;
import java.io.File;
import android.view.View;

import com.fyusion.sdk.common.FyuseSDK;

public class FyusionViewActivity extends AppCompatActivity {

   private static final String LCAT = "AfyusionModule";
   private RemoteFyuseView fyuseViewRemote;
   private String fyuseId;
   private View root;

   public void closeActivity(View v){
     finish();
     return;
   }

   /**
    * Listener used to get informed about the loading states of a remote fyuse.
    */
    RequestListener fyuseLoadRequestListener = new RequestListener() {
       @Override
       public boolean onLoadFailed(@Nullable FyuseException e, Object o) {
           loadFailed(R.string.m_remote_load_failed);
           return false;
       }

       @Override
       public boolean onResourceReady(Object o) {
           return false;
       }

       @Override
       public void onProgress(int i) {
          Log.e(LCAT, "Loading Fyuse For Display : " + i);
       }
   };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            fyuseId = extras.getString("fyuseId");

            Log.e(LCAT, "About to Display a Fyusion by id: " + fyuseId);

            setContentView(R.layout.activity_remote_fyuse);
            Log.e(LCAT, "Getting the View for remote ...");
            root = findViewById(R.id.root);
            fyuseViewRemote = findViewById(R.id.remoteFyuseView);
            fyuseViewRemote.setRotateWithGravity(false);
            fyuseViewRemote.setEnforceAspectRatio(false);
            fyuseViewRemote.setVisibility(View.VISIBLE);
            FyuseViewer.with(FyusionViewActivity.this)
                    .load(fyuseId)
                    .listener(fyuseLoadRequestListener)
                    .highRes(true)
                    .into(fyuseViewRemote);
        }
    }

    /**
     * Inform the user about a failed loading.
     *
     * @param message message displayed in the Snackbar
     */
     private void loadFailed(int message) {
        Log.e(LCAT, "Failed Remote Load : " + message);
        fyuseViewRemote.setVisibility(View.INVISIBLE);
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
     }
}
