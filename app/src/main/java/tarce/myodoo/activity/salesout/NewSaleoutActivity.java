package tarce.myodoo.activity.salesout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Response;
import tarce.api.MyCallback;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.NewSaleBean;
import tarce.myodoo.MyApplication;
import tarce.myodoo.R;
import tarce.myodoo.activity.BaseActivity;
import tarce.myodoo.adapter.SalesStatesAdapter;
import tarce.myodoo.greendaoUtils.ContactBeanUtils;

/**
 * Created by zws on 2017/8/9.
 */

public class NewSaleoutActivity extends BaseActivity {
    /*@InjectView(R.id.search_customer)
    SearchView searchCustomer;*/
    @InjectView(R.id.search_sales_number)
    SearchView searchSalesNumber;
    @InjectView(R.id.recyclerview)
    RecyclerView recyclerview;
    @InjectView(R.id.tv_get_last)
    TextView tvGetLast;
    @InjectView(R.id.recyclerviewStates)
    RecyclerView recyclerviewStates;
    private InventoryApi inventoryApi;
    private SalesStatesAdapter salesStatesAdapter;
    private List<NewSaleBean.ResultBean.ResDataBean> beanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_out);
        ButterKnife.inject(this);
        inventoryApi = RetrofitClient.getInstance(NewSaleoutActivity .this).create(InventoryApi.class);
        setTitle("销售出货");
        setRecyclerview(recyclerview);
        setRecyclerview(recyclerviewStates);
        initData();
    }

    private void initData() {
        showDefultProgressDialog();

        Call<NewSaleBean> saleTeam = inventoryApi.getSaleTeam(new HashMap());
        saleTeam.enqueue(new MyCallback<NewSaleBean>() {
            @Override
            public void onResponse (Call < NewSaleBean > call, Response< NewSaleBean > response){
                dismissDefultProgressDialog();
                if (response.body() == null || response.body().getResult() == null) return;
                beanList = response.body().getResult().getRes_data();
                salesStatesAdapter = new SalesStatesAdapter(R.layout.item_sale_new, beanList);
                recyclerviewStates.setAdapter(salesStatesAdapter);
                initListener();
            }

            @Override
            public void onFailure (Call < NewSaleBean > call, Throwable t){
                dismissDefultProgressDialog();
            }
        });
    }

    private void initListener() {
        salesStatesAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                List<NewSaleBean.ResultBean.ResDataBean> data = (List<NewSaleBean.ResultBean.ResDataBean>)adapter.getData();
                Intent intent = new Intent(NewSaleoutActivity.this, NewSaleActivity.class);
                intent.putExtra("team_name", data.get(position).getName());
                intent.putExtra("team_id", data.get(position).getTeam_id());
                startActivity(intent);
            }
        });
    }


}
