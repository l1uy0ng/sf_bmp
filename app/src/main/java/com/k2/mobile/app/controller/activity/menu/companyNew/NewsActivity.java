package com.k2.mobile.app.controller.activity.menu.companyNew;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.HttpConstants;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.core.BaseActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.NewsListAdapter;
import com.k2.mobile.app.model.bean.CommonNewsBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.bean.ResNewsBean;
import com.k2.mobile.app.model.bean.TopNewsBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.ImageLoaderUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.view.widget.XListView;
import com.k2.mobile.app.view.widget.XListView.IXListViewListener;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

/**
 * 
 * @ClassName: NewsActivity
 * @Description: ????????????
 * @author linqijun
 * @date 2015-7-31 ??????17:32:57
 *
 */
public class NewsActivity extends BaseActivity implements IXListViewListener, OnClickListener {
	
	// ????????????
	private TextView tv_titles;
	// ??????
	private Button btn_back;
	// ??????
	private TextView tv_search;
	private ViewPager viewPager;
	// ?????????????????????
	private List<ImageView> imageViews; 
	private FrameLayout fl_news;
	// ??????????????????????????????
	private List<View> dots;
	// ????????????
	private TextView tv_tip_title;
	// ????????????????????????
	private int currentItem = 0;
	// ????????????
	private ScheduledExecutorService scheduledExecutorService;
	// ?????????????????????
	private XListView mXListView;
	// ??????????????????
	private NewsListAdapter mListAdapter;
	// ??????????????????
	private List<TopNewsBean> newsTop ;
	// ??????????????????
	private List<CommonNewsBean> newsList;
	// ??????
	private int pageIndex = 1;
	// ????????????
	private int pageSize = 10;
	// ????????????
	private int operClass = 1;
	// ?????????
	private int count = 0;
	// ??????????????????
	public ImageLoaderUtil imageLoader;   
	// ???????????????????????????
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String json = (String) msg.obj;
					System.out.println("json = "+json);
					if(2 != operClass){
						newsList.clear();
					}
					
					newsTop.clear();
					imageViews.clear();
					
					ResNewsBean rnBean = JSON.parseObject(json, ResNewsBean.class);
					if(null != rnBean){
						if(null != rnBean.getNewsTopRspEntities() && rnBean.getNewsTopRspEntities().size()>0){
							newsTop.addAll(rnBean.getNewsTopRspEntities());
							if(1 > count){
								showTopNews();
							}
							count++;
						}
						if(null != rnBean.getNewsListRspEntities() && rnBean.getNewsListRspEntities().size()>0){
							newsList.addAll(rnBean.getNewsListRspEntities());
							mListAdapter.notifyDataSetChanged();
							
							String tmpTips = null;
							
							if((null == rnBean.getNewsListRspEntities() || 1 > rnBean.getNewsListRspEntities().size()) && 1 > newsList.size()){ // ????????????
								tmpTips = getString(R.string.no_data);
							}else if(null != rnBean.getNewsListRspEntities() && rnBean.getNewsListRspEntities().size() < 10){			// ?????????????????????
								tmpTips = getString(R.string.all_data_loaded);
							}else{													// ????????????
								tmpTips = getString(R.string.xlistview_footer_hint_normal);
							}
							
							mXListView.setTips(tmpTips);
						}
					}
					
					break;
					
				default:
					viewPager.setCurrentItem(currentItem);
					break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ????????????
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_news_com);
		initView();
		initListener();
	}
	
	/*
	 * ?????????: initView() 
	 * ??? ??? : ????????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		tv_titles = (TextView) findViewById(R.id.tv_title);
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_search = (TextView) findViewById(R.id.tv_sech);
		
		tv_titles.setText(getString(R.string.company_news));
		tv_search.setVisibility(View.GONE);
		
		newsTop = new ArrayList<TopNewsBean>();
		newsList = new ArrayList<CommonNewsBean>();
		imageViews = new ArrayList<ImageView>();
		
		tv_tip_title = (TextView) findViewById(R.id.tv_tip_title);
		fl_news = (FrameLayout) findViewById(R.id.fl_news);
		viewPager = (ViewPager) findViewById(R.id.vp);
		mXListView = (XListView) findViewById(R.id.lv_news_show);
		// ????????????????????????
		mXListView.setPullRefreshEnable(true);
		// ????????????????????????
		mXListView.setPullLoadEnable(true);
		mListAdapter = new NewsListAdapter(NewsActivity.this, newsList);
		mXListView.setAdapter(mListAdapter);
		
		remoteRequest();
	}
	/*
	 * ?????????: initListener() 
	 * ??? ??? : ?????????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener(){
		btn_back.setOnClickListener(this);
		mXListView.setXListViewListener(this);
		mXListView.setOnItemClickListener(itemListener);
	}
	
	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CommonNewsBean cnBean = newsList.get(position - 1);
			if(null != cnBean &&  null != cnBean.getFormInstanceCode() && !"".equals(cnBean.getFormInstanceCode().trim())){
				Intent mIntent = new Intent(NewsActivity.this, NewsInfolActivity.class);
				mIntent.putExtra("formInstanceCode", cnBean.getFormInstanceCode());
				mIntent.putExtra("type", 1);
				mIntent.putExtra("operClass", 1);
				startActivity(mIntent);
			}
		}
	};
	/*
	 * ?????????: showView() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void showTopNews() {
		
		if(null == newsTop){
			return;
		}
		
		imageLoader = new ImageLoaderUtil(NewsActivity.this); 
		// ??????
		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.v_dot1));
		dots.add(findViewById(R.id.v_dot2));
		dots.add(findViewById(R.id.v_dot3));
		dots.add(findViewById(R.id.v_dot4));
		dots.add(findViewById(R.id.v_dot5));
		dots.add(findViewById(R.id.v_dot6));
		
		// ?????????????????????
		for (int i = 0; i < newsTop.size(); i++) {
			final TopNewsBean tnBean = newsTop.get(i);
			ImageView imageView = new ImageView(NewsActivity.this);
			String url = "http://"+ HttpConstants.DOMAIN_NAME + ":" + HttpConstants.PROT + tnBean.getNewsImg();
			System.out.println("url = "+url);
			imageLoader.DisplayImage(url, imageView, 2, null); // ??????????????????
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent mIntent = new Intent(NewsActivity.this, NewsInfolActivity.class);
					mIntent.putExtra("formInstanceCode", tnBean.getFormInstanceCode());
					mIntent.putExtra("type", 1);
					startActivity(mIntent);
				}
			});
			
			imageViews.add(imageView);
			fl_news.setVisibility(View.VISIBLE);
			dots.get(i).setVisibility(View.VISIBLE);
		}
		
		tv_tip_title.setText(newsTop.get(0).getNewsTitle());
		// ????????????ViewPager??????????????????
		viewPager.setAdapter(new NewsAdAdapter());
		// ???????????????????????????ViewPager???????????????????????????
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
	}
		
	/**
	 * ?????????: remoteRequestData() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: String ????????????
	 */
	private String remoteRequestData() {
				
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F40000002");
		bBean.setInvokeParameter("[\"T4\",\""+pageSize+"\",\""+pageIndex+"\"]");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	/**
	 * ?????????: remoteRequest() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void remoteRequest(){
		// ??????????????????
		if (!NetWorkUtil.isNetworkAvailable(NewsActivity.this)) {            
			DialogUtil.showLongToast(NewsActivity.this, R.string.global_network_disconnected);
		}else{
			String info = remoteRequestData();
			SendRequest.submitRequest(NewsActivity.this, info, submitCallBack);
		}
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
			if(1 == operClass){
				DialogUtil.showWithCancelProgressDialog(NewsActivity.this, null, res.getString(R.string.global_prompt_message), null);
			}
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
				
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			if(1 == operClass){
				DialogUtil.closeDialog();
			}else{
				onLoad();
			}
			String result = responseInfo.result.toString();
			byte[] ebase64 = EncryptUtil.decodeBase64(result);
			if (null != result && !"".equals(result.trim())) {
				PublicResultBean resMsg = JSON.parseObject(new String(ebase64), PublicResultBean.class);
				// ????????????????????????????????????
				if (null == resMsg || null == resMsg.getResHeader().getStateCode() || "".equals(resMsg.getResHeader().getStateCode())) {				
					LogUtil.promptInfo(NewsActivity.this, ErrorCodeContrast.getErrorCode("0", res));
					return;
				}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
					LogUtil.promptInfo(NewsActivity.this, resMsg.getResHeader().getReturnMsg());	
					return;
				}
				// ???????????????????????????
				if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
					LogUtil.promptInfo(NewsActivity.this, ErrorCodeContrast.getErrorCode("0", res));
				} else {
					if(null != resMsg.getResHeader().getSectetKey() && !"".equals(resMsg.getResHeader().getSectetKey().trim())){
						BaseApp.key = resMsg.getResHeader().getSectetKey();
					}
					// ?????????????????????????????????
					Message msgs = new Message();
					msgs.what = 1;
					msgs.obj = resMsg.getResBody().getResultString();
					mHandler.sendMessage(msgs);
				}
			} else {
				LogUtil.promptInfo(NewsActivity.this, ErrorCodeContrast.getErrorCode("0", res));
			}		
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			if(1 == operClass){
				DialogUtil.closeDialog();
			}else{
				onLoad();
			}
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(NewsActivity.this, ErrorCodeContrast.getErrorCode("1207", res));
			}else{
				LogUtil.promptInfo(NewsActivity.this, ErrorCodeContrast.getErrorCode("0", res));
			}
		}
	};	
	
	@Override
	public void onStart() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		// ???Activity?????????????????????5??????????????????????????????
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 5, TimeUnit.SECONDS);
		super.onStart();
	}
	
	@Override
	public void onStop() {
		// ???Activity??????????????????????????????
		scheduledExecutorService.shutdown();
		super.onStop();
	}
	
	/* ??????????????????  */
	private class ScrollTask implements Runnable {
		@Override
		public void run() {
			synchronized (viewPager) {
				currentItem = (currentItem + 1) % imageViews.size();
				mHandler.obtainMessage().sendToTarget(); // ??????Handler????????????
			}
		}
	}
	/* ???ViewPager??????????????????????????????????????? */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;
		@Override
		public void onPageSelected(int position) {
			currentItem = position;
			tv_tip_title.setText(newsTop.get(position).getNewsTitle());
			dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
			oldPosition = position;
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}
	
	/* ??????ViewPager?????????????????? */
	private class NewsAdAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return newsTop.size();
		}
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			try {
				((ViewPager) arg0).addView(imageViews.get(arg1));
				return imageViews.get(arg1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}
		@Override
		public Parcelable saveState() {
			return null;
		}
		@Override
		public void startUpdate(View arg0) {

		}
		@Override
		public void finishUpdate(View arg0) {

		}
	}

	// ??????
	@Override
	public void onRefresh() {
		operClass = 3;
		pageIndex = 1;
		remoteRequest();
	}

	// ????????????
	@Override
	public void onLoadMore() {
		operClass = 2;
		pageIndex++;
		remoteRequest();
	}

	// ????????????
	private void onLoad() {
		mXListView.stopRefresh();
		mXListView.stopLoadMore();
		mXListView.setRefreshTime(res.getString(R.string.just));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
		}
	}
}
