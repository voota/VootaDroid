package org.voota.droid;

import java.util.ArrayList;

import org.voota.api.EntityInfo;
import org.voota.droid.EntityInfoAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EntityTopAdapter extends EntityInfoAdapter
{
    public EntityTopAdapter(Context context, int textViewResourceId, 
            ArrayList<EntityInfo> items)
    {
        super(context, textViewResourceId, items);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View v = super.getView(position, convertView, parent);
        if(m_oneEntity != null)
        {
            TextView tvVotes = (TextView) v.findViewById(R.id.label_rowentity_votes);
            tvVotes.setText(String.format(m_strLabelVotes, 
                m_oneEntity.getRecPositiveVotes(),
                m_oneEntity.getRecNegativeVotes()));
        }
        return v;
    }
}