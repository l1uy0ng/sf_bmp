package com.k2.mobile.app.controller.activity.menu.application;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.HttpConstants;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.config.MenuConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.menu.addressList.ContactslActivity;
import com.k2.mobile.app.controller.activity.menu.airTicketReservation.AirTicketReservationActivity;
import com.k2.mobile.app.controller.activity.menu.benefitLife.BenefitLifeActivity;
import com.k2.mobile.app.controller.activity.menu.calendar.MyCalendarActivity;
import com.k2.mobile.app.controller.activity.menu.companyNew.ApprovalProcessActivity;
import com.k2.mobile.app.controller.activity.menu.companyNew.NewsActivity;
import com.k2.mobile.app.controller.activity.menu.consumption.PersonalConsumptionQueryActivity;
import com.k2.mobile.app.controller.activity.menu.dormitory.DormitoryRepairActivity;
import com.k2.mobile.app.controller.activity.menu.feedback.FeedbackActivity;
import com.k2.mobile.app.controller.activity.menu.feedback.TroubleshootingActivity;
import com.k2.mobile.app.controller.activity.menu.hr.HRHelperActivity;
import com.k2.mobile.app.controller.activity.menu.hr.HRStaticLinkListActivity;
import com.k2.mobile.app.controller.activity.menu.reimbursement.ExpenseClaimsActivity;
import com.k2.mobile.app.controller.activity.menu.task.MyTaskActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.CheeseDynamicAdapter;
import com.k2.mobile.app.model.bean.HomeMenuBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.db.DBHelper;
import com.k2.mobile.app.model.db.table.MenuTable;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.view.dynamicgrid.DynamicGridView;
import com.k2.mobile.app.view.dynamicgrid.DynamicGridView.OnDragListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @Title CommonFragment.java
 * @Package com.oppo.mo.controller.activity.fragment
 * @Description ???????????????
 * @Company  K2
 * 
 * @author linqijun
 * @date 2015-04-22 20:45:03
 * @version V1.0
 */
public class CommonFragment extends Fragment implements OnClickListener, OnItemClickListener, OnItemLongClickListener, OnDragListener{
	
	private View view;
	
	private CheeseDynamicAdapter mAdapter;
	private boolean isShowDelete = true;	// ????????????????????????
	private boolean isSort = false;		// ????????????
	private List<HomeMenuBean> hList = null;
	private String action;
	private Intent mIntent;
	private UpdateReceiver uReceiver;
	
	private DynamicGridView gv_common;
	protected Resources res;
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			DialogUtil.closeDialog();
			switch (msg.what) {
			case 1:
				String json = (String)msg.obj;
				if (null == json && "".equals(json.trim())) {
					break;
				}
				
				hList.clear();
				List<HomeMenuBean> hbList = JSON.parseArray(json, HomeMenuBean.class);
				if(null != hbList) {
					for(int i = 0; i<hbList.size(); i++) {
						HomeMenuBean hmBean = hbList.get(i);
						if(null != hmBean.getIsBlank() && "false".equals(hmBean.getIsBlank())){
							for(int j=0; j<MenuConstants.menuCode.length; j++) {
								if (MenuConstants.menuCode[j].contains(hmBean.getMenuCode().trim())) {
									if (hmBean.getMenuCode().trim().equals(MenuConstants.menuCode[j])) {
										hmBean.setMenuIcons(MenuConstants.icons[j]);
										hmBean.setMenuNmae(getActivity().getString(MenuConstants.names[j]));
									}
								}
							}
							hList.add(hmBean);
						}else if(null != hmBean.getIsBlank() && "true".equals(hmBean.getIsBlank())){
							hmBean.setMenuNmae(hmBean.getMenuChsName());
							hList.add(hmBean);
						}
					}
				}
				
				setAddMenu();
				
				// ??????????????????????????????????????????
				List<MenuTable> mtList = BaseApp.db.customQuery(MenuTable.class, "userAccount", BaseApp.user.getUserId());
				if(null != mtList && 0 < mtList.size()){
					List<HomeMenuBean> tmpList = new ArrayList<HomeMenuBean>();
					for(int i=0; i<mtList.size(); i++){
						for(int j=0; j<hList.size(); j++){
							if(mtList.get(i).getMenuCode().equals(hList.get(j).getMenuCode())){
								tmpList.add(hList.get(j));
							}
						}
					}
					
					hList.removeAll(tmpList);
					List<HomeMenuBean> nList = new ArrayList<HomeMenuBean>();
					for(int i=0; i<hList.size(); i++){
						HomeMenuBean nBean = hList.get(i);
						nList.add(nBean);
					}
					hList.clear();
					
					hList.addAll(tmpList);
					hList.addAll(nList);
				}
				
				initAdapter();
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_common, null);
        initView();
		initListener();
		createFilter();
		initDB();
		return view;
	}
	
	/*
	 * ?????????: initView()  
	 * ??? ??? : ????????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		
		gv_common = (DynamicGridView) view.findViewById(R.id.gv_common);
		hList = new ArrayList<HomeMenuBean>();
		res = getResources();
		// ??????????????????
		if (!NetWorkUtil.isNetworkAvailable(getActivity())) {            
			DialogUtil.showLongToast(getActivity(), R.string.global_network_disconnected);
		}else{
			String json = remoteDataRequest();
			if(null != json){
				SendRequest.submitRequest(getActivity(), json, submitCallBack);
			}
		}
	}
		
	/*
	 * ?????????: initListener()  
	 * ??? ??? : ???????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		gv_common.setOnItemLongClickListener(this);
		gv_common.setOnItemClickListener(this);
		gv_common.setOnDragListener(this);
	}
	
	private void initAdapter(){
		mAdapter = new CheeseDynamicAdapter(getActivity(), hList, getResources().getInteger(R.integer.column_count));
		gv_common.setAdapter(mAdapter);	
	}
	
	/**
	 * @Title: createFilter
	 * @Description: ??????IntentFilter
	 * @param void
	 * @return void 
	 * @throws
	 */
	private void createFilter() {
		uReceiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastNotice.ADD_UPDATE_COMMOM);
		filter.addAction(BroadcastNotice.HOME_DEL_UPDATE);
		getActivity().registerReceiver(uReceiver, filter);	// ????????????
	}
	
	private void setAddMenu(){
		// ????????????
		HomeMenuBean hBean = new HomeMenuBean();
		hBean.setMenuNmae(getString(MenuConstants.names[MenuConstants.menuCode.length - 1]));
		hBean.setMenuIcons(MenuConstants.icons[MenuConstants.menuCode.length - 1]);
		hBean.setMenuCode(MenuConstants.menuCode[MenuConstants.menuCode.length - 1]);
		hBean.setMenuType("1");
		hBean.setIsBlank("false");
		hList.add(hBean);
	}
	
	/**
	 * ????????????
	 */
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(null == intent){
				return;
			}
			
			String action = intent.getAction();
			if(BroadcastNotice.ADD_UPDATE_COMMOM.equals(action)){
				ArrayList<HomeMenuBean> bList = (ArrayList<HomeMenuBean>)intent.getExtras().getSerializable("updateCommon");
				if(null == bList){
					return;
				}
				
				boolean chVal = false;
				if (HttpConstants.ADD.equals(hList.get(hList.size() - 1).getMenuCode())){
					hList.remove(hList.size() - 1);
					chVal = true;
				}
				
				for (int i=0; i<bList.size(); i++) {
					boolean bl = true;
					for (int j=0; j < hList.size(); j++) {
						if (bList.get(i).getMenuCode().equals(hList.get(j).getMenuCode())) {
							bl = false;
						}
					}
					if (bl) {
						hList.add(bList.get(i));
					}
				}
				
				if(chVal){
					// ????????????
					setAddMenu();
				}
				
				initAdapter();
			}else if(BroadcastNotice.HOME_DEL_UPDATE.equals(action)){
				int positions = intent.getIntExtra("position", -1);
				if(-1 != positions && null != hList &&positions < hList.size()){
					// ??????
					BaseApp.db.delByColumnName(MenuTable.class, "menuCode", hList.get(positions).getMenuCode());
					hList.remove(positions);
				}
				
				int valCount = 0;
				for(HomeMenuBean hmBean : hList){
					if(null != hmBean && null != hmBean.getMenuType() && !"1".equals(hmBean.getMenuType().trim())){
						valCount++;
					}
				}
				
				if(0 == valCount){
					setStart();
				}
			}
		} 
	} 
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(uReceiver);
		super.onDestroy();
	}
	
	/**
	 * ?????????: initDB() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initDB() {
		
		if (BaseApp.db == null) {
			BaseApp.db = new DBHelper(getActivity().getApplicationContext());
		}

		BaseApp.db.createTable(MenuTable.class);
	}
	
	/**
	 * ?????????: remoteDataRequest()  
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: String JSON????????????
	 */
	private String remoteDataRequest() {
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F20000002");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/*
	 * ?????????: setStart()  
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	public boolean setStart() {
		if (gv_common.isEditMode()) {
			 gv_common.stopEditMode();
         }
		
		if(isSort){
			BaseApp.db.delByColumnName(MenuTable.class, "userAccount", BaseApp.user.getUserId());
			for (HomeMenuBean hmBean : hList) {
				MenuTable mTable = new MenuTable();
				mTable.setMenuCode(hmBean.getMenuCode());
				mTable.setMenuNmae(hmBean.getMenuNmae());
				mTable.setUserAccount(BaseApp.user.getUserId());
				BaseApp.db.save(mTable);
			}
			
			isSort = false;
		}
		
		boolean val = isShowDelete;
		isShowDelete = false;
		if(null != mAdapter){
			mAdapter.setIsShowDelete(isShowDelete);
		}
		isShowDelete = true;
		return val;
	}
	
	@Override
	public void onPause() {
		setStart();
		super.onPause();
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomeMenuBean bean = hList.get(position);
		if(null != bean.getIsBlank() && "true".equals(bean.getIsBlank().trim())){
			Intent mIntent = new Intent(getActivity(), HRStaticLinkListActivity.class);
			mIntent.putExtra("title", bean.getMenuChsName());
			mIntent.putExtra("url", bean.getMenuUrl());
			startActivity(mIntent);
			return;
		}
		
		if (isShowDelete) {
			if (HttpConstants.COMPANY_NEW.equals(bean.getMenuCode())) {				// ????????????
				startActivity(NewsActivity.class);
			} else if (HttpConstants.APPROVAL_DOCUMENTS.equals(bean.getMenuCode())) {	// ????????????
				startActivity(ApprovalProcessActivity.class);
			} else if (HttpConstants.MY_CONTACTS.equals(bean.getMenuCode())) {		// ???????????????
				startActivity(ContactslActivity.class);
			} else if (HttpConstants.MY_TASK.equals(bean.getMenuCode())) {			// ????????????
				startActivity(MyTaskActivity.class);
			} else if (HttpConstants.MY_CALENDAR.equals(bean.getMenuCode())) {		// ????????????
				startActivity(MyCalendarActivity.class);
			} else if (HttpConstants.HR_HELPER.equals(bean.getMenuCode())) {		// HR??????
				startActivity(HRHelperActivity.class);
			} else if (HttpConstants.LOGSVC.equals(bean.getMenuCode())) {			// ????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.MY_REIMBURSEMENT.equals(bean.getMenuCode())) {			// ????????????
				startActivity(ExpenseClaimsActivity.class);
			} else if (HttpConstants.MY_RECORDS_CONSUMPTION.equals(bean.getMenuCode())) {	// ????????????
				startActivity(PersonalConsumptionQueryActivity.class);
			} else if (HttpConstants.OIA_FORUM.equals(bean.getMenuCode())) {			// OIA??????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.MODULE_RESPONSIBLE.equals(bean.getMenuCode())) {	// ???????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.IT.equals(bean.getMenuCode())) {					// IT??????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.FEEDBACK.equals(bean.getMenuCode())) {				// ????????????
				startActivity(FeedbackActivity.class);
			} else if (HttpConstants.HANDLING_PROBLE.equals(bean.getMenuCode())) {		// ????????????
				startActivity(TroubleshootingActivity.class);
			} else if (HttpConstants.LEAVE_FLOW.equals(bean.getMenuCode())) {			// ??????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.WORK_OVERTIME_FLOW.equals(bean.getMenuCode())) {	// ????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.BUSINESS_TRAVEL_FLOW.equals(bean.getMenuCode())) {	// ????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.ACCESS_FLOW.equals(bean.getMenuCode())) {			// ????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.QUIT_FLOW.equals(bean.getMenuCode())) {			// ????????????
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.DREPAIR.equals(bean.getMenuCode())) {				// ????????????
				startActivity(DormitoryRepairActivity.class);
			} else if (HttpConstants.DMAINTENANCE.equals(bean.getMenuCode())) {			// ????????????
//				startActivity(DormitoryMaintenanceActivity.class);
				LogUtil.promptInfo(getActivity(), "??????????????????????????????");
			} else if (HttpConstants.BENEFIT_LIFE.equals(bean.getMenuCode())) {			// ?????????
				startActivity(BenefitLifeActivity.class);
			} else if (HttpConstants.GHOTEL.equals(bean.getMenuCode())) {				// ????????????
			
			} else if (HttpConstants.BANDS.equals(bean.getMenuCode())) {				// ????????????
			
			} else if (HttpConstants.SAFETY.equals(bean.getMenuCode())) {				// ????????????
				 
			} else if (HttpConstants.CERTIFICATE.equals(bean.getMenuCode())) {			// ????????????
				 
			} else if (HttpConstants.BUS_TIMETABLE.equals(bean.getMenuCode())) {		// ???????????????
				 
			} else if (HttpConstants.SPORTS.equals(bean.getMenuCode())) {				// ????????????
				 
			} else if (HttpConstants.NETWORK.equals(bean.getMenuCode())) {				// ????????????
				 
			} else if (HttpConstants.PLANE_TICKET_RESERVE.equals(bean.getMenuCode())) {	// ????????????
				startActivity(AirTicketReservationActivity.class);
			}else if (HttpConstants.ADD.equals(bean.getMenuCode())) {					// ??????
				if(null == BaseApp.hList){
					BaseApp.hList = new ArrayList<HomeMenuBean>();
				}else{
					BaseApp.hList.clear();
				}
				
				BaseApp.hList.addAll(hList);
				setStart();
				startActivity(AddCommonMenu.class);
			}
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		
		if(1 >= hList.size()){
			return true;
		}
		
		gv_common.startEditMode(position);
		
		mAdapter.setIsShowDelete(isShowDelete);
		if (isShowDelete) {
			isShowDelete = false;
		} else {
			isShowDelete = true;
		}
		
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		}
	}
	
	/**
	 * @Title: startActivity
	 * @Description: ??????Activity
	 * @return void
	 * @throws
	 */
	public void startActivity(Class<?> activity) {
		Intent intent = new Intent(getActivity(), activity);
		startActivity(intent);
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			DialogUtil.closeDialog();
			String result = responseInfo.result.toString();
			byte[] ebase64 = EncryptUtil.decodeBase64(result);
			System.out.println("common = "+new String(ebase64));
			if (null != result && !"".equals(result.trim())) {
				PublicResultBean resMsg = JSON.parseObject(new String(ebase64), PublicResultBean.class);
				// ????????????????????????????????????
				if (null == resMsg || null == resMsg.getResHeader().getStateCode() || "".equals(resMsg.getResHeader().getStateCode())) {				
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
					return;
				// ???????????????
				}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
					LogUtil.promptInfo(getActivity(), resMsg.getResHeader().getReturnMsg());	
					return;
				}
				// ???????????????????????????
				if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
					hList.clear();
					setAddMenu();
					initAdapter();
				} else {
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
			if(msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("1207", getActivity().getResources()));
			}else{
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getActivity().getResources()));
			}
		}
	};

	@Override
	public void onDragStarted(int position) {				// ??????????????????
//		 System.out.println("drag started at position " + position);
	}

	@Override
	public void onDragPositionsChanged(int oldPosition, int newPosition) {		// ?????????????????????????????????
		
		HomeMenuBean oldBean = hList.get(oldPosition);
		
		if(0 < (oldPosition - newPosition)){		// ????????????
			ArrayList<HomeMenuBean> tmpList = new ArrayList<HomeMenuBean>();
			for(int i = newPosition; i < oldPosition; i++){
				tmpList.add(hList.get(i));
			}
						
			hList.set(newPosition, oldBean);
			
			for(int i = 0; i < tmpList.size(); i++){
				HomeMenuBean tmpBean = tmpList.get(i);
				hList.set(newPosition+i+1, tmpBean);
			}
		}else{										// ????????????
			for(int i = oldPosition; i <= newPosition; i++){
				if(i == newPosition){
					hList.set(newPosition, oldBean);
				}else{
					HomeMenuBean tmpBean = hList.get(i+1);;
					hList.set(i, tmpBean);
				}
			}
		}
		
		isSort = true;
	}
}
