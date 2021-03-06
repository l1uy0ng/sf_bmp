package com.k2.mobile.app.controller.activity.menu.examine;
 
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.BuildConfig;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.menu.hr.HRStaticLinkListActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.FlowAdapter;
import com.k2.mobile.app.model.bean.FlowBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.view.widget.AddAndSubEditText;
import com.k2.mobile.app.view.widget.XListView;
import com.k2.mobile.app.view.widget.XListView.IXListViewListener;

import java.util.ArrayList;
import java.util.List;

/**
* @Title IinitiatedFragment.java
* @Package com.oppo.mo.controller.activity.menu.workbench;
* @Description ????????????
* @Company  K2
* 
* @author linqijun
* @date 2015-3-13 ??????3:22:19
* @version V1.0
*/
@SuppressLint("NewApi")
public class IinitiatedFragment extends Fragment implements OnClickListener{

	private static final String TAG = "IinitiatedFragment";
	private View view            = null;
	private TextView tv_fiction;
	private TextView tv_review;
	private AddAndSubEditText mSearch;
	private XListView lv_work_show;
	// ??????
	private int pageIndex = 1;
	// ????????????
	private int pageSize = 10;
	// ???????????? 1,????????? 2,????????? 3,????????? 4,????????? 5,?????????
	private int operClass = 1;
	// ????????????????????????
	private int flag = 1;
	// ?????????
	private String tipsQuasi = null;
	// ?????????
	private String tipsAudit = null;
	// ?????????
	private String tipspassed = null;
	// ?????????
	private FlowAdapter fAdapter = null;
	private List<FlowBean> fList = null;
	// ??????????????????
	private List<FlowBean> ficList = null;
	// ?????????????????????
	private List<FlowBean> apiList = null;
	// ?????????????????????
	private List<FlowBean> passList = null;
	// ??????
	private IncomingReceiver iReceiver = null;
	private int apiCount = 0;
	private int passCount = 0;
	private int positions = -1;
	private int x = 687;
	private int y = 600;
	
	protected Resources res;
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 1:
				String json = (String) msg.obj;
				System.out.println("app json = "+json);
				if(null != json && !"-1".equals(json)){
					List<FlowBean> fbList = JSON.parseArray(json, FlowBean.class);
					if(null != fbList){
						if(3 != flag){
							fList.clear();
						}
						// ??????????????????
						for(int i=0; i<fbList.size(); i++){
							fbList.get(i).setClassType(operClass);
						}
						
						fList.addAll(fbList);
						if (BuildConfig.DEBUG)
						fAdapter.notifyDataSetChanged();
						
						if(1 == operClass){
							ficList.clear();
							ficList.addAll(fList);
						}else if(2 == operClass){
							apiList.clear();
							apiList.addAll(fList);
						}else if(3 == operClass){
							passList.clear();
							passList.addAll(fList);
						}
					}
					
					String tmpTips = null;
					
					if((null == fbList || 1 > fbList.size()) && 1 > fList.size()){ 				// ????????????
						tmpTips = getString(R.string.no_data);
					}else if(null != fbList && fbList.size() < 10){			// ?????????????????????
						tmpTips = getString(R.string.all_data_loaded);
					}else{													// ????????????
						tmpTips = getString(R.string.xlistview_footer_hint_normal);
					}
					
					lv_work_show.setTips(tmpTips);
					if(1 == operClass){
						tipsQuasi = tmpTips;
					}else if(2 == operClass){
						tipsAudit = tmpTips;
					}else if(3 == operClass){
						tipspassed = tmpTips;
					}
				}					
				break;
			case 7:
				lv_work_show.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), 
	        			SystemClock.uptimeMillis()+20, MotionEvent.ACTION_UP, x, y, 0));
				break;
			case 8:
				lv_work_show.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), 
	        			SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x, y, 0));
				y+=110;
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_launch_approval, null);
		initView();
		initListener();
		initAdapter(1);
		createFilter();
		requestServer();
		return view;
	}
	/*
	 * ?????????: initView() 
	 * ??? ??? : ????????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView(){
		tv_fiction = (TextView) view.findViewById(R.id.tv_fiction);
		tv_review = (TextView) view.findViewById(R.id.tv_review);
		mSearch = (AddAndSubEditText) view.findViewById(R.id.search);
	    lv_work_show = (XListView) view.findViewById(R.id.lv_work_show);
	    lv_work_show.setPullRefreshEnable(true);	// ??????????????????
	    lv_work_show.setPullLoadEnable(false);		// ???????????????????????? ljw 2016-01-16
		
		fList = new ArrayList<FlowBean>();
		ficList = new ArrayList<FlowBean>();
		apiList = new ArrayList<FlowBean>();
		passList = new ArrayList<FlowBean>();
		
		res = getResources();
		
		switchBg(1);
	}

	/**
	 * ?????????: initAdapter() 
	 * ??? ??? : ?????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	//ljw 2016-06-17 ?????????????????????
	private void initAdapter(int openType) {
		fAdapter = new FlowAdapter(getActivity(), fList,openType);
		lv_work_show.setAdapter(fAdapter);
	}
		
	/*
	 * ?????????: initListener()  
	 * ??? ??? : ???????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener(){
		tv_fiction.setOnClickListener(this);
		tv_review.setOnClickListener(this);
		lv_work_show.setXListViewListener(xListener);
		lv_work_show.setOnItemClickListener(itemListener);
		//????????????
		mSearch.setOnDrawableRightListener(new AddAndSubEditText.OnDrawableRightListener() {
			@Override
			public void onDrawableRightClick(View view) {
				String searchString = mSearch.getText().toString().toLowerCase();
				if (pageFlag == 1) {
					if (searchString.equals("")) {
						fList.clear();
						fList.addAll(ficList);
						fAdapter.notifyDataSetChanged();
					} else {
						fList.clear();
						for (FlowBean flowBean : ficList) {
							String activityName = flowBean.getActivityName();
							String displayName = flowBean.getDisplayName();
							String folio = flowBean.getFolio();
							if (!activityName.isEmpty() && activityName.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							} else if (!displayName.isEmpty()&&displayName.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							} else if (!folio.isEmpty()&&folio.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							}
						}
						fAdapter.notifyDataSetChanged();
					}
				} else if (pageFlag == 2) {
					if (searchString.equals("")) {
						fList.clear();
						fList.addAll(apiList);
						fAdapter.notifyDataSetChanged();
					} else {
						fList.clear();
						for (FlowBean flowBean : apiList) {
							String activityName = flowBean.getActivityName();
							String displayName = flowBean.getDisplayName();
							String folio = flowBean.getFolio();
							if (!activityName.isEmpty() && activityName.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							} else if (!displayName.isEmpty()&&displayName.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							} else if (!folio.isEmpty()&&folio.toLowerCase().contains(searchString)) {
								fList.add(flowBean);
							}
						}
						fAdapter.notifyDataSetChanged();
					}
				}
			}
		});
	}
	
	/**
	 * @Title: createFilter
	 * @Description: ??????IntentFilter
	 * @return void 
	 * @throws
	 */
	private void createFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastNotice.I_STARTED_REFRESH);
		iReceiver = new IncomingReceiver();
		// ????????????
		getActivity().registerReceiver(iReceiver, filter);
	}
	
	/**
	 * ????????????
	 */ 
	private class IncomingReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();  
	        if(action.equals(BroadcastNotice.I_STARTED_REFRESH))  
	        { 
	        	fList.remove(positions);
	        	fAdapter.notifyDataSetChanged();
	        } 
		}
	}
	
	/**
	 * @Title: getLoginInfo
	 * @Description: ??????????????????
	 * @return String ??????????????? 
	 * @throws
	 */
	private String getLoginInfo(String code){		
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode(code);
		bBean.setInvokeParameter("[\""+pageSize+"\",\""+pageIndex+"\"]");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * @Title: doubleClickRefresh
	 * @Description: ????????????
	 * @return void 
	 * @throws
	 */
	public void doubleClickRefresh(){
		x = 687;
    	y = 200;
    	lv_work_show.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));
    	new Thread(new Runnable() {
			@Override
			public void run() {
				for(int i=0; i < 6;i++){
					Message msg = new Message();
					msg.what = 8;
					mHandler.sendMessage(msg);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Message msg = new Message();
				msg.what = 7;
				mHandler.sendMessage(msg);
				
			}
		}).start();
	}
	
	/*
	 * ?????????: refresh()  
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	public void refresh(){
		flag = 2;
		pageIndex = 1;
		requestServer();
	}
	
	IXListViewListener xListener = new IXListViewListener() {
		@Override
		public void onRefresh() {
			refresh();
		}
		
		@Override
		public void onLoadMore() {
			flag = 3;
			pageIndex++;
			requestServer();
		}
	};
	
	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			positions = position - 1; 
			if(fList.get(positions).getOpentype()!=null&&fList.get(positions).getOpentype()!="")
			{
				int _type=Integer.parseInt(fList.get(positions).getOpentype());
				switch(_type)
				{
				  case 1: OpenNativeIntent(); break;
				  case 2:break;
				  case 3: OpenHtml5Intent(fList.get(positions));break;
				  default:OpenNativeIntent();break;
				}
			} 
			else
			{
				//??????????????????Native
				OpenNativeIntent();
			} 
		}
	};
	
	//??????Native???????????????
	private void OpenNativeIntent()
	{ 
		Intent mIntent = new Intent(getActivity(), ApprovedInfolActivity.class);
		mIntent.putExtra("SN", fList.get(positions).getSN());
		mIntent.putExtra("folio", fList.get(positions).getFolio());
		mIntent.putExtra("checkServer", 1);
		mIntent.putExtra("destination", fList.get(positions).getDestination()); 
		mIntent.putExtra("processFullName", fList.get(positions).getProcessFullName());
		mIntent.putExtra("bsCode", fList.get(positions).getBsCode());
		startActivity(mIntent);
	}

	//??????HTML5???????????????
	private void OpenHtml5Intent(FlowBean Model)	
	{
		Intent mIntent = new Intent(getActivity(), HRStaticLinkListActivity.class);
		mIntent.putExtra("title", Model.getFolio());
		mIntent.putExtra("url", Model.getLinkurl());
		startActivity(mIntent);
		return;
	}
	
	/**
	* @Title: onLoad
	* @Description: ?????????????????????
	* @return String ??????????????? 
	* @throws
	*/
	private void onLoad() {
		lv_work_show.stopRefresh();
		lv_work_show.stopLoadMore();
		lv_work_show.setRefreshTime(getResources().getString(R.string.just));
	}

	/**
	* @Title: requestServer
	* @Description: ??????????????????
	* @return void
	* @throws
	*/
	private void requestServer(){
		// ????????????????????????
		if (!NetWorkUtil.isNetworkAvailable(getActivity())) {
			DialogUtil.showLongToast(getActivity(), R.string.global_network_disconnected);
		}else{
			// ??????????????????
			String code = null;
			if(1 == operClass){
				code = "F60000003";
			}else if(2 == operClass){
				code = "F60000004";
			}
			
			String info = getLoginInfo(code);
			SendRequest.submitRequest(getActivity(), info, submitCallBack);
		}
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
			if (1 == flag) {
				DialogUtil.showWithCancelProgressDialog(getActivity(), null, getResources().getString(R.string.global_prompt_message), null);
			}else{
				onLoad();
			}
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			if (1 == flag) {
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
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
					return;
				}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
					LogUtil.promptInfo(getActivity(), resMsg.getResHeader().getReturnMsg());	
					return;
				}
				// ???????????????????????????
				if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
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
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
			}		
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			if (1 == flag) {
				DialogUtil.closeDialog();
			}else{
				onLoad();
			}
		}
	};
	
	/**
	 * ?????????: MyOnPageChangeListener
	 * ??? ??? : ???????????????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(final int position) {
			
		}
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			 
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			 
		}
	}
	
	@Override
	public void onDestroy() {
		if(null != iReceiver)
			getActivity().unregisterReceiver(iReceiver);
		super.onDestroy();
	}
	private int pageFlag = 1;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_fiction:
				switchBg(1);
				operClass = 1;
				pageIndex = 1;
				pageFlag = 1;
				lv_work_show.setTips(tipsQuasi);
				flag  = 1;
				fList.clear();
				mSearch.setText("");
				fList.addAll(ficList);
				initAdapter(1);
				break;
			case R.id.tv_review:
				switchBg(2);
				operClass = 2;
				pageIndex = 1;
				pageFlag = 2;
				mSearch.setText("");
				lv_work_show.setTips(tipsAudit);
				flag  = 2;
				if(1 > apiCount){
					apiCount++;
//					requestServer();
					doubleClickRefresh();
				}else{
					fList.clear();
					fList.addAll(apiList);
					initAdapter(2);
				}
				break;
		}
	}
	/**
	 * ?????????: switchBg() 
	 * ??? ??? : ????????????
	 * ??? ??? : val ???????????????????????? 
	 * ?????????: void
	 */
	private void switchBg(int val){
		switch (val) {
			case 1:
				if (Build.VERSION.SDK_INT < 16) {
					tv_fiction.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
					tv_review.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	tv_fiction.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            	tv_review.setBackground(getResources().getDrawable(R.color.white));
	            }
				tv_fiction.setTextColor(getResources().getColor(R.color.white));
				tv_review.setTextColor(getResources().getColor(R.color.black));
				break;
			case 2:
				if (Build.VERSION.SDK_INT < 16) {
					tv_fiction.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					tv_review.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
	            } else {
	            	tv_fiction.setBackground(getResources().getDrawable(R.color.white));
	            	tv_review.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            }
				tv_review.setTextColor(getResources().getColor(R.color.white));
				tv_fiction.setTextColor(getResources().getColor(R.color.black));
				break;
			case 3:
				if (Build.VERSION.SDK_INT < 16) {
					tv_fiction.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					tv_review.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	tv_fiction.setBackground(getResources().getDrawable(R.color.white));
	            	tv_review.setBackground(getResources().getDrawable(R.color.white));
	            }
				tv_fiction.setTextColor(getResources().getColor(R.color.black));
				tv_review.setTextColor(getResources().getColor(R.color.black));
				break;
		}
	}
}