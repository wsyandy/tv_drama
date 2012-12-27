package com.jumplife.tvdrama;

import java.util.ArrayList;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sectionlistview.DramaSectionAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Section;
import com.kuad.KuBanner;
import com.kuad.kuADListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DramaSectionActivity extends Activity implements AdWhirlInterface{
    private GridView        sectioGridView;
    private ImageButton     imageButtonRefresh;
    private TextView		textViewFeedback;
    private TextView 		tvChapterNO;
    private TextView 		tvNotify;
    private String currentSection = "";
    private int screenWidth;
    private LoadDataTask    loadTask;
    private TextView topbar_text;
	private ArrayList<Section> sectionList;

	private int dramaId = 0;
	private String dramaName = "";
	private int chapterNo = 0;
	private SharePreferenceIO shIO;
	private DramaSectionAdapter dramaSectionAdapter;
	private static String TAG = "DramaSectionActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dramasection);
        
        Log.d(TAG, "init view begin");
        
        initViews();

        Log.d(TAG, "init view end");
        
        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        
        AdTask adTask = new AdTask();
    	adTask.execute();
    }

    private void initViews() {
    	shIO = new SharePreferenceIO(this);
    	
    	Bundle extras = getIntent().getExtras();
        if(extras != null) {
        	dramaId = extras.getInt("drama_id");
        	dramaName = extras.getString("drama_name");
        	chapterNo = extras.getInt("chapter_no");
        }
        
        topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(dramaName);
        
        tvChapterNO = (TextView)findViewById(R.id.textview_chapternumber);
        tvChapterNO.setText(getResources().getString(R.string.episode) + chapterNo + getResources().getString(R.string.no));
        
        tvNotify = (TextView)findViewById(R.id.textview_notify);
        
        sectioGridView = (GridView)findViewById(R.id.gridview_section);
    	imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
    	textViewFeedback = (TextView)findViewById(R.id.textview_feedback);
    }
    
    public void setAd() {
    	
    	Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 60); 
        //AdWhirlTargeting.setAge(23);
        //AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
        //AdWhirlTargeting.setKeywords("online games gaming");
        //AdWhirlTargeting.setPostalCode("94123");
        AdWhirlTargeting.setTestMode(false);
   		
        AdWhirlLayout adwhirlLayout = new AdWhirlLayout(this, adwhirlKey);	
        
    	adwhirlLayout.setAdWhirlInterface(this);
    	
    	adwhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);
    	//adwhirlLayout.setLayoutParams();
    	
    	/*TextView ta  = (TextView) findViewById(R.layout.text_view);
       LayoutParams lp = new LayoutParams();
       lp.gravity= Gravity.CENTER_HORIZONTAL; 
       ta.setLayoutParams(lp);
    	 * 
    	 */

	 	
    	adLayout.addView(adwhirlLayout);
   
    }
    
    public void setKuAd() {
    	KuBanner banner;
    	banner = new KuBanner(this);
    	
    	Resources res = getResources();
    	String kuAdKey = res.getString(R.string.kuad_key);
    	
    	banner.setAPID(kuAdKey);
    	banner.appStart();
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);

        // Add the adView to it
    	adLayout.addView(banner);
        
        banner.setkuADListener(new kuADListener(){
        	public void onRecevie(String msg) {
			//成功接收廣告
				Log.i("AdOn", "OnReceviekuAd");
			}
			public void onFailedRecevie(String msg) {
			//失敗接收廣告
				Log.i("AdOn", "OnFailesToReceviekuAd");
			}
			});
    }
    
    // 設定畫面上的UI
    private void setViews() {
    	
    	textViewFeedback.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
				String[] ccs={"abooyaya@gmail.com, chunyuko85@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				it.putExtra(Intent.EXTRA_CC, ccs); 
				it.putExtra(Intent.EXTRA_SUBJECT, "[電視連續劇] 建議回饋(" + dramaName + "第" + chapterNo + "集)"); 
				it.putExtra(Intent.EXTRA_TEXT, dramaName + "第" + chapterNo + "集");      
				startActivity(it);  
			}			
		});
    	imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Log.d("", "Click imageButtonRefresh");
                loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
    	sectioGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	currentSection = chapterNo + ", " + (position + 1);
            	dramaSectionAdapter.setCurrentSection(currentSection);
            	dramaSectionAdapter.notifyDataSetChanged();
            	shIO.SharePreferenceI("views", true);
            	Uri uri;
            	if(sectionList.get(position).getUrl() == null ||
            			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
            			sectionList.get(position).getUrl().contains("maplestage")) {
            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity.this);
    		        dialog.setTitle(getResources().getString(R.string.no_link));
    		        dialog.setMessage(getResources().getString(R.string.use_google_search));
    		        dialog.setPositiveButton(getResources().getString(R.string.google_search), new DialogInterface.OnClickListener() {
    		            public void onClick(DialogInterface dialog, int which) {
    		            	Intent search = new Intent(Intent.ACTION_WEB_SEARCH);  
    	            		search.putExtra(SearchManager.QUERY, dramaName + " " + getResources().getString(R.string.episode) 
    	            				+ chapterNo + getResources().getString(R.string.no));  
    	            		startActivity(search);
    		            }
    		        });
    		        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
    		            public void onClick(DialogInterface dialog, int which) {
    		            }
    		        });
    		        dialog.show();            		 
            	} else {
            		if (sectionList.get(position).getUrl().contains("http://www.dailymotion.com/embed/video/")) {
            			String url = sectionList.get(position).getUrl();
            			url = url.substring(39);
            			String[] tmpUrls = url.split("\\?");
            			String tmpId = null;
            			if(tmpUrls.length > 0)
            				tmpId = tmpUrls[0];
            			if(tmpId != null)
            				sectionList.get(position).setUrl("http://touch.dailymotion.com/video/" + tmpId);
            		}
            			
            		uri = Uri.parse(sectionList.get(position).getUrl());
            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
            		startActivity(it);
            	}
            	new UpdateDramaSectionRecordTask().execute();
            }
        });
    	
    	DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        dramaSectionAdapter = new DramaSectionAdapter(DramaSectionActivity.this, sectionList, (int) ((screenWidth / 2)),
        		(int) (((screenWidth / 2)) * 0.6), currentSection, chapterNo);
        sectioGridView.setAdapter(dramaSectionAdapter);
        
        Boolean notify = false;
        for(int i=0; i<sectionList.size(); i++) {
        	if(sectionList.get(i).getUrl().contains("www.wat.tv") 
        			|| sectionList.get(i).getUrl().contains("http://106.187.51.230"))
        		notify = true;
        }
        
        if(notify)
        	tvNotify.setVisibility(View.VISIBLE);
        else
        	tvNotify.setVisibility(View.GONE);
    }

    private void fetchData() {
    	Log.d(TAG, "load data API begin");
    	DramaAPI dramaAPI = new DramaAPI(this);
    	sectionList = new ArrayList<Section>();
    	sectionList = dramaAPI.getChapterSectionNew(dramaId, chapterNo);
    	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
    	currentSection = sqlTvDrama.getDramaSectionRecord(dramaId);
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	              LoadDataTask.this.cancel(true);
	              imageButtonRefresh.setVisibility(View.VISIBLE);
	              finish();
	          }
	      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(DramaSectionActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            
            Log.d(TAG, "load data onPreExecute");
            
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	if(DramaSectionActivity.this != null && !DramaSectionActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();

            if (sectionList == null) {
            	sectioGridView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	sectioGridView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }
            Log.d(TAG, "load data onPostExecute");
            
            super.onPostExecute(result);
        }

    }

    class UpdateViewTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	if(shIO.SharePreferenceO("views", false)) {
        		DramaAPI dramaAPI = new DramaAPI();
        		dramaAPI.updateViews(dramaId);
        		shIO.SharePreferenceI("views", false);
        	}
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
        }

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	UpdateViewTask task = new UpdateViewTask();
            task.execute();            
        }

        return super.onKeyDown(keyCode, event);
    }
    
    class UpdateDramaSectionRecordTask extends AsyncTask<Integer, Integer, String>{  
        
		@Override  
        protected void onPreExecute() {
			super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(DramaSectionActivity.this);
			sqlTvDrama.updateDramaSectionRecord(dramaId, currentSection);
			return "progress end";
        }  
 

		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);  
        }  
          
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
	protected void onDestroy(){
        super.onDestroy();
        if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED)
        	loadTask.cancel(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    class AdTask extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... arg0) {
			
			return null;
		}
		
		 @Override  
	     protected void onPostExecute(String result) {
			 setAd();
			 super.onPostExecute(result);

		 }
    	
    }

	public void adWhirlGeneric()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void showKuAd() {
		setKuAd();
	}
}