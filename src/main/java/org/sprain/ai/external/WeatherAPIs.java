package org.sprain.ai.external;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface WeatherAPIs {
    @GET("api/typ01/url/kma_sfctm2.php")
    Call<String> getWeather(@Query("authKey") String authKey, @Query("stn") int stn);

}
