/*
 * This file is part of the Voota package.
 * (c) 2010 Tatyana Ulyanova <levkatata.voota@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * This file contains implementation of EntityInfoAdapter class. This is a custom
 * list adapter used for displaying custom view of list row. Every row shows
 * information about politician or party: small image, name, positive votes
 * and negative votes. This adapter is used for all entities list in
 * SearchResultsActivity and EntitiesListView activities.
 *
 * @package    Voota
 * @subpackage Droid
 * @author     Tatyana Ulyanova
 * @version    1.0
 */

package org.voota.droid;

import java.util.ArrayList;

import org.voota.api.EntityInfo;
import org.voota.droid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntityInfoAdapter extends ArrayAdapter<EntityInfo> 
{
    private ArrayList<EntityInfo> m_entityItems;
    protected EntityInfo m_oneEntity = null;
    protected static String m_strLabelVotes = null;
    private byte[] m_byImage;
    
    private Bitmap m_btmpImage = null;

    public EntityInfoAdapter(Context context, int textViewResourceId, 
            ArrayList<EntityInfo> items) 
    {
            super(context, textViewResourceId, items);
            this.m_entityItems = items;
            m_strLabelVotes = context.getString(R.string.entityrow_votes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
            View v = convertView;
            if (v == null) 
            {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.entity_row, null);
            }
             
            m_oneEntity = m_entityItems.get(position);
            if (m_oneEntity != null)
            {
            	ImageView imgEntity = (ImageView) v.findViewById(R.id.image_row_entity);
	            TextView tvName = (TextView) v.findViewById(R.id.label_rowentity_name);
	            TextView tvVotes = (TextView) v.findViewById(R.id.label_rowentity_votes);
	                
	            m_byImage = m_oneEntity.getBytesImageS();
	            if (m_byImage != null)
	            {
	                m_btmpImage = BitmapFactory.decodeByteArray(m_byImage, 0, m_byImage.length);
	                if (m_btmpImage != null)
	                {
	                    imgEntity.setImageBitmap(m_btmpImage);
	                }
	                else
	                {
	                    imgEntity.setImageResource(R.drawable.party);
	                }
	            }
	            else
	            {
	                imgEntity.setImageResource(R.drawable.party);
	            }

	            tvName.setText(m_oneEntity.getName());
	            tvVotes.setText(String.format(m_strLabelVotes, m_oneEntity.getPositiveVotes(),
	            		m_oneEntity.getNegativeVotes()));
            }
            return v;
    }
}

