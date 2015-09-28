package edu.buffalo.cse.maybe_.android.library.rest;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by xcv58 on 9/21/15.
 */
public interface MaybeRESTService {
    @GET("devices/{deviceID}")
    Observable<List<Device>> getDevice(@Path("deviceID") String deviceID);

    @PUT("devices/{deviceID}")
    Observable<Device> putDevice(@Path("deviceID") String deviceID, @Body Device device);

    @POST("devices/")
    Observable<Device> postDevice(@Body Device device);

    @POST("logs/{deviceID}/{packageName}")
    Observable<LogResponse> postLog(@Path("deviceID") String deviceID, @Path("packageName") String packageName, @Body MaybeLog logObject);
}
