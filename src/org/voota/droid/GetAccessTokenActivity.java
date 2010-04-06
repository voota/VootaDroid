package org.voota.droid;

import org.voota.api.VootaApiException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

public class GetAccessTokenActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
 
        SharedPreferences settings = getSharedPreferences(
                VootaDroidConstants.PREFERENCES_FILE, MODE_PRIVATE);
        String strDefVal = "defAccessToken";
        String strAccessToken = settings.getString(VootaDroidConstants.PREFKEY_ACCESSTOKEN,
                strDefVal);

        if (strAccessToken.equals(strDefVal))
        {
            try
            {
                String strAuth = VootaDroid.m_vootaApi.getAuthorizeUrl();
                if (strAuth != null)
                {
                    Intent iAuthorize = new Intent(Intent.ACTION_VIEW);
                    iAuthorize.setData(Uri.parse(strAuth));

                    startActivity(iAuthorize);
                    GetAccessTokenActivity.this.finish();
                }
            }
            catch (VootaApiException e)
            {
                new AlertDialog.Builder(GetAccessTokenActivity.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(e.getMessage())
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        Intent iVootaDroid = new Intent(GetAccessTokenActivity.this, VootaDroid.class);
                        startActivity(iVootaDroid);
                        GetAccessTokenActivity.this.finish();
                    }
                }).show();
            }
        }
        else
        {
            String strTokenSecret = settings.getString(VootaDroidConstants.PREFKEY_TOKENSECRET, "");
            
            Intent iVootaDroid = new Intent(GetAccessTokenActivity.this, VootaDroid.class);
            iVootaDroid.putExtra(VootaDroidConstants.BUNDLEKEY_ACCESSTOKEN, strAccessToken);
            iVootaDroid.putExtra(VootaDroidConstants.BUNDLEKEY_TOKENSECRET, strTokenSecret);
            startActivity(iVootaDroid);

            GetAccessTokenActivity.this.finish();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        this.setRequestedOrientation(newConfig.orientation);
    }

}