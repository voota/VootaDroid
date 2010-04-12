package org.voota.droid;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.voota.api.EntityInfo;
import org.voota.api.ReviewInfo;
import org.voota.api.VootaApi;
import org.voota.api.VootaApiException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EntityViewActivity extends ListActivity
{
    private static String POSITIVE_VOTE = "";
    private static String NEGATIVE_VOTE = "";
    private final static int DLG_GET_REVIEW = 100;
    
    private ImageView m_imgEntity = null;
    private TextView m_tvEntityName = null;
    private TextView m_tvPositiveVotes = null;
    private TextView m_tvNegativeVotes = null;
    private ImageButton m_btnTumbUp = null;
    private ImageButton m_btnTumbDown = null;
    private RelativeLayout m_rlVootesRoot = null;
    private LinearLayout m_llVootes15Root = null;
    private ListView m_lvEntity = null;
    
    private Bitmap m_btmpEntity = null;
    private EntityInfo m_entityInfo = null;
    private ArrayList<ReviewInfo> m_listReviews = new ArrayList<ReviewInfo>();
    private ReviewAdapter m_adapter = null;
    private Handler m_handler = new Handler();
    private VootaApiException m_throwThread = null;
    private ProgressDialog m_pDlg = null;
    private ReviewInfo m_reviewInfo = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        POSITIVE_VOTE = getString(R.string.positive_review);
        NEGATIVE_VOTE = getString(R.string.negative_review);
        
        m_adapter = new ReviewAdapter(EntityViewActivity.this, 
                R.layout.review_row, m_listReviews);
        m_adapter.setNotifyOnChange(true);
        m_lvEntity = this.getListView();
        setListAdapter(m_adapter);
        
        getListView().setItemsCanFocus(true);
        fillInitialInfo();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        this.setRequestedOrientation(newConfig.orientation);
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        Dialog dialog;
        switch(id) 
        {
        case DLG_GET_REVIEW:
            AlertDialog.Builder builder;

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dlg_entry_review, 
                    (ViewGroup) findViewById(R.id.layout_root));
            final EditText review = (EditText) layout.findViewById(R.id.entry_review_text);
            
            builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            builder.setTitle(R.string.dlg_reviewtext_title);
            builder.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postReview(review.getText().toString());
                    review.setText("");
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dlg_cancel, OnClickCancel);
            
            dialog = builder.create();
            break;
        default:
            dialog = null;  
        }
        return dialog;
    }
    
    private View.OnClickListener OnClickUp = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			m_reviewInfo = new ReviewInfo(m_entityInfo.getID(), ReviewInfo.REVIEW_VALUE_POSITIVE, 
					m_entityInfo.getType(), "");

			showDialog(DLG_GET_REVIEW);
		}
	};

    private View.OnClickListener OnClickDown = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			m_reviewInfo = new ReviewInfo(m_entityInfo.getID(), ReviewInfo.REVIEW_VALUE_NEGATIVE, 
					m_entityInfo.getType(), "");
	
			showDialog(DLG_GET_REVIEW);
		}
	};
	
    private DialogInterface.OnClickListener OnClickCancel = new DialogInterface.OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };
    
    private void fillInitialInfo()
    {
        m_entityInfo = (EntityInfo)getIntent()
            .getSerializableExtra(VootaDroidConstants.BUNDLEKEY_ENTITYINFO);
        
        m_pDlg = ProgressDialog.show(EntityViewActivity.this, 
                getText(R.string.pdlg_title), 
                getText(R.string.pdlg_msg_getting_info), true);
        
        Thread initialThread = new Thread(new GetDataRunnable(false));
        initialThread.start();
    }

    private void postReview(String strText) 
    {
        if(m_reviewInfo != null)
        {
            m_reviewInfo.setReviewText(strText);
            m_pDlg = ProgressDialog.show(EntityViewActivity.this, getString(R.string.pdlg_title),
                    getString(R.string.pdlg_msg_posting_review), true);
    
            PostReviewRunnable r = new PostReviewRunnable(m_reviewInfo);
            Thread thread = new Thread(r);
            thread.start();
        }
    }
    
    private class GetDataRunnable implements Runnable {
        
        private boolean m_bIfOkReviewPost = false;
        
        public GetDataRunnable(boolean bIfOkReviewPost) 
        {
            m_bIfOkReviewPost = bIfOkReviewPost;
        }
        
        @Override
        public void run() {
            
            try
            {
                m_entityInfo = VootaDroid.m_vootaApi.getEntityInfo(m_entityInfo);
                if (m_btmpEntity == null)
                {
                    byte[] bImage = VootaApi.getUrlImageBytes(m_entityInfo.getUrlImage()); 
                    if (bImage != null)
                    {
                        m_btmpEntity = BitmapFactory.decodeByteArray(bImage, 0, bImage.length);
                    }
                }
                m_listReviews = VootaDroid.m_vootaApi.getReviews(m_entityInfo);
            }
            catch(VootaApiException e)
            {
                m_throwThread = e;
            }
            finally
            {
                m_handler.post(new EndProcRunnable(m_bIfOkReviewPost));
            }
        }
    }; 
    
    private class EndProcRunnable implements Runnable {
        
        private boolean m_bIfOkPostReview = false;
        
        public EndProcRunnable(boolean bIfOkPostReview)
        {
            m_bIfOkPostReview = bIfOkPostReview;
        }
        
        @Override
        public void run() {
            
            if(m_throwThread == null)
            {
                m_adapter.clear();
                //Additional review because of the first item of the list not a review!!!
                m_adapter.add(new ReviewInfo());
                
                //True add other review
                for (ReviewInfo oneReview : m_listReviews) 
                {
                    m_adapter.add(oneReview);
                }
                
                m_pDlg.dismiss();
                
                if (m_bIfOkPostReview)
                {
                    Toast.makeText(EntityViewActivity.this, 
                        R.string.toast_success_post_review, Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                m_pDlg.dismiss();
                
                new AlertDialog.Builder(EntityViewActivity.this)
                .setTitle(R.string.adlg_title_error)
                .setMessage(/*VootaDroidConstants.getErrorMessage(
                    m_throwThread.getErrorCode(), EntityViewActivity.this)*/
                        m_throwThread.getMessage())
                .setNeutralButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                m_throwThread = null;
            }
        }
    };
    
    private class ReviewAdapter extends ArrayAdapter<ReviewInfo> 
    {
        private ArrayList<ReviewInfo> m_reviewItems;
        private final String m_strAPILevel = android.os.Build.VERSION.SDK;
        private Integer m_nAPILevel;
        private LinearLayout.LayoutParams m_layoutParams;

        public ReviewAdapter(Context context, int textViewResourceId, 
                ArrayList<ReviewInfo> items) 
        {
                super(context, textViewResourceId, items);
                this.m_reviewItems = items;
                m_layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.FILL_PARENT);

                if (m_strAPILevel.equals("3"))
                {
                    m_nAPILevel = 3;
                }
                else
                {
                    try 
                    {
                        Class c = Class.forName("android.os.Build$VERSION");
                        Field field_SDK_INT = c.getField("SDK_INT");
                        m_nAPILevel = (Integer)field_SDK_INT.get(null);
                    } 
                    catch (Throwable e) 
                    {
                        m_nAPILevel = Integer.getInteger(m_strAPILevel);
                    }
                }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            View v = convertView;
            if(position == 0)
            {
                if (v == null)
                {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (m_nAPILevel <= 3)
                    {
                        v = vi.inflate(R.layout.entity_15, null);
                    }
                    else
                    {
                        v = vi.inflate(R.layout.entity, null);
                    }
                }
                
                m_tvEntityName = (TextView) v.findViewById(R.id.label_entity_name);
                m_imgEntity = (ImageView) v.findViewById(R.id.image_entity);
                m_btnTumbUp = (ImageButton) v.findViewById(R.id.btn_tumb_up);
                m_btnTumbDown = (ImageButton) v.findViewById(R.id.btn_tumb_down);
                m_tvPositiveVotes = (TextView) v.findViewById(R.id.label_votes_positive);
                m_tvNegativeVotes = (TextView) v.findViewById(R.id.label_votes_negative);
                
                m_tvEntityName.setText(m_entityInfo.getLongName());
                m_tvPositiveVotes.setText(String.valueOf(m_entityInfo.getPositiveVotes()));
                m_tvNegativeVotes.setText(String.valueOf(m_entityInfo.getNegativeVotes()));
                m_btnTumbUp.setOnClickListener(OnClickUp);
                m_btnTumbDown.setOnClickListener(OnClickDown);
                
                if (m_btmpEntity != null)
                {
                    m_imgEntity.setImageBitmap(m_btmpEntity);
                    int nImgHeight = m_btmpEntity.getHeight();
                    
                    m_btnTumbUp.measure(0, 0);
                    int nTwoBtnHeight = m_btnTumbUp.getMeasuredHeight() * 2 + 2;
                    // we must know what layout is higher to make one of its height - fill parent
                    if (m_nAPILevel > 3)
                    {
                        if (nImgHeight > nTwoBtnHeight)
                        {
                            m_rlVootesRoot = (RelativeLayout) v.findViewById(R.id.layout_votes_root);
                            m_rlVootesRoot.setLayoutParams(m_layoutParams);
                        }
                    }
                    else
                    {
                        m_llVootes15Root = (LinearLayout) v.findViewById(R.id.layout_15_votes_root);
                        int nTopPadding = (nImgHeight - nTwoBtnHeight) / 2;
                        int nLeftPadding = (m_lvEntity.getWidth() - 
                                m_btmpEntity.getWidth() - (nTwoBtnHeight / 2 + 12)) / 2;
                        m_llVootes15Root.setPadding(
                                nLeftPadding > 0 ? nLeftPadding : 0, 
                                nTopPadding > 0 ? nTopPadding : 0, 0, 0);
                    }
                }
                return v;
            }
            else
            {
                if (v == null)
                {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.review_row, null);
                }
                ReviewInfo oneReview = m_reviewItems.get(position);
                if (oneReview != null) 
                {
                        TextView tvPosNeg = (TextView) v.findViewById(R.id.label_pos_or_neg);
                        TextView tvReviewText = (TextView) v.findViewById(R.id.label_review_text);
                        
                        if(oneReview.isReviewPositive())
                        {
                            tvPosNeg.setText(POSITIVE_VOTE);
                        }
                        else
                        {
                            tvPosNeg.setText(NEGATIVE_VOTE);
                        }
                        tvReviewText.setText(oneReview.getText());
                }
                return v;
            }
        }
        
        @Override
        public int getViewTypeCount()
        {
            return 2;
        }
        
        @Override 
        public int getItemViewType(int position)
        {
            if (position == 0)
                return 0;
            else
                return 1;
        }
    }

    private class PostReviewRunnable implements Runnable 
    {
    	private ReviewInfo m_reviewPost;
    	
    	public PostReviewRunnable(ReviewInfo review) 
    	{
    		m_reviewPost = review;
		}
    	
		@Override
		public void run() 
		{
			try
			{
				VootaDroid.m_vootaApi.postReview(m_reviewPost, 
				        VootaDroid.m_strAccessToken, VootaDroid.m_strTokenSecret);
				// get new values of pos/neg reviews
				//m_entityInfo = VootaDroid.m_vootaApi.getEntityInfo(m_entityInfo);
				m_handler.post(new GetDataRunnable(true));
			}
			catch (VootaApiException e)
			{
				m_throwThread = e;
				m_handler.post(new EndProcRunnable(false));
			}
		}
	}; 
}