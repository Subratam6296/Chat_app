package com.example.newchatapplication.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newchatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.HashMap;
import java.util.Map;

public class Util {


    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        } else return false;

    }

    public static void updateDeviceTokken(Context context, String token) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference reference = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(NodeNames.DEVICE_TOKEN, token);

            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(context, context.getString(R.string.failed_to_update_device_token, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

    }

    public static void sendNotification(final Context context, String message, String tittle, String userId) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(NodeNames.DEVICE_TOKEN).getValue() != null) {

                    String deviceToken = snapshot.child(NodeNames.DEVICE_TOKEN).getValue().toString();
                    Log.d("tokken", deviceToken);

                    JSONObject notification = new JSONObject();
                    JSONObject notificationData = new JSONObject();

                    try {

                        notificationData.put(Constants.NOTIFICATION_TITTLE, tittle);
                        notificationData.put(Constants.NOTIFICATION_MESSAGE, message);

                        notification.put(Constants.NOTIFICATION_TO, deviceToken);
                        notification.put(Constants.NOTIFICATION_DATA, notificationData);

                        String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
                        String contentType = "application/json";

                        Response.Listener successListener = new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                Toast.makeText(context, context.getString(R.string.notificayion_sent), Toast.LENGTH_SHORT).show();
                            }
                        };

                        Response.ErrorListener errorListener = new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, context.getString(R.string.failed_to_sent_notification), Toast.LENGTH_SHORT).show();
                            }
                        };

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,fcmApiUrl, notification, successListener, errorListener) {

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                Map<String, String> params = new HashMap<>();
                                params.put("Authorization", "key=" + Constants.FCM_SERVER_KEY);
                                //params.put("Sender","id="+Constants.FCM_SENDER_ID);
                                params.put("Content-Type", contentType);

                                return params;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });


    }

    public static void updateChatDetails(Context context, String currentUserId, String chatUserId,final String lastMessage){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef = rootRef.child(NodeNames.CHAT).child(chatUserId).child(currentUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String currentCount="0";
                if(snapshot.child(NodeNames.UNREAD_COUNT).getValue()!=null){
                    currentCount=snapshot.child(NodeNames.UNREAD_COUNT).getValue().toString();
                }

                Map chatMap = new HashMap();
                chatMap.put(NodeNames.TIME_STAMP, ServerValue.TIMESTAMP);
                chatMap.put(NodeNames.UNREAD_COUNT, Integer.valueOf(currentCount)+1);
                chatMap.put(NodeNames.LAST_MESSAGE, lastMessage);
                chatMap.put(NodeNames.LAST_MESSAGE_TIME,ServerValue.TIMESTAMP);


                Map chatUserMap = new HashMap();
                chatUserMap.put(NodeNames.CHAT+"/"+chatUserId+"/"+currentUserId, chatMap);

                rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error!=null){
                            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Details Updated Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
