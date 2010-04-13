/*
 * This file is part of the Voota package.
 * (c) 2010 Tatyana Ulyanova <levkatata.voota@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * This file contains implementation of EntityTopAdapter class. This is a custom
 * list adapter used for displaying custom view of list row. Every row shows
 * information about politician or party: small image, name, recent positive votes
 * and recent negative votes. This adapter is used for top entities list in
 * VootaDroid activity.
 *
 * @package    Voota
 * @subpackage Droid
 * @author     Tatyana Ulyanova
 * @version    1.0
 */

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