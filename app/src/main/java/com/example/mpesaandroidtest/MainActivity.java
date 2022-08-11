package com.example.mpesaandroidtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mpesaandroidtest.Model.AccessToken;
import com.example.mpesaandroidtest.Model.STKPush;
import com.example.mpesaandroidtest.Services.DarajaApiClient;
import com.example.mpesaandroidtest.Services.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.mpesaandroidtest.Constants.BUSINESS_SHORT_CODE;
import static com.example.mpesaandroidtest.Constants.CALLBACKURL;
import static com.example.mpesaandroidtest.Constants.PARTYB;
import static com.example.mpesaandroidtest.Constants.PASSKEY;
import static com.example.mpesaandroidtest.Constants.TRANSACTION_TYPE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    @BindView(R.id.editText2)
    EditText editText2;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.paynowbtn)
    Button paynowbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressDialog=new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true);
        paynowbtn.setOnClickListener(this);
        getAccessToken();

    }
    public void getAccessToken(){
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v==paynowbtn){
            String phone_number = editText.getText().toString();
            String amount = editText2.getText().toString();
            performSTKPush(phone_number,amount);
        }
    }

    private void performSTKPush(String phone_number, String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE,PASSKEY,timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                Utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "harry",
                "test"
        );

        mApiClient.setGetAccessToken(false);

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Timber.d("post submitted to API. %s", response.body());
                    } else {
                        Timber.e("Response %s", response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t);
            }
        });
    }
}
