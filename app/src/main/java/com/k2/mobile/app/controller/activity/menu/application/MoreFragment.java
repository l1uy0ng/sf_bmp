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
import com.k2.mobile.app.model.adapter.MyGridViewAddAdapter;
import com.k2.mobile.app.model.bean.HomeMenuBean;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.bean.ResPublicBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @Title MoreFragment.java
 * @Package com.oppo.mo.controller.activity.fragment
 * @Description ???????????????
 * @Company  K2
 * 
 * @author linqijun
 * @date 2015-04-22 20:45:03
 * @version V1.0
 */
@SuppressLint("NewApi")
public class MoreFragment extends Fragment implements OnClickListener, OnItemClickListener {
	
	private View view;
	// ????????? ??????
	private TextView tv_select, tv_confirm;
	private TextView tv_personal, tv_service, tv_process, tv_other;
	private LinearLayout ll_personal, ll_service, ll_process, ll_other, ll_tips;
	// ??????????????????
	private GridView gridview;
	private LinearLayout ll_bottom;
	private RelativeLayout rl_item;
	private MyGridViewAddAdapter mAdapter;
	// ?????????????????????????????????
	private List<HomeMenuBean> allList = null;
	// ??????????????????????????????
	private List<HomeMenuBean> hList = null;
	// ?????????????????????????????????
	private List<HomeMenuBean> tmpList = null;
	private int flag = 0;
	private int showView = 1;
	// CheckBox????????????????????????1?????????0??????
	private int type = 0;
	// ??????????????????
	private String pCode = HttpConstants.PERSONAL_OFFICE;
	// ????????????
	private int operType = 2;
	private String action;
	private Intent mIntent;
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 1:
				String json = (String) msg.obj;
				if (null != json) {
					ResPublicBean bean = JSON.parseObject(json, ResPublicBean.class);
					if(null != bean.getSuccess() && "1".equals(bean.getSuccess())){
						DialogUtil.showLongToast(getActivity(), R.string.add_menu_successd);
						ArrayList<HomeMenuBean> addList = new ArrayList<HomeMenuBean>();
						for (int i = 0; i < allList.size(); i++) {
							if (1 == allList.get(i).getIsChoose()) {
								addList.add(allList.get(i));
							}
						}
						action = BroadcastNotice.ADD_UPDATE_COMMOM;
						mIntent = new Intent(action);
						mIntent.putExtra("updateCommon", addList);
						// ?????????????????????UI
						getActivity().sendBroadcast(mIntent);
					} else {
						DialogUtil.showLongToast(getActivity(), R.string.add_failed);
					}
				}
				break;
				
			case 2:
				String jsons = (String) msg.obj;
				if(null != jsons){
					allList.clear();
					List<HomeMenuBean> hListSer = JSON.parseArray(jsons, HomeMenuBean.class);
					if(null != hListSer){
						allList.addAll(hListSer);
					}
				}
				checkMenu();
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_more, null);
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
		tv_select = (TextView) view.findViewById(R.id.tv_select);
		tv_confirm = (TextView) view.findViewById(R.id.tv_confirm);
		
		//ll_personal = (LinearLayout) view.findViewById(R.id.ll_personal);
		//ll_service = (LinearLayout) view.findViewById(R.id.ll_service);
		ll_process = (LinearLayout) view.findViewById(R.id.ll_process); 
		//ll_other = (LinearLayout) view.findViewById(R.id.ll_other);
		ll_tips = (LinearLayout) view.findViewById(R.id.ll_tips);
		
		//tv_personal = (TextView) view.findViewById(R.id.tv_personal);
		//tv_service = (TextView) view.findViewById(R.id.tv_service);
		tv_process = (TextView) view.findViewById(R.id.tv_process); 
		//tv_other = (TextView) view.findViewById(R.id.tv_other);
		
		rl_item = (RelativeLayout) view.findViewById(R.id.ll_item);
		ll_bottom = (LinearLayout) view.findViewById(R.id.ll_bottom);
		gridview = (GridView) view.findViewById(R.id.gv_more);
		
		allList = new ArrayList<HomeMenuBean>();
		hList = new ArrayList<HomeMenuBean>();


		switchBg();
	}

	/**
	 * @Title: checkMenu
	 * @Description: ????????????
	 * @param operType ???????????????1?????????2??????
	 * @return void ??????????????? 
	 * @throws
	 */
	private void checkMenu() {
		hList.clear();
		for (int i = 0; i<allList.size(); i++) {
			HomeMenuBean hmBean = allList.get(i);
			if(null != hmBean.getIsBlank() && "false".equals(hmBean.getIsBlank())){
				for(int j=0; j<MenuConstants.menuCode.length; j++) {
					if (MenuConstants.menuCode[j].contains(hmBean.getMenuCode().trim()) && pCode.equals(hmBean.getParentCode().trim())) {
						if (hmBean.getMenuCode().trim().equals(MenuConstants.menuCode[j])) {
							hmBean.setMenuIcons(MenuConstants.icons[j]);
							hmBean.setMenuNmae(getActivity().getString(MenuConstants.names[j]));
						}
						hList.add(hmBean);
					}
				}
			}else if(null != hmBean.getIsBlank() && "true".equals(hmBean.getIsBlank())){
				if(pCode.equals(hmBean.getParentCode().trim())){
					hmBean.setMenuNmae(hmBean.getMenuChsName());
					hList.add(hmBean);
				}
			}
		}
		
		// ?????????????????????????????????????????????????????????????????? 
		if(hList.size() < 1){
			ll_tips.setVisibility(View.VISIBLE);
		}else{
			ll_tips.setVisibility(View.GONE);
		}
				
		if(1 > hList.size()){
			tv_select.setVisibility(View.GONE);
			tv_confirm.setVisibility(View.GONE);
		}else{
			if(1 == type){
				tv_select.setVisibility(View.VISIBLE);
				tv_confirm.setVisibility(View.VISIBLE);
			}
		}
		
		mAdapter = new MyGridViewAddAdapter(getActivity(), hList, type, tmpList);
		gridview.setAdapter(mAdapter);
	}
	
	/**
	 * @Title: operMenu
	 * @Description: ????????????????????????????????????
	 * @param operType ???????????????1?????????2??????
	 * @return void ??????????????? 
	 * @throws
	 */
	public void operMenu(String info) {
		if (!NetWorkUtil.isNetworkAvailable(getActivity())) {            // ????????????????????????
			DialogUtil.showLongToast(getActivity(), R.string.global_network_disconnected);
		}
		if (null != info) {
			SendRequest.submitRequest(getActivity(), info, submitCallBack);
		}
	}
	
	/**
	 * ?????????: addMenu()  
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: String JSON????????????
	 */
	private String addMenuJson() {
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		
		List<String> mList = new ArrayList<String>();
		for (int i = 0; i < allList.size(); i++) {
			if (1 == allList.get(i).getIsChoose()) {
				mList.add(allList.get(i).getMenuCode());
			}
		}

		if(0 < mList.size()){
			bean.setMenuCode(mList);
		}else{
			DialogUtil.showLongToast(getActivity(), R.string.select_menu);
			return null;
		}
		
		bean.setMenuType("2");
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * ?????????: getMenu()  
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: String JSON????????????
	 */
	public String getMenu() {
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F20000001");
		bBean.setInvokeParameter("[\"24198966f5104d4dba76b8e3d64a9468\"]");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/*
	 * ?????????: initListener()  
	 * ??? ??? : ???????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		//ll_personal.setOnClickListener(this);
		//ll_service.setOnClickListener(this);
		ll_process.setOnClickListener(this);
		//ll_other.setOnClickListener(this);
				
		tv_select.setOnClickListener(this);
		tv_confirm.setOnClickListener(this);
		rl_item.setOnClickListener(this);
		gridview.setOnItemClickListener(this);
	}
	
	public int getType(){
		return type;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_item:
			break;
		case R.id.tv_select:
			int tmp = 0;
			if(null != tmpList){
				for(int i=0; i<hList.size(); i++){
					for(int j=0;j<tmpList.size();j++){
						if(null != hList.get(i).getMenuCode() && null != tmpList.get(j).getMenuCode() 
								&& hList.get(i).getMenuCode().equals(tmpList.get(j).getMenuCode())){
							tmp++;
						}
					}
				}
			}
			
			if(tmp == hList.size()){
				LogUtil.promptInfo(getActivity(), getString(R.string.exists_common_menu));
				break;
			}
			
			if (flag == 0) {
				selectAll();
				tv_select.setText(getString(R.string.global_cancel));
				flag = 1;
			} else {
				selectNone();
				tv_select.setText(getString(R.string.select_all));
				flag = 0;
			}
			
			break;
		case R.id.tv_confirm:	// ??????????????????
			getChooseItem();
			int tmps = 0;
			for(int i=0;i<allList.size(); i++){
				if(1 == allList.get(i).getIsChoose()){
					tmps++;
				}
			}
			
			if(0 == tmps){
				break;
			}
			
			String info = addMenuJson();
			operMenu(info);
			break;
//		case R.id.ll_personal:
//			if(1 == type){
//				getChooseItem();
//			}
//
//			if(1 == flag){
//				selectNone();
//				tv_select.setText(getString(R.string.select_all));
//				flag = 0;
//			}
//			showView = 1;
//			switchBg();
//			pCode = HttpConstants.PERSONAL_OFFICE;
//			checkMenu();
//			if(1 == type){
//				reductionChoose();
//			}
//			break;
//		case R.id.ll_service:
//			if(1 == type){
//				getChooseItem();
//			}
//			if(1 == flag){
//				selectNone();
//				tv_select.setText(getString(R.string.select_all));
//				flag = 0;
//			}
//			showView = 2;
//			switchBg();
//			pCode = HttpConstants.SERVICE_SUPPORT;
//			checkMenu();
//			if(1 == type){
//				reductionChoose();
//			}
//			break;
		case R.id.ll_process:
			if(1 == type){
				getChooseItem();
			}
			if(1 == flag){
				selectNone();
				tv_select.setText(getString(R.string.select_all));
				flag = 0;
			}
			showView = 3;
			switchBg();
			pCode = HttpConstants.PROCESS_ENTRANCE;
			checkMenu();
			if(1 == type){
				reductionChoose();
			}
			break;
//		case R.id.ll_other:
//			if(1 == type){
//				getChooseItem();
//			}
//			if(1 == flag){
//				selectNone();
//				tv_select.setText(getResources().getString(R.string.select_all));
//				flag = 0;
//			}
//			showView = 4;
//			switchBg();
//			pCode = HttpConstants.OTHER_MENU;
//			checkMenu();
//			if(1 == type){
//				reductionChoose();
//			}
//			break;
		}
	}
	
	/**
	 * ?????????: switchBg() 
	 * ??? ??? : ????????????
	 * ??? ??? : val ???????????????????????? 
	 * ?????????: void
	 */
	private void switchBg(){
		switch (showView) {
			case 1:
				if (Build.VERSION.SDK_INT < 16) {
					//tv_personal.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
					//tv_service.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					tv_process.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					//tv_other.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	//tv_personal.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            	//tv_service.setBackground(getResources().getDrawable(R.color.white));
	            	tv_process.setBackground(getResources().getDrawable(R.color.white));
	            	//tv_other.setBackground(getResources().getDrawable(R.color.white));
	            }
				//tv_personal.setTextColor(getResources().getColor(R.color.white));
				//tv_service.setTextColor(getResources().getColor(R.color.main_tv_font));
				tv_process.setTextColor(getResources().getColor(R.color.main_tv_font));
				//tv_other.setTextColor(getResources().getColor(R.color.main_tv_font));
				break;
			case 2:
				if (Build.VERSION.SDK_INT < 16) {
					//tv_service.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
					//tv_personal.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					tv_process.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					//tv_other.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	//tv_service.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            	//tv_personal.setBackground(getResources().getDrawable(R.color.white));
	            	tv_process.setBackground(getResources().getDrawable(R.color.white));
	            	//tv_other.setBackground(getResources().getDrawable(R.color.white));
	            }
				//tv_service.setTextColor(getResources().getColor(R.color.white));
				//tv_personal.setTextColor(getResources().getColor(R.color.main_tv_font));
				tv_process.setTextColor(getResources().getColor(R.color.main_tv_font));
				//tv_other.setTextColor(getResources().getColor(R.color.main_tv_font));
				break;
			case 3:
				if (Build.VERSION.SDK_INT < 16) {
					tv_process.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
					//tv_service.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					//tv_personal.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					//tv_other.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	tv_process.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            	//tv_service.setBackground(getResources().getDrawable(R.color.white));
	            	//tv_personal.setBackground(getResources().getDrawable(R.color.white));
	            	//tv_other.setBackground(getResources().getDrawable(R.color.white));
	            }
				tv_process.setTextColor(getResources().getColor(R.color.white));
				//tv_service.setTextColor(getResources().getColor(R.color.main_tv_font));
				//tv_personal.setTextColor(getResources().getColor(R.color.main_tv_font));
				//tv_other.setTextColor(getResources().getColor(R.color.main_tv_font));
				break;
			case 4:
				if (Build.VERSION.SDK_INT < 16) {
					//tv_other.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
					//tv_service.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					tv_process.setBackgroundDrawable(getResources().getDrawable(R.color.white));
					//tv_personal.setBackgroundDrawable(getResources().getDrawable(R.color.white));
	            } else {
	            	//tv_other.setBackground(getResources().getDrawable(R.drawable.tab_bg));
	            	//tv_service.setBackground(getResources().getDrawable(R.color.white));
	            	tv_process.setBackground(getResources().getDrawable(R.color.white));
	            	//tv_personal.setBackground(getResources().getDrawable(R.color.white));
	            }
				//tv_other.setTextColor(getResources().getColor(R.color.white));
				//tv_service.setTextColor(getResources().getColor(R.color.main_tv_font));
				tv_process.setTextColor(getResources().getColor(R.color.main_tv_font));
				//tv_personal.setTextColor(getResources().getColor(R.color.main_tv_font));
				break;
		}
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomeMenuBean bean = hList.get(position);
		System.out.println("position = "+position + ", type = "+type+", bean.getIsBlank() = "+bean.getIsBlank());
		if(1 == type){
			return; 
		}else if(null != bean.getIsBlank() && "true".equals(bean.getIsBlank().trim())){
			Intent mIntent = new Intent(getActivity(), HRStaticLinkListActivity.class);
			mIntent.putExtra("title", bean.getMenuChsName());
			mIntent.putExtra("url", bean.getMenuUrl());
			startActivity(mIntent);
			return;
		}
		
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
		} else if (HttpConstants.DREPAIR.equals(bean.getMenuCode())) {				// ????????????
			startActivity(DormitoryRepairActivity.class);
		} else if (HttpConstants.DMAINTENANCE.equals(bean.getMenuCode())) {			// ????????????
//			startActivity(DormitoryMaintenanceActivity.class);
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.BENEFIT_LIFE.equals(bean.getMenuCode())) {			// ?????????
			// ?????????
			startActivity(BenefitLifeActivity.class);
		} else if (HttpConstants.GHOTEL.equals(bean.getMenuCode())) {				// ????????????
			// ????????????
		} else if (HttpConstants.BANDS.equals(bean.getMenuCode())) {				// ????????????
			// ????????????
		} else if (HttpConstants.SAFETY.equals(bean.getMenuCode())) {				// ????????????
			// ????????????
		} else if (HttpConstants.CERTIFICATE.equals(bean.getMenuCode())) {			// ????????????
			// ????????????
		} else if (HttpConstants.BUS_TIMETABLE.equals(bean.getMenuCode())) {		// ???????????????
			// ???????????????
		} else if (HttpConstants.SPORTS.equals(bean.getMenuCode())) {				// ????????????
			// ????????????
		} else if (HttpConstants.NETWORK.equals(bean.getMenuCode())) {				// ????????????
			// ????????????
		}else if (HttpConstants.LEAVE_FLOW.equals(bean.getMenuCode())) {			// ??????
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.WORK_OVERTIME_FLOW.equals(bean.getMenuCode())) {	// ????????????
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.BUSINESS_TRAVEL_FLOW.equals(bean.getMenuCode())) {	// ????????????
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.ACCESS_FLOW.equals(bean.getMenuCode())) {			// ????????????
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.QUIT_FLOW.equals(bean.getMenuCode())) {			// ????????????
			LogUtil.promptInfo(getActivity(), "??????????????????????????????");
		} else if (HttpConstants.PLANE_TICKET_RESERVE.equals(bean.getMenuCode())) {	// ????????????
			startActivity(AirTicketReservationActivity.class);
		}
	}
	
	/** ?????? */
	private void selectAll() {
		for (int i = 0; i < hList.size(); i++) {
			// ???????????????????????????????????????????????????????????????
			boolean val = true;
			if(null != tmpList){
				for(int j=0;j<tmpList.size();j++){
					if(null != hList.get(i).getMenuCode() && null != tmpList.get(j).getMenuCode() 
							&& hList.get(i).getMenuCode().equals(tmpList.get(j).getMenuCode())){
						val = false;
					}
				}
			}
			
			MyGridViewAddAdapter.getIsSelected().put(i, val);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	/** ?????? */
	private void selectNone() {
		for (int i = 0; i < hList.size(); i++) {
			MyGridViewAddAdapter.getIsSelected().put(i, false);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * ??????????????? - CheckBox
	 */
	public void showCheckBox() {
		type = 1;
		
		for(int i=0; i<allList.size(); i++){
			allList.get(i).setIsChoose(0);
		}
		
		if(null == pCode || "".equals(pCode)){
			pCode = HttpConstants.PERSONAL_OFFICE;
		}
		
		tmpList = BaseApp.hList;
		
		checkMenu();
		
		if(1 == flag){
			selectNone();
			tv_select.setText(getString(R.string.select_all));
			flag = 0;
		}
	}
	
	/**
	 * ??????????????? - CheckBox
	 */
	public void hidCheckBox() {
		type = 0;
		checkMenu();
		for(int i=0; i<allList.size(); i++){
			allList.get(i).setIsChoose(0);
		}
	}
	/**
	 * ??????????????????-??????
	 */
	public void showSelect() {
		ll_bottom.setVisibility(View.VISIBLE);
	}
	/**
	 * ??????????????????-??????
	 */
	public void hideSelect() {
		ll_bottom.setVisibility(View.GONE);
	}
	
	/**
	 * ?????????????????????-????????????
	 */
	public void checkPer() {
		checkMenu();
	}
	/**
	 * ?????????????????????-??????
	 */
	public void checkSelectAll() {
		tv_select.setText(getString(R.string.select_all));
	}
	/**
	 * ????????????
	 */
	private void getChooseItem() {
		for (int i = 0; i<hList.size(); i++) {
			if (MyGridViewAddAdapter.getIsSelected().get(i)) {
				int count = -1;
				for(int j=0; j<allList.size(); j++){
					if(allList.get(j).getMenuCode().equals(hList.get(i).getMenuCode())){
						count = j;
					}
				}
				
				if(-1 != count){
					allList.get(count).setIsChoose(1);
				}
			}else{
				int count = -1;
				for(int j=0; j<allList.size(); j++){
					if(allList.get(j).getMenuCode().equals(hList.get(i).getMenuCode())){
						count = j;
					}
				}
				
				if(-1 != count){
					allList.get(count).setIsChoose(0);
				}
			}
		}
	}
	
	/**
	 * ?????????????????????????????????
	 */
	private void reductionChoose(){
		for(int i=0; i<hList.size(); i++){
			for(int j=0; j<allList.size(); j++){
				if(allList.get(j).getMenuCode().equals(hList.get(i).getMenuCode())){
					if(1 == allList.get(j).getIsChoose()){
						MyGridViewAddAdapter.getIsSelected().put(i, true);
					}
				}
			}
		}
		
		mAdapter.notifyDataSetChanged();
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
			DialogUtil.showWithCancelProgressDialog(getActivity(), null, getResources().getString(R.string.global_prompt_message), null);
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			DialogUtil.closeDialog();
			String result = responseInfo.result.toString();
			byte[] ebase64 = EncryptUtil.decodeBase64(result);
			if (null != result && !"".equals(result.trim())) {
				PublicResultBean resMsg = JSON.parseObject(new String(ebase64), PublicResultBean.class);
				// ????????????????????????????????????
				if (null == resMsg || null == resMsg.getResHeader().getStateCode() || "".equals(resMsg.getResHeader().getStateCode())) {				
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
					return;
				// ???????????????
				}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
					LogUtil.promptInfo(getActivity(), resMsg.getResHeader().getReturnMsg());	
					return;
				}
				// ???????????????????????????
				if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
				} else {
					if(null != resMsg.getResHeader().getSectetKey() && !"".equals(resMsg.getResHeader().getSectetKey().trim())){
						BaseApp.key = resMsg.getResHeader().getSectetKey();
					}
					// ?????????????????????????????????
					Message msgs = new Message();
					msgs.what = operType;
					msgs.obj = resMsg.getResBody().getResultString();
					mHandler.sendMessage(msgs);
				}
			} else {
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
			}		
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			DialogUtil.closeDialog();
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("1207", getResources()));
			}else{
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getResources()));
			}
		}
	};
	
}
