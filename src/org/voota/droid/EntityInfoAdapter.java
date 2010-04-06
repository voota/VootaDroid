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
    private static String m_strLabelVotes = null;
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
             
            EntityInfo oneEntity = m_entityItems.get(position);
            if (oneEntity != null)
            {
            	ImageView imgEntity = (ImageView) v.findViewById(R.id.image_row_entity);
	            TextView tvName = (TextView) v.findViewById(R.id.label_rowentity_name);
	            TextView tvVotes = (TextView) v.findViewById(R.id.label_rowentity_votes);
	                
	            m_byImage = oneEntity.getBytesImageS();
	            if (m_byImage != null)
	            {
	                m_btmpImage = BitmapFactory.decodeByteArray(m_byImage, 0, m_byImage.length);
	                if (m_btmpImage != null)
	                {
	                    imgEntity.setImageBitmap(m_btmpImage);
	                }
	            }

	            tvName.setText(oneEntity.getName());
	            tvVotes.setText(String.format(m_strLabelVotes, oneEntity.getPositiveVotes(),
	            		oneEntity.getNegativeVotes()));
            }
            return v;
    }
}

