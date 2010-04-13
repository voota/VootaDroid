/*
 * This file is part of the Voota package.
 * (c) 2010 Tatyana Ulyanova <levkatata.voota@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * This file contains implementation of EntitiesListView class. This Activity is
 * used to display list of politicians or list of parties, change order of their
 * displaying. Activity shows information per page, size of page is defined by 
 * Voota Api and now it's 20 entities. New portion of information is added when 
 * user gets last entity.
 *
 * @package    Voota
 * @subpackage Droid
 * @author     Tatyana Ulyanova
 * @version    1.0
 */

package org.voota.droid;

import java.util.ArrayList;

import org.voota.api.EntityInfo;
import org.voota.api.VootaApiException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class EntitiesListView extends ListActivity
{
    private final int MENU_SORT_POSITIVE = 1;
    private final int MENU_SORT_NEGATIVE = 2;
    private final int m_nPageLimit = 100;
    private ArrayList<EntityInfo> m_listEntitiesInfo = new ArrayList<EntityInfo>();
    private EntityInfoAdapter m_adapter;
    private boolean m_bIsSortedPositive = true;
    private boolean m_bIsPolicies = true;
    private String m_strActivityTitle = "";
    private int m_nCurrentPage = 1;
    private int m_nLastTotalItems = -1;
    
    private TextView m_tvEmpty = null;
    private ListView m_lvEntities = null;
    
    private ProgressDialog m_progressDialog = null;
    private Handler m_handler = new Handler();
    private VootaApiException m_throwThread = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_view);
        
        m_tvEmpty = ((TextView)this.findViewById(R.id.label_empty));
        m_lvEntities = getListView();
        m_lvEntities.setEmptyView(m_tvEmpty);
        m_lvEntities.setOnItemClickListener(OnEntityClick);
        m_lvEntities.setOnItemSelectedListener(OnSelectItem);
        m_lvEntities.setOnScrollListener(OnScrollView);
        
        m_adapter = new EntityInfoAdapter(EntitiesListView.this, 
                android.R.layout.simple_list_item_1, m_listEntitiesInfo);
        m_lvEntities.setAdapter(m_adapter);
        
        //forming title of activity
        m_bIsPolicies = getIntent().getExtras()
            .getBoolean(VootaDroidConstants.BUNDLEKEY_ISPOLITIC);
        if (!m_bIsPolicies)
        {
            m_strActivityTitle = getString(R.string.actname_party_list);
        }
        else
        {
            m_strActivityTitle = getString(R.string.actname_policies_list);
        }
        
        fillInitialInfo();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        this.setRequestedOrientation(newConfig.orientation);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_SORT_POSITIVE, Menu.NONE, R.string.menuitem_sort_positive);
        menu.add(0, MENU_SORT_NEGATIVE, Menu.NONE, R.string.menuitem_sort_negative);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.findItem(MENU_SORT_POSITIVE).setEnabled(!m_bIsSortedPositive);
        menu.findItem(MENU_SORT_NEGATIVE).setEnabled(m_bIsSortedPositive);
        return true;
    }

    // Handles item selections 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) 
        {
        case MENU_SORT_POSITIVE:
        {
            m_bIsSortedPositive = true;
            break;
        }
        case MENU_SORT_NEGATIVE:
        {
            m_bIsSortedPositive = false;
            break;
        }
        }

        m_nCurrentPage = 1;
        m_nLastTotalItems = -1;
        
        m_progressDialog = ProgressDialog.show(EntitiesListView.this, 
                getText(R.string.pdlg_title), 
                getText(R.string.pdlg_msg_getting_info), true);
        
        GetEntities getOtherOrder = new GetEntities(true);
        Thread threadGetInfo = new Thread(getOtherOrder);
        threadGetInfo.start();
        return true;
    }
  
    private AdapterView.OnItemClickListener OnEntityClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,  long id) {
            
            Intent iEntityInfo = new Intent(EntitiesListView.this, EntityViewActivity.class);
            iEntityInfo.putExtra(VootaDroidConstants.BUNDLEKEY_ENTITYINFO, 
                    m_listEntitiesInfo.get(position));
            startActivity(iEntityInfo);
        }
    };
    
    private AdapterView.OnItemSelectedListener OnSelectItem = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
            if(position + 1 == m_listEntitiesInfo.size())
            {
                fillWithNextPage(position + 1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };
    
    private AbsListView.OnScrollListener OnScrollView = new AbsListView.OnScrollListener() {
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
        
        @Override
        public void onScroll(AbsListView view, int first, int visible, int total) {
            
            // detect if last item is visible
            if (visible < total && (first + visible == total)) 
            {
                // only process first event
                if (m_nLastTotalItems != total) 
                {
                    m_nLastTotalItems = total;
                    fillWithNextPage(total);
                }
            }
        }
    }; 
    
    private class GetEntities implements Runnable {
        
        private boolean m_bIsOrderChange;
        
        public GetEntities(boolean bIsOrderChange) 
        {
            m_bIsOrderChange = bIsOrderChange;
        }
        
        @Override
        public void run() {
            ArrayList<EntityInfo> pageRes = new ArrayList<EntityInfo>();
            try
            {
                ArrayList<EntityInfo> fullRes;
                
                if (m_bIsPolicies)
                {
                    pageRes = VootaDroid.m_vootaApi.getListOfPoliciesByPage(
                            m_bIsSortedPositive, m_nCurrentPage);
                }
                else
                {
                    pageRes = VootaDroid.m_vootaApi.getListOfPartyByPage(
                            m_bIsSortedPositive, m_nCurrentPage);
                }
                
                if (pageRes.size() != 0)
                {
                    if (!m_bIsOrderChange)
                    {
                        fullRes = new ArrayList<EntityInfo>(m_listEntitiesInfo);
                    }
                    else
                    {
                       fullRes = new ArrayList<EntityInfo>(); 
                    }
                    m_listEntitiesInfo.clear();
                    fullRes.addAll(pageRes);
                    m_listEntitiesInfo = fullRes;
                }
            }
            catch (VootaApiException e)
            {
                m_throwThread = e;
            }
            finally
            {
                DoEnd addPage = new DoEnd(pageRes.size(), m_bIsOrderChange);
                m_handler.post(addPage);
            }
        }
    };
    
    private class DoEnd implements Runnable { 
    
        private int m_nPageSize;
        private boolean m_bIsNeedToClean;
        
        public DoEnd(int nPageSize, boolean bIsNeedToClean)
        {
            m_nPageSize = nPageSize;
            m_bIsNeedToClean = bIsNeedToClean;
        }
        
        @Override
        public void run() {
            setTitle(getActTitle());
            
            if (m_throwThread != null)
            {
                m_progressDialog.dismiss();
                
                new AlertDialog.Builder(EntitiesListView.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(VootaDroidConstants.getErrorMessage(
                    m_throwThread.getErrorCode(), EntitiesListView.this))
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
                if (m_bIsNeedToClean)
                {
                    m_adapter.clear();
                }
                if (m_nPageSize != 0)
                {
                    int size = m_listEntitiesInfo.size();
                    // check for page limitation
                    if (size > m_nPageLimit)
                    {
                        for(int i = 0; i < size - m_nPageLimit; i++)
                        {
                            //remove extra items from adapter and list
                            m_adapter.remove(m_listEntitiesInfo.get(i));
                            m_listEntitiesInfo.remove(i);
                        }
                        // rollback last total items for get next page, must be
                        // not equal to 100
                        m_nLastTotalItems = 80;
                        size = m_listEntitiesInfo.size();
                    }
                    
                    for (int i = size - m_nPageSize; i < size; i++)
                    {
                        m_adapter.add(m_listEntitiesInfo.get(i));
                    }
                    
                    m_adapter.notifyDataSetChanged();
                    m_lvEntities.setSelection(size - m_nPageSize);
                }
                m_progressDialog.dismiss();
            }
        }
    };
    
    private void fillInitialInfo()
    {
        m_progressDialog = ProgressDialog.show(EntitiesListView.this, 
                getText(R.string.pdlg_title), 
                getText(R.string.pdlg_msg_getting_info), true);
        
        GetEntities getInitialInfo = new GetEntities(false); 
        Thread threadGetInfo = new Thread(getInitialInfo);
        threadGetInfo.start();
    }

    private void fillWithNextPage(int nTotal)
    {
            m_nCurrentPage++;
        
            m_progressDialog = ProgressDialog.show(EntitiesListView.this, 
                    getString(R.string.pdlg_title), 
                    getString(R.string.pdlg_msg_getting_info), true);
            
            GetEntities getNewPage = new GetEntities(false);
            Thread t = new Thread(getNewPage);
            t.start();

    }
    
    private String getActTitle()
    {
        String strRes = m_strActivityTitle + " ";
        
        if (m_bIsSortedPositive)
        {
            strRes += getString(R.string.menuitem_sort_positive);
        }
        else
        {
            strRes += getString(R.string.menuitem_sort_negative);
        }
        return strRes;
    }
}