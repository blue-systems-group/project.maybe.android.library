package edu.buffalo.cse.maybe_.android.library.rest;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by xcv58 on 9/21/15.
 */
public interface MaybeRESTService {
    @GET("devices/{deviceID}")
    Observable<List<Device>> getDevice(@Path("deviceID") String deviceID);
}
