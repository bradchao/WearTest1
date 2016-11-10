package tw.brad.android.apps.mywearproject5;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity {
    private static final int GOOGLE_API_CLIENT_REQUEST_RESOLVE_ERROR = 1000;
    private GoogleApiClient mGoogleApiClient;
    private boolean mbResolvingGoogleApiCLientError = false;

    private GoogleApiClient.ConnectionCallbacks gooApiClientConnCallback =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    mbResolvingGoogleApiCLientError = false;
                    Log.v("brad", "onConnected");
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
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(gooApiClientConnCallback)
                .addOnConnectionFailedListener(gooApiClientOnConnFail)
                .build();
        if( mGoogleApiClient != null && !( mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting() ) )
            mGoogleApiClient.connect();
    }

    public void test1(View v){
//        new Thread(){
//            @Override
//            public void run() {
//                sendMessage();
//            }
//        }.start();
        new MyAsyncTask().execute();
    }

    private void sendMessage(){
        Log.v("brad", "Send Message...");
        // 取得所有連線的 Device
        NodeApi.GetConnectedNodesResult connectedNodesResult =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        Log.v("brad", "=> " + connectedNodesResult.getNodes().size());

        // 對每個Device發出 Message
        for (Node node : connectedNodesResult.getNodes()){
            byte[] payload = "Halo".getBytes();

            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient,node.getId(),
                            "/message", payload).await();

            if (result.getStatus().isSuccess()){
                Log.v("brad", "Send OK");
            }else{
                Log.v("brad", "Send Fail");
            }
        }
    }

    private class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Log.v("brad", "start...");
            sendMessage();
            return null;
        }
    }

}
