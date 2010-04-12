package org.voota.droid;

import org.voota.api.VootaApiException;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

public class SearchResultsActivity extends VootaDroid
{
    private String m_strSearchString = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        m_btnSearch.setOnClickListener(onClickSearch);
        m_btnPoliticRank.setVisibility(View.GONE);
        m_btnPartyRank.setVisibility(View.GONE);
        m_tvTopThisWeek.setVisibility(View.GONE);
        
        m_strSearchString = getIntent().getExtras().getString(VootaDroidConstants.BUNDLEKEY_SEARCHSTRING);
        m_etSearch.setText(m_strSearchString);
    }
    
    
    private View.OnClickListener onClickSearch = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            m_strSearchString = m_etSearch.getText().toString();
            if (m_strSearchString.length() != 0)
            {
                fillInitialInfo();
            }
        }
    };
    
    private Runnable getSearchResults = new Runnable() {
        
        @Override
        public void run() {
            try
            {
                m_listEntitiesInfo = VootaDroid.m_vootaApi.getSearchEntities(m_strSearchString);
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
    
    protected void fillInitialInfo()
    {
        m_bIsEntityFilled = true;
        m_progressDialog = ProgressDialog.show(SearchResultsActivity.this, 
                getText(R.string.pdlg_title), getText(R.string.pdlg_msg_getting_info), true);
        
        Thread threadGetSearch = new Thread(getSearchResults);
        threadGetSearch.start();
    }
}