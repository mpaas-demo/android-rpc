package com.mpaas.demo.rpc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.alipay.mobile.common.rpc.RpcException;
import com.alipay.mobile.common.rpc.RpcInvokeContext;
import com.alipay.mobile.framework.LauncherApplicationAgent;
import com.alipay.mobile.framework.service.annotation.OperationType;
import com.alipay.mobile.framework.service.common.RpcService;
import com.mpaas.demo.R;
import com.mpaas.demo.rpc.model.AccountInfo;
import com.mpaas.demo.rpc.model.UserInfo;
import com.mpaas.demo.rpc.request.GetIdGetReq;
import com.mpaas.demo.rpc.request.PostPostReq;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xingcheng on 2018/7/27.
 */

public class RpcActivity extends Activity {

    private RpcDemoClient demoClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpc);

        RpcService rpcService = LauncherApplicationAgent.getInstance().getMicroApplicationContext().findServiceByInterface(RpcService.class.getName());
        demoClient = rpcService.getRpcProxy(RpcDemoClient.class);

        initView();
        initRpcConfig(rpcService);

        // 内部测试使用，开发者无需关注
        test();
    }

    private void initView() {
        setTitle(getString(R.string.rpc_title));
        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGet();
            }
        });
        findViewById(R.id.btn_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testPost();
            }
        });
        findViewById(R.id.btn_exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testException();
            }
        });
    }

    private void initRpcConfig(RpcService rpcService) {
        RpcInvokeContext rpcInvokeContext = rpcService.getRpcInvokeContext(demoClient);
        // 设置超时时间，单位ms
        rpcInvokeContext.setTimeout(60000);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "val1");
        headerMap.put("key2", "val2");
        rpcInvokeContext.setRequestHeaders(headerMap);
        // 设置rpc拦截器
        // 全局设置，可在 MockLauncherApplicationAgent.postInit() 中设置
        rpcService.addRpcInterceptor(OperationType.class, new CommonInterceptor());
    }

    private void testGet() {
        // rpc 请求是同步的，请不要在UI线程中调用
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GetIdGetReq req = new GetIdGetReq();
                    req.id = "123";
                    req.age = 14;
                    req.isMale = true;
                    String result = demoClient.getIdGet(req);
                    showToast(result);
                } catch (RpcException e) {
                    showToast(e.getMessage());
                }
            }
        }, "rpc-get").start();
    }

    private void testPost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AccountInfo accountInfo = new AccountInfo();
                    accountInfo.username = "123@alipay.com";
                    accountInfo.password = "123456";
                    PostPostReq req = new PostPostReq();
                    req._requestBody = accountInfo;
                    UserInfo userInfo = demoClient.postPost(req);

                    showToast("vip level: " + userInfo.vipInfo.level);
                } catch (RpcException e) {
                    showToast(e.getMessage());
                }
            }
        }, "rpc-post").start();
    }

    private void testException() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = demoClient.exceptionGet();
                if (result != null) {
                    showToast(result);
                }
            }
        }, "rpc-exception").start();
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RpcActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 内部测试使用，开发者无需关注
    private void test(){
        try {
            Class healthActivity = Class.forName("com.mpaas.diagnose.ui.HealthBizSelectActivity");
            Intent intent = new Intent(this, healthActivity);
            startActivity(intent);
        } catch (Exception e) {
        }
    }

}
