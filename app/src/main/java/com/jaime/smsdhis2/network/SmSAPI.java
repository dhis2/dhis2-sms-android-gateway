package com.jaime.smsdhis2.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SmSAPI {

    @POST("api/sms/inbound")
    Call<SMSResponse> sendSMS(@Header("Authorization") String authkey , @Body IncomingSMS incomingSMS);
}
