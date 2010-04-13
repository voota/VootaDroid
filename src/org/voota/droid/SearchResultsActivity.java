/*
 * This file is part of the Voota package.
 * (c) 2010 Tatyana Ulyanova <levkatata.voota@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * This file contains implementation of SearchResultsActivity class. This Activity is
 * used to search politicians or parties by key word, show search results to user, 
 * change key word. 
 * This class inherits from VootaDroid, but excludes OAuth calls, top buttons and
 * redefines some methods.
 *
 * @package    Voota
 * @subpackage Droid
 * @author     Tatyana Ulyanova
 * @version    1.0
 */

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
    
    @Override
    protected void fillInitialInfo()
    {
        m_bIsEntityFilled = true;

        m_adapterView = new EntityInfoAdapter(SearchResultsActivity.this, 
                R.layout.entity_row, m_listEntitiesInfo);
        m_lvVotedEntities.setAdapter(m_adapterView);

        m_progressDialog = ProgressDialog.show(SearchResultsActivity.this, 
                getText(R.string.pdlg_title), getText(R.string.pdlg_msg_getting_info), true);
        
        Thread threadGetSearch = new Thread(getSearchResults);
        threadGetSearch.start();
    }
    
    @Override
    protected void getAccessToken()
    {
    }
}