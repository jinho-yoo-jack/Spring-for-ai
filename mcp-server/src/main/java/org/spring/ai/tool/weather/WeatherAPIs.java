package org.spring.ai.tool.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPIs {
    @GET("api/typ01/url/kma_sfctm2.php")
    Call<String> getWeather(@Query("authKey") String authKey, @Query("stn") int stn);

}
