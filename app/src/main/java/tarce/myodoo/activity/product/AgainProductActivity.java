package tarce.myodoo.activity.product;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.PickingDetailBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.BaseActivity;
import tarce.myodoo.activity.OrderDetailActivity;
import tarce.myodoo.activity.ProductLineListActivity;
import tarce.myodoo.activity.ProductingActivity;
import tarce.myodoo.activity.salesout.SalesDetailActivity;
import tarce.myodoo.adapter.product.PickingDetailAdapter;
import tarce.myodoo.uiutil.TipDialog;
import tarce.myodoo.utils.StringUtils;
import tarce.support.SharePreferenceUtils;

/**
 * Created by zouwansheng on 2017/10/20.
 */

public class AgainProductActivity extends BaseActivity {
    @InjectView(R.id.recycler_line)
    RecyclerView recyclerLine;
    private InventoryApi inventoryApi;
    private PickingDetailAdapter pickAdapter;
    private List<PickingDetailBean.ResultBean.ResDataBean> res_data;
    private int production_line_id;
    private String state;
    private int process_id;
    private int origin_sale_id;
    private boolean search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineproduct);
        ButterKnife.inject(this);

        inventoryApi = RetrofitClient.getInstance(AgainProductActivity.this).create(InventoryApi.class);
        Intent intent = getIntent();
        production_line_id = intent.getIntExtra("production_line_id", -1000);
        state = intent.getStringExtra("state");
        process_id = intent.getIntExtra("process_id", 1000);
        origin_sale_id = intent.getIntExtra("origin_sale_id", 0);
        setRecyclerview(recyclerLine);
        search = intent.getBooleanExtra("search", false);
        if (search){
            res_data = (List<PickingDetailBean.ResultBean.ResDataBean>) intent.getSerializableExtra("data");
            pickAdapter = new PickingDetailAdapter(R.layout.adapter_picking_activity, res_data);
            recyclerLine.setAdapter(pickAdapter);
            clickAdapterItem();
        }else {
            showDefultProgressDialog();
            initData();
        }
    }

    @Override
    protected void onResume() {
        if (res_data == null) {
           initData();
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        res_data = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        res_data = null;
        super.onDestroy();
    }

    private void initData() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("state", state);
        int partner_id = SharePreferenceUtils.getInt("partner_id", 1000, AgainProductActivity.this);
        hashMap.put("partner_id", partner_id);
        if (process_id != -1000) {
            hashMap.put("process_id", process_id);
        }
        if (production_line_id == -1000){
            hashMap.put("production_line_id", false);
        }else {
            hashMap.put("production_line_id", production_line_id);
        }
        if (origin_sale_id!=0){
            hashMap.put("origin_sale_id", origin_sale_id);
        }else {
            hashMap.put("origin_sale_id", false);
        }
        Call<PickingDetailBean> secondMos = inventoryApi.getNewSecondMos(hashMap);
        secondMos.enqueue(new Callback<PickingDetailBean>() {
            @Override
            public void onResponse(Call<PickingDetailBean> call, Response<PickingDetailBean> response) {
                dismissDefultProgressDialog();
                if (response.body() == null) return;
                if (response.body().getError()!=null){
                    new TipDialog(AgainProductActivity.this, R.style.MyDialogStyle, response.body().getError().getMessage())
                            .show();
                    return;
                }
                if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code()==1){
                    res_data = response.body().getResult().getRes_data();
                    pickAdapter = new PickingDetailAdapter(R.layout.adapter_picking_activity, res_data);
                    recyclerLine.setAdapter(pickAdapter);
                    clickAdapterItem();
                }
            }

            @Override
            public void onFailure(Call<PickingDetailBean> call, Throwable t) {
                dismissDefultProgressDialog();
            }
        });
    }

    private void clickAdapterItem() {
        pickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (res_data == null) return;
                int order_id = res_data.get(position).getOrder_id();
                if (res_data.get(position).getState().equals("progress")) {
                    Intent intent = new Intent(AgainProductActivity.this, ProductingActivity.class);
                    intent.putExtra("order_name", res_data.get(position).getDisplay_name());
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("state", "progress");
                    intent.putExtra("name_activity", "生产中");
                    intent.putExtra("state_activity", res_data.get(position).getState());
                    intent.putExtra("production_line_id", -1000);
                    intent.putExtra("process_id", process_id);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AgainProductActivity.this, OrderDetailActivity.class);
                    intent.putExtra("order_name", res_data.get(position).getDisplay_name());
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("state", res_data.get(position).getState());
                    //intent.putExtra("name_activity", name_activity);
                    intent.putExtra("state_activity", res_data.get(position).getState());
                    intent.putExtra("production_line_id", -1000);
                    intent.putExtra("process_id", process_id);
                    startActivity(intent);
                }
            }
        });
    }
}
