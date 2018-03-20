package tarce.myodoo.activity.inspect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Response;
import tarce.api.MyCallback;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.QcFeedbaskBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.BaseActivity;
import tarce.myodoo.activity.moreproduce.InquiriessMorePActivity;
import tarce.myodoo.adapter.InspectionSubAdapter;
import tarce.myodoo.uiutil.RecyclerFooterView;
import tarce.myodoo.uiutil.RecyclerHeaderView;
import tarce.myodoo.uiutil.TipDialog;
import tarce.support.ToastUtils;
import tarce.support.ToolBarActivity;

/**
 * Created by rose.zou on 2017/6/1.
 * 品检的子页面 列表
 */

public class InspectionSubActivity extends BaseActivity {

    private static final int Refresh_Move = 1;//下拉动作
    private static final int Load_Move = 2;//上拉动作

    @InjectView(R.id.swipe_refresh_header)
    RecyclerHeaderView swipeRefreshHeader;
    @InjectView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @InjectView(R.id.swipe_load_more_footer)
    RecyclerFooterView swipeLoadMoreFooter;
    @InjectView(R.id.swipeToLoad)
    SwipeToLoadLayout swipeToLoad;
    private InventoryApi inventoryApi;
    private String state;
    private InspectionSubAdapter subAdapter;
    private List<QcFeedbaskBean.ResultBean.ResDataBean> res_data = new ArrayList<>();
    private List<QcFeedbaskBean.ResultBean.ResDataBean> for_transform = new ArrayList<>();
    private int loadTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_sub);
        ButterKnife.inject(this);

        setRecyclerview(swipeTarget);

        Intent intent = getIntent();
        state = intent.getStringExtra("state");
                switch (state){
            case "draft":
                setTitle("等待生产品检");
                break;
            case "qc_ing":
                setTitle("品检中");
                break;
            case "qc_success":
                setTitle("等待入库");
                break;
        }
        getFeedback(0,20, Refresh_Move);
        setRecyc();
    }

    @Override
    protected void onResume(){
        if (for_transform == null){
            swipeToLoad.setRefreshing(true);
            loadTime = 0;
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        for_transform = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        for_transform = null;
        super.onPause();
    }

    private void setRecyc() {
        showDefultProgressDialog();
        swipeRefreshHeader.setGravity(Gravity.CENTER);
        swipeLoadMoreFooter.setGravity(Gravity.CENTER);
        swipeToLoad.setRefreshHeaderView(swipeRefreshHeader);
        swipeToLoad.setLoadMoreFooterView(swipeLoadMoreFooter);

        swipeToLoad.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFeedback(0, 20, Refresh_Move);
                if (subAdapter!=null){
                    subAdapter.notifyDataSetChanged();
                }
                swipeToLoad.setRefreshing(false);
            }
        });
        swipeToLoad.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                swipeToLoad.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadTime++;
                        getFeedback(20 * loadTime, 20, Load_Move);
                        subAdapter.notifyDataSetChanged();
                        swipeToLoad.setLoadingMore(false);
                    }
                }, 1000);
            }
        });
    }

    /**
     * 得到返回的数据
     * */
    private void getFeedback(int offset, int limit, final int move) {
        inventoryApi = RetrofitClient.getInstance(InspectionSubActivity.this).create(InventoryApi.class);
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("limit", limit);
        hashMap.put("offset", offset);
        hashMap.put("state", state);
        Call<QcFeedbaskBean> qcfb = inventoryApi.getQcfb(hashMap);
        qcfb.enqueue(new MyCallback<QcFeedbaskBean>() {
            @Override
            public void onResponse(Call<QcFeedbaskBean> call, Response<QcFeedbaskBean> response) {
                dismissDefultProgressDialog();
                if (response.body() == null)return;
                if (response.body().getError()!=null){
                    new TipDialog(InspectionSubActivity.this, R.style.MyDialogStyle, response.body().getError().getData().getMessage())
                            .show();
                    return;
                }
                if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code() == 1){
                    if (move == Refresh_Move){
                        res_data = response.body().getResult().getRes_data();
                        for_transform = res_data;
                        subAdapter = new InspectionSubAdapter(R.layout.adapter_inspec_sub, res_data);
                        swipeTarget.setAdapter(subAdapter);
                    }else {
                        res_data = response.body().getResult().getRes_data();
                        if (res_data == null){
                            ToastUtils.showCommonToast(InspectionSubActivity.this, "没有更多数据...");
                            return;
                        }else {
                            for_transform = subAdapter.getData();
                            for_transform.addAll(res_data);
                            subAdapter.setData(for_transform);
                        }
                    }
                    clickItem();
                }else if (response.body().getResult().getRes_code() == 1 && response.body().getResult().getRes_data() == null
                        && move!=Load_Move){
                    swipeTarget.setVisibility(View.GONE);
                    ToastUtils.showCommonToast(InspectionSubActivity.this, "没有更多数据...");
                }
            }

            @Override
            public void onFailure(Call<QcFeedbaskBean> call, Throwable t) {
                dismissDefultProgressDialog();
                Log.e("zws", t.toString());
                ToastUtils.showCommonToast(InspectionSubActivity.this, t.toString());
            }
        });
    }

    /**
     * item点击事件
     * */
    private void clickItem() {
        subAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                QcFeedbaskBean.ResultBean.ResDataBean resDataBean = subAdapter.getData().get(position);
                Intent intent = new Intent(InspectionSubActivity.this, InspectMoDetailActivity.class);
                intent.putExtra("data", resDataBean);
                startActivity(intent);
            }
        });
    }
}