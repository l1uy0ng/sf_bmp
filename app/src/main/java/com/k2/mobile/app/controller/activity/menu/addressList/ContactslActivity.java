package com.k2.mobile.app.controller.activity.menu.addressList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.menu.other.SearchActivity;
import com.k2.mobile.app.controller.core.BaseActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.SortAdapter;
import com.k2.mobile.app.model.bean.Contacts;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DateUtil;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PinyinComparator;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.view.widget.SideBar;
import com.k2.mobile.app.view.widget.XListView;
import com.k2.mobile.app.view.widget.SideBar.OnTouchingLetterChangedListener;
import com.k2.mobile.app.view.widget.XListView.IXListViewListener;

/**
 * @Package com.oppo.mo.controller.activity.menu.workbench;
 * @Description ???????????????
 * @Company  K2
 * 
 * @ClassName: ContactslActivity
 * @Description: ???????????????
 * @author linqijun
 * @date 2015-4-8 ??????4:04:00
 * 
 */
public class ContactslActivity extends BaseActivity implements OnClickListener,IXListViewListener {
	
	private TextView tv_title;
	private ImageView iv_per_center;
	private TextView tv_sech;
	private Button btn_back;
	private XListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	
	private List<Contacts> cList = null;
	private PinyinComparator pinyinComparator;
	private SharedPreferences sharedPreferences;
	
	private int flag = 1;
	private Intent mIntent = null;

	public Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String str = (String) msg.obj;
					// ??????????????????????????????
					if(null == str || "".equals(str.trim())){
						break;
					}
					if(null != cList){
						cList.clear(); // ??????cList
					}else{
						cList = new ArrayList<Contacts>();
					}
					// ??????JSON?????????list
					cList.addAll(JSON.parseArray(str, Contacts.class));
					initAdapter();
					break;
				case 2:
					DialogUtil.closeDialog();
					String json = (String) msg.obj;
					if(null == json || "".equals(json.trim())){
						break;
					}
					
					Contacts contacts = JSON.parseObject(json, Contacts.class);
					if(contacts != null){
						mIntent = new Intent(ContactslActivity.this, MyAddressBookDetailActivity.class);
						Bundle mBundle = new Bundle();  
				        mBundle.putSerializable("contacts",contacts); 
				        mIntent.putExtras(mBundle);
				        startActivity(mIntent);
					}
					break;
					//????????????
				case 3:
					DialogUtil.closeDialog();
					refreshData();
					initAdapter();
					break;
					//????????????????????????
				case 4:
					refreshData();
					onLoad(); //????????????
					adapter.updateListView(cList);
					break;
			}
		}
	};
	
	
	
	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		// ????????????
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_contacts);
		initView();
		initListener();
		
		String info = getAddressBookUser();
		queryUserAddrBook(info);
		
		BaseApp.addActivity(this);
	}
	
	/**
	 * @Title: refreshData
	 * @Description: ??????/?????????????????????
	 * @param     
	 * @return void  
	 * @throws
	 */
	private void refreshData() {
		Iterator<Contacts> iter = cList.iterator();  
		while(iter.hasNext()){  
			Contacts c = iter.next();  
		    if(c.getUserCode().trim().length() == 8){  
		    }else{
		    	iter.remove();  
		    }
		} 
		// ??????????????????
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("time", DateUtil.getDownloadDateStr());
		editor.commit();
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		iv_per_center = (ImageView) findViewById(R.id.iv_per_center);
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_sech = (TextView) findViewById(R.id.tv_sech);
		tv_title.setText(R.string.my_address_book);
		pinyinComparator = new PinyinComparator();
		
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		dialog.getBackground().setAlpha(179);
//		mClearEditText = (ClearEditText) view.findViewById(R.id.filter_edit);
		sortListView = (XListView) findViewById(R.id.country_lvcountry);
		//????????????????????????
		sortListView.setPullRefreshEnable(false);
		sortListView.setPullLoadEnable(false);
		iv_per_center.setVisibility(View.GONE);
		btn_back.setVisibility(View.VISIBLE);
		
		tv_sech.setText("");
		sideBar.setTextView(dialog);
	}
	
	/**
	 * ?????????: initListener() 
	 * ??? ??? : ???????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		btn_back.setOnClickListener(this);
		tv_sech.setOnClickListener(this);
		sideBar.setOnTouchingLetterChangedListener(this.tlchListener);
		sortListView.setOnItemClickListener(itemClickListener);
		sortListView.setXListViewListener(this);
//		mClearEditText.addTextChangedListener(editTextWatcher);
	}
	
	/**
	 * ?????????: initAdapter() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initAdapter() {
		Collections.sort(this.cList, this.pinyinComparator);
		adapter = new SortAdapter(ContactslActivity.this, this.cList);
		sortListView.setAdapter(this.adapter);
	}
			 
	/**
	 * ?????????: getAddressBookUser() 
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: JSON??????
	 */
	private String getAddressBookUser() {
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F30000001");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * ?????????: queryUserAddrBook() 
	 * ??? ??? : ????????????  
	 * ??? ??? : info ????????????
	 * 		   server ????????????
	 * ?????????: void
	 */
	private void queryUserAddrBook(String info) {
		
		if(null == info || "".equals(info.trim())){
			return;
		}
		
		if (!NetWorkUtil.isNetworkAvailable(ContactslActivity.this)) {
			DialogUtil.showLongToast(ContactslActivity.this, R.string.global_network_disconnected);
		}else{
			if (null != info) {
				SendRequest.submitRequest(ContactslActivity.this, info, submitCallBack);
			}
		}
	}
	
	// ????????????????????????
	OnTouchingLetterChangedListener tlchListener = new OnTouchingLetterChangedListener() {
		@Override
		public void onTouchingLetterChanged(String s) {
			if(null != cList && cList.size() > 0){
				//??????????????????????????????
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}
			}
		}
	};
		
		// listView item????????????
	OnItemClickListener itemClickListener = new OnItemClickListener() {
	    @Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	if(position == 0)return;
	    	flag = 2;
			// ??????adapter.getItem(position)???????????????position??????????????????
	    	String userAccount = ((Contacts)adapter.getItem(position - 1)).getUserCode();
	    	getUserInfo(userAccount);
		}
	};
		
		/**
		 * http????????????
		 */
		RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

			@Override
			public void onStart() {
				if(flag == 4)return;
				DialogUtil.showWithCancelProgressDialog(ContactslActivity.this, null, res.getString(R.string.global_prompt_message), null);
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
						LogUtil.promptInfo(ContactslActivity.this, ErrorCodeContrast.getErrorCode("0", res));
						return;
					// ???????????????
					}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
						LogUtil.promptInfo(ContactslActivity.this, resMsg.getResHeader().getReturnMsg());	
						return;
					}
					// ???????????????????????????
					if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
						LogUtil.promptInfo(ContactslActivity.this, ErrorCodeContrast.getErrorCode("0", res));
					} else {
						if(null != resMsg.getResHeader().getSectetKey() && !"".equals(resMsg.getResHeader().getSectetKey().trim())){
							BaseApp.key = resMsg.getResHeader().getSectetKey();
						}
						// ?????????????????????????????????
						Message msgs = new Message();
						msgs.what = flag;
						msgs.obj = resMsg.getResBody().getResultString();
						mHandler.sendMessage(msgs);
					}
				} else {
					LogUtil.promptInfo(ContactslActivity.this, ErrorCodeContrast.getErrorCode("0", res));
				}		
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				DialogUtil.closeDialog();
				if(msg.equals(LocalConstants.API_KEY)){
					LogUtil.promptInfo(ContactslActivity.this, ErrorCodeContrast.getErrorCode("1207", res));
				}else{
					LogUtil.promptInfo(ContactslActivity.this, ErrorCodeContrast.getErrorCode("0", res));
				}
			}
		};
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_per_center:	// ????????????
				finish();
				break;
			case R.id.tv_sech:
				mIntent = new Intent(ContactslActivity.this, SearchActivity.class);
				mIntent.putExtra("operType", 11);
				ContactslActivity.this.startActivity(mIntent);
				break;
			case R.id.btn_back:
				finish();
				break;
			}
		}
		
		/**
		 * @Title: startActivity
		 * @Description: ??????Activity
		 * @return void    ????????????
		 * @throws
		 */
		@Override
		public void startActivity(Class<?> activity) {
			Intent intent = new Intent(ContactslActivity.this, activity);
			startActivity(intent);
		}
		/**
		 * @Title: startActivity
		 * @Description: ??????Activity
		 * @return void    ????????????
		 * @throws
		 */
		public void getUserInfo(String userCode){
	    	
	    	ReqBodyBean bBean = new ReqBodyBean();
			bBean.setInvokeFunctionCode("F30000002");
			bBean.setInvokeParameter("[\""+userCode+"\"]");
					
			PublicRequestBean bean = new PublicRequestBean();
			bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
			bean.setReqBody(bBean);
	    	
	    	String info = JSON.toJSONString(bean);
	    	queryUserAddrBook(info);
		}
		
		//????????????
		@Override
		public void onRefresh() {
			flag = 4;
			// ????????????????????????
			String info = getAddressBookUser();
			queryUserAddrBook(info);
		}
		
		//????????????
		@Override
		public void onLoadMore() {
		}
		
		//????????????
		private void onLoad() {
			sortListView.stopRefresh();
			sortListView.stopLoadMore();
			sortListView.setRefreshTime(res.getString(R.string.just));
		}
}
