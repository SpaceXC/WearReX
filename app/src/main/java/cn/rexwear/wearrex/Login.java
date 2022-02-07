package cn.rexwear.wearrex;

import static cn.rexwear.wearrex.MainActivity.TAG;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.rexwear.wearrex.beans.UserBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;

public class Login extends Fragment {
    ImageButton ok;
    ImageButton buOK;

    EditText user;
    EditText password;

    TextView textViewTitle;
    TextView textViewTitle2;

    boolean isEnteringPassword = false;

    final ExecutorService mThreadPool = Executors.newCachedThreadPool();


    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ok = getView().findViewById(R.id.yes);
        buOK = getView().findViewById(R.id.no);


        user = getView().findViewById(R.id.userName);
        password = getView().findViewById(R.id.password);

        textViewTitle = getView().findViewById(R.id.loginTitle);
        textViewTitle2 = getView().findViewById(R.id.loginTitle2);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEnteringPassword){
                    isEnteringPassword = true;
                    user.setVisibility(View.INVISIBLE);
                    password.setVisibility(View.VISIBLE);
                    textViewTitle.setTextColor(Color.parseColor("#90CAF9"));
                    textViewTitle.setText("输入密码");
                    textViewTitle2.setText("区分大小写");
                    ok.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_24));
                    buOK.setImageDrawable(getResources().getDrawable(R.drawable.arrow_back));
                }else{
                    if(!TextUtils.isEmpty(user.getText()) && !TextUtils.isEmpty(password.getText())){
                        Login(user.getText().toString(), password.getText().toString());
                    }else{
                        Toast.makeText(getContext(), "请正确填写ID和密码！", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        buOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEnteringPassword){

                    NavController controller = Navigation.findNavController(view);
                    controller.navigate(R.id.action_login_to_loginOrUp);
                }
                else{
                    isEnteringPassword = false;
                    password.setVisibility(View.INVISIBLE);
                    user.setVisibility(View.VISIBLE);
                    textViewTitle.setTextColor(Color.parseColor("#90CAF9"));
                    textViewTitle.setText("输入用户名/邮箱");
                    textViewTitle2.setText("不支持ID登陆");
                    ok.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_ios_24));
                    buOK.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_close_24));
                }

            }
        });
    }

    void Login(String userName, String passWord){
        Log.d(TAG, "Login: userName: " + userName + ", password: " + passWord);

        ok.setEnabled(false);
        buOK.setEnabled(false);
        password.setEnabled(false);
        textViewTitle.setTextColor(Color.parseColor("#90CAF9"));
        textViewTitle.setText("校验中...");

        OkHttpClient client = NetworkUtils.getInstance(getContext()).client;
        RequestBody body = new FormBody.Builder()
                .add("login", userName)
                .add("password", passWord)
                .build();
        Request request = new Request.Builder()
                .url("https://rexwear.cn/index.php?api/auth")
                .post(body)
                .addHeader("User-Agent", "ReXAppAndroid/JavaOkHttpRequested")
                .addHeader("XF-API-Key", "x3KEr7kI-ZOrNOjN46HAkB0oGgqHkXLt")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewTitle.setText("校验失败，请重新登陆！");
                                textViewTitle.setTextColor(Color.parseColor("#F2B8B5"));
                                ok.setEnabled(true);
                                buOK.setEnabled(true);
                                password.setEnabled(true);
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.code());
                if(response.code() == 200){
                    //Log.d(TAG, "onResponse: " + response.body().string());
                    UserBean user = UserBean.objectFromData(response.body().string());

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userBean", user);
                    Log.d(TAG, "onResponse: 登录成功，欢迎" + user.user.username);
                    
                    mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewTitle.setTextColor(Color.parseColor("#90CAF9"));
                                    textViewTitle.setText("登录成功");
                                    NavController controller = Navigation.findNavController(getView());
                                    controller.navigate(R.id.action_login_to_welcomeLoginFragment, bundle);
                                }
                            });
                        }
                    });

                }
                else{


                    mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewTitle.setText("校验失败，请重新登陆！");
                                    textViewTitle.setTextColor(Color.parseColor("#F2B8B5"));
                                    textViewTitle.setCompoundDrawables(getResources().getDrawable(R.drawable.ic_baseline_close_24_pink), null, null, null);
                                    ok.setEnabled(true);
                                    buOK.setEnabled(true);
                                    password.setEnabled(true);
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}