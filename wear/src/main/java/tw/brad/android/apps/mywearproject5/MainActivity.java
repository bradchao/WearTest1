package tw.brad.android.apps.mywearproject5;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity {
    private static final int GOOGLE_API_CLIENT_REQUEST_RESOLVE_ERROR = 1000;
    private GoogleApiClient mGoogleApiClient;
    private boolean mbResolvingGoogleApiCLientError = false;


    private TextView mTextView;

    private GoogleApiClient.ConnectionCallbacks gooApiClientConnCallback =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    mbResolvingGoogleApiCLientError = false;
                    Log.v("brad", "onConnected");
                    //Wearable.MessageApi.addListener(mGoogleApiClient, wearableMsgListener);
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.v("brad", "無法連線");
                }
            };

    private GoogleApiClient.OnConnectionFailedListener gooApiClientOnConnFail =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    if (mbResolvingGoogleApiCLientError){
                        Log.v("brad", "mbResolvingGoogleApiCLientError");
                        return;
                    }else if (connectionResult.hasResolution()){
                        try {
                            mbResolvingGoogleApiCLientError = true;
                            connectionResult.startResolutionForResult(MainActivity.this,
                                    GOOGLE_API_CLIENT_REQUEST_RESOLVE_ERROR);
                        }catch (Exception e){
                            mbResolvingGoogleApiCLientError = false;
                            mGoogleApiClient.connect();
                        }
                    }else{
                        mbResolvingGoogleApiCLientError = false;
                        Wearable.MessageApi.removeListener(mGoogleApiClient,
                                wearableMsgListener);
                    }
                }
            };

    private MessageApi.MessageListener wearableMsgListener =
            new MessageApi.MessageListener() {
                @Override
                public void onMessageReceived(MessageEvent messageEvent) {
                    // 由 messageEvent.getData() 來取得 message 中的 byte[]
                    Log.v("brad", "onMessageReceived");
                    byte[] receiveMesg = messageEvent.getData();
                    Log.v("brad", new String(receiveMesg));

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(gooApiClientConnCallback)
                .addOnConnectionFailedListener(gooApiClientOnConnFail)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mbResolvingGoogleApiCLientError){
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        if (!mbResolvingGoogleApiCLientError){
//            Wearable.MessageApi.removeListener(mGoogleApiClient,
//                    wearableMsgListener);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }



}
