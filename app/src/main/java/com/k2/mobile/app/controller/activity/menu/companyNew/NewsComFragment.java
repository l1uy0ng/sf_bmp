package com.k2.mobile.app.controller.activity.menu.companyNew;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.NewsListAdapter;
import com.k2.mobile.app.model.bean.CommonNewsBean;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.ReqMessage;
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
import com.k2.mobile.app.view.widget.XListView;
import com.k2.mobile.app.view.widget.XListView.IXListViewListener;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class NewsComFragment extends Fragment implements IXListViewListener {
	
	private View view;
	private ViewPager viewPager;
	// ?????????????????????
	private List<ImageView> imageViews; 
	private FrameLayout fl_news;
	// ??????????????????????????????
	private List<View> dots;
	// ????????????
	private TextView tv_title;
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
						}
					}
					
					break;
					
				default:
					viewPager.setCurrentItem(currentItem);
			}
		};
	};

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
		view = inflater.inflate(R.layout.fragment_news_com, null);
		initView();
		initListener();
		return view;
	}

	/*
	 * ?????????: initView() 
	 * ??? ??? : ????????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		
		newsTop = new ArrayList<TopNewsBean>();
		newsList = new ArrayList<CommonNewsBean>();
		imageViews = new ArrayList<ImageView>();
		
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		fl_news = (FrameLayout)view.findViewById(R.id.fl_news);
		viewPager = (ViewPager) view.findViewById(R.id.vp);
		mXListView = (XListView) view.findViewById(R.id.lv_news_show);
		// ???????????????????????????
		mXListView.setPullRefreshEnable(true);
		// ????????????????????????
		mXListView.setPullLoadEnable(true);
		mListAdapter = new NewsListAdapter(getActivity(), newsList);
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
		mXListView.setXListViewListener(this);
		mXListView.setOnItemClickListener(itemListener);
	}
	
	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CommonNewsBean cnBean = newsList.get(position - 1);
			if(null != cnBean &&  null != cnBean.getFormInstanceCode() && !"".equals(cnBean.getFormInstanceCode().trim())){
				Intent mIntent = new Intent(getActivity(), NewsInfolActivity.class);
				mIntent.putExtra("formInstanceCode", cnBean.getFormInstanceCode());
				mIntent.putExtra("type", 1);
				mIntent.putExtra("operClass", 1);
				mIntent.putExtra("procVerID", cnBean.getProcVerID());
				mIntent.putExtra("procSetID", cnBean.getProcSetID());
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
		
		imageLoader = new ImageLoaderUtil(getActivity()); 
		// ??????
		dots = new ArrayList<View>();
		dots.add(view.findViewById(R.id.v_dot1));
		dots.add(view.findViewById(R.id.v_dot2));
		dots.add(view.findViewById(R.id.v_dot3));
		dots.add(view.findViewById(R.id.v_dot4));
		dots.add(view.findViewById(R.id.v_dot5));
		dots.add(view.findViewById(R.id.v_dot6));
		
		// ?????????????????????
		for (int i = 0; i < newsTop.size(); i++) {
			final TopNewsBean tnBean = newsTop.get(i);
			ImageView imageView = new ImageView(getActivity());
			imageLoader.DisplayImage(tnBean.getNewsImg(), imageView, 2, null); // ??????????????????
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent mIntent = new Intent(getActivity(), NewsInfolActivity.class);
					mIntent.putExtra("formInstanceCode", tnBean.getFormInstanceCode());
					mIntent.putExtra("type", 1);
					mIntent.putExtra("operClass", 1);
					mIntent.putExtra("procVerID", tnBean.getProcVerID());
					mIntent.putExtra("procSetID", tnBean.getProcSetID());
					startActivity(mIntent);
				}
			});
			
			imageViews.add(imageView);
			fl_news.setVisibility(View.VISIBLE);
			dots.get(i).setVisibility(View.VISIBLE);
		}
		
		tv_title.setText(newsTop.get(0).getNewsTitle());
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
		
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		bean.setPageIndex(pageIndex + "");
		bean.setPageSize(pageSize + "");
		
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
		if (!NetWorkUtil.isNetworkAvailable(getActivity())) {            
			DialogUtil.showLongToast(getActivity(), R.string.global_network_disconnected);
		}else{
			String info = remoteRequestData();
			SendRequest.sendSubmitRequest(getActivity(), info, BaseApp.token, BaseApp.reqLang, 
					LocalConstants.NEW_LIST_SERVER, BaseApp.key, submitCallBack);
		}
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
			if(1 == operClass){
				DialogUtil.showWithCancelProgressDialog(getActivity(), null, getResources().getString(R.string.global_prompt_message), null);
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
			if (null != result && !"".equals(result.trim())) {
				ReqMessage msgs = JSON.parseObject(result, ReqMessage.class);
				// ????????????????????????????????????
				if (null == msgs || null == msgs.getResCode() || "".equals(msgs.getResCode())) {				
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
					return;
				}else if ("1103".equals(msgs.getResCode()) || "1104".equals(msgs.getResCode())) {
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode(msgs.getResCode(), getResources()));	
					Intent mIntent = new Intent(BroadcastNotice.USER_EXIT);
					getActivity().sendBroadcast(mIntent);
					return;
				}else if("1210".equals(msgs.getResCode())){
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode(msgs.getResCode(), getResources()));	
					Intent mIntent = new Intent(BroadcastNotice.WIPE_USET);
					getActivity().sendBroadcast(mIntent);
					return;
				}else if ("1".equals(msgs.getResCode())) {
					Message msg = new Message();
					String decode = EncryptUtil.getDecodeData(msgs.getMessage(), BaseApp.key);
					msg.what = 1;
					msg.obj = decode;
					mHandler.sendMessage(msg);
				}
			} else {
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
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
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("1207", getResources()));
			}else{
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
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
			tv_title.setText(newsTop.get(position).getNewsTitle());
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
		mXListView.setRefreshTime(getResources().getString(R.string.just));
	}
}