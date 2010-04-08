package org.voota.droid;

import org.voota.api.EntityInfo;
import org.voota.api.VootaApi;
import org.voota.api.VootaApiException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class VootaDroid extends Activity {
	public static String m_strAccessToken;
	public static String m_strTokenSecret;
	public static VootaApi m_vootaApi;

	private static final String CONSUMER_KEY;
    private static final String CONSUMER_SECRET;
    private static final String CALLBACK_URL;
	private static final String ACCESSTOKEN_PARAM;
	
	protected EditText m_etSearch = null;
	protected ImageButton m_btnSearch = null;
	protected ListView m_lvVotedEntities = null;
	protected Button m_btnPoliticRank = null;
	protected Button m_btnPartyRank = null;
	private TextView m_tvEmptyView = null;
	
	protected ProgressDialog m_progressDialog = null;
	protected Handler m_handler = new Handler();
	
	protected ArrayList<EntityInfo> m_listEntitiesInfo = new ArrayList<EntityInfo>();
	protected EntityInfoAdapter m_adapterView = null; 
	protected Throwable m_throwThread = null;
	
	static 
	{
        CONSUMER_KEY = "5d26c3ba75a227db79a29234e2c5bc1404bab9e9e";
        CONSUMER_SECRET = "f0f87c0729e61eb9373be52a10426c9a";
        CALLBACK_URL = "voota:///";
        ACCESSTOKEN_PARAM = "oauth_token";
        
        m_strAccessToken = "";
        m_strTokenSecret = "";
        
	    m_vootaApi = new VootaApi(CONSUMER_KEY, CONSUMER_SECRET, CALLBACK_URL);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	m_etSearch = ((EditText)this.findViewById(R.id.edit_text_search));
    	m_btnSearch = ((ImageButton)this.findViewById(R.id.btn_search));
    	m_lvVotedEntities = ((ListView)this.findViewById(R.id.list_top6));
    	m_btnPoliticRank = ((Button)this.findViewById(R.id.btn_politician_ranking));
    	m_btnPartyRank = ((Button)this.findViewById(R.id.btn_party_ranking));
    	m_tvEmptyView = ((TextView)this.findViewById(R.id.label_empty_top6));
    	
    	m_btnSearch.setOnClickListener(OnClickSearch);
    	m_lvVotedEntities.setOnItemClickListener(OnClickEntity);
    	m_btnPoliticRank.setOnClickListener(OnClickPoliticRank);
    	m_btnPartyRank.setOnClickListener(OnClickPartyRank);
    	m_btnSearch.requestFocus();
    	
    	m_adapterView = new EntityInfoAdapter(VootaDroid.this, 
    	        R.layout.entity_row, m_listEntitiesInfo);
    	m_lvVotedEntities.setAdapter(m_adapterView);
    	m_lvVotedEntities.setEmptyView(m_tvEmptyView);
    	
    	getAccessToken();
    }
    
    @Override
    public void onNewIntent(Intent i)
    {
        super.onNewIntent(i);
        
        Uri uri = i.getData();
        if(uri != null) 
        {
            try
            {
                //loadVootaApi();
                
                String request_token = uri.getQueryParameter(ACCESSTOKEN_PARAM);
                m_vootaApi.convertToAccessToken(request_token);
                m_strAccessToken = m_vootaApi.getAccessToken();
                m_strTokenSecret = m_vootaApi.getTokenSecret();
                saveAccessToken();
                i.setData(null);
                
                fillInitialInfo();
            }
            catch (VootaApiException e)
            {
                new AlertDialog.Builder(VootaDroid.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(e.getMessage())
                .setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        fillInitialInfo();
                    }
                }).show();
            }
        }
    }
    
    /*public void onResume() {
        super.onResume();
        
        Intent iThis = getIntent();
	    Uri uri = iThis.getData();
        if(uri != null) 
        {
            try
            {
                String request_token = uri.getQueryParameter(ACCESSTOKEN_PARAM);
                m_vootaApi.convertToAccessToken(request_token);
                m_strAccessToken = m_vootaApi.getAccessToken();
                m_strTokenSecret = m_vootaApi.getTokenSecret();
                saveAccessToken();
                iThis.setData(null);
            }
            catch (VootaApiException e)
            {
                new AlertDialog.Builder(VootaDroid.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(e.getMessage())
                .setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
            }
            fillInitialInfo();
        }
        else
        {
            String strAccessToken = iThis.getStringExtra(VootaDroidConstants.BUNDLEKEY_ACCESSTOKEN);
            if (strAccessToken != null)
            {
                m_strAccessToken = strAccessToken;
            }
            
            String strTokenSecret = iThis.getStringExtra(VootaDroidConstants.BUNDLEKEY_TOKENSECRET);
            if (strTokenSecret != null)
            {
                m_strTokenSecret = strTokenSecret;
            }
        }
    }*/
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        this.setRequestedOrientation(newConfig.orientation);
    }
    
    private View.OnClickListener OnClickSearch = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            String strSearch = m_etSearch.getText().toString().trim();
            if (strSearch.length() != 0)
            {
                Intent iSearchResults = new Intent(VootaDroid.this, SearchResultsActivity.class);
                Bundle b = new Bundle();
                b.putString(VootaDroidConstants.BUNDLEKEY_SEARCHSTRING, strSearch);
                iSearchResults.putExtras(b);
                
                VootaDroid.this.startActivity(iSearchResults);
            }
        }
    };

    private View.OnClickListener OnClickPoliticRank = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Intent iPoliciesList = new Intent(VootaDroid.this, EntitiesListView.class);
            Bundle b = new Bundle();
            b.putBoolean(VootaDroidConstants.BUNDLEKEY_ISPOLITIC, true);
            iPoliciesList.putExtras(b);
            
            VootaDroid.this.startActivity(iPoliciesList);
        }
    };
    
    private View.OnClickListener OnClickPartyRank = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Intent iPoliciesList = new Intent(VootaDroid.this, EntitiesListView.class);
            Bundle b = new Bundle();
            b.putBoolean(VootaDroidConstants.BUNDLEKEY_ISPOLITIC, false);
            iPoliciesList.putExtras(b);
            
            VootaDroid.this.startActivity(iPoliciesList);
        }
    };
    
    /*private View.OnClickListener OnClickExit = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            VootaDroid.this.finish();
        }
    };*/
    
    private AdapterView.OnItemClickListener OnClickEntity = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView arg0, View arg1, int position, long id) {
            Intent iEntityInfo = new Intent(VootaDroid.this, EntityViewActivity.class);
            iEntityInfo.putExtra(VootaDroidConstants.BUNDLEKEY_ENTITYINFO, 
                    m_listEntitiesInfo.get((int)id));
            startActivity(iEntityInfo);
        }
    };
    
    private Runnable getInitialInfo = new Runnable() {
        
        @Override
        public void run() {
            try
            {
                m_listEntitiesInfo = m_vootaApi.getTopSix();
            }
            catch (VootaApiException e)
            {
                m_throwThread = e;
            }
            finally
            {
                m_handler.post(doEndInitial);
            }
        }
    };
 
    protected Runnable doEndInitial = new Runnable() {
        
        @Override
        public void run() {

            if (m_throwThread != null)
            {
                m_progressDialog.dismiss();

                new AlertDialog.Builder(VootaDroid.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(m_throwThread.getMessage())
                .setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                m_throwThread = null;
            }
            else
            {
                m_adapterView.notifyDataSetChanged();
                m_adapterView.clear();
                for (EntityInfo oneEntity : m_listEntitiesInfo)
                {
                    m_adapterView.add(oneEntity);
                }
                m_adapterView.notifyDataSetChanged();
                m_progressDialog.dismiss();
            }
        }
    };

    
    protected void fillInitialInfo()
    {
        m_progressDialog = ProgressDialog.show(VootaDroid.this, 
                getString(R.string.pdlg_title), 
                getString(R.string.pdlg_msg_starting), true);
        
        Thread startingThread = new Thread(getInitialInfo);
        startingThread.start();
    }
    
    private void getAccessToken()
    {
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

                    //persistVootaApi();
                    startActivity(iAuthorize);
                }
            }
            catch (VootaApiException e)
            {
                new AlertDialog.Builder(VootaDroid.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(e.getMessage())
                .setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        fillInitialInfo();
                    }
                }).show();
            }
        }
        else
        {
            m_strAccessToken = strAccessToken;
            m_strTokenSecret = settings.getString(VootaDroidConstants.PREFKEY_TOKENSECRET, "");
            fillInitialInfo();
        }
    }
    
    private void saveAccessToken()
    {
        SharedPreferences settings = getSharedPreferences(
                VootaDroidConstants.PREFERENCES_FILE, MODE_PRIVATE);
        Editor editSettings = settings.edit();
        editSettings.putString(VootaDroidConstants.PREFKEY_ACCESSTOKEN, m_strAccessToken);
        editSettings.putString(VootaDroidConstants.PREFKEY_TOKENSECRET, m_strTokenSecret);
        editSettings.commit();
    }
    
    /*protected void persistVootaApi()
    {
        try
        {
            FileOutputStream fout = this.openFileOutput("provider.dat", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(VootaDroid.m_vootaApi);
            oos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    protected void loadVootaApi()
    {
        try
        {
            FileInputStream fin = this.openFileInput("provider.dat");
            ObjectInputStream ois = new ObjectInputStream(fin);
            VootaDroid.m_vootaApi = (VootaApi) ois.readObject();
            ois.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }*/


}