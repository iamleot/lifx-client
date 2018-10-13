package com.github.gilbertotcc.lifx.impl;

import java.io.IOException;
import java.util.List;

import com.github.gilbertotcc.lifx.LifxClient;
import com.github.gilbertotcc.lifx.api.LifxApi;
import com.github.gilbertotcc.lifx.exception.LifxClientException;
import com.github.gilbertotcc.lifx.models.Light;
import com.github.gilbertotcc.lifx.models.Selector;
import com.github.gilbertotcc.lifx.models.converter.SelectorConverter;
import com.github.gilbertotcc.lifx.models.converter.StringConverterFactory;
import com.github.gilbertotcc.lifx.util.JacksonUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LifxClientImpl implements LifxClient {

    private static final String LIFX_BASE_URL = "https://api.lifx.com";

    private final LifxApi lifxApi;

    LifxClientImpl(final LifxApi lifxApi) {
        this.lifxApi = lifxApi;
    }

    public static LifxClientImpl createNew(final String accessToken) {
        final OkHttpClient okHttpClient = LifxOkHttpClientFactory.INSTANCE.getOkHttpClient(accessToken);
        final LifxApi lifxApi = new Retrofit.Builder()
                .baseUrl(LIFX_BASE_URL)
                .addConverterFactory(StringConverterFactory.of(Selector.class, new SelectorConverter()))
                .addConverterFactory(JacksonConverterFactory.create(JacksonUtils.OBJECT_MAPPER))
                .client(okHttpClient)
                .build()
                .create(LifxApi.class);
        return new LifxClientImpl(lifxApi);
    }

    @Override
    public List<Light> listLights(final Selector selector) {
        return executeAndGetBody(lifxApi.listLights(selector));
    }

    private static <T> T executeAndGetBody(final Call<T> call) {
        try {
            final Response<T> response = call.execute();
            assertSuccessfull(response);
            return response.body();
        } catch (IOException e) {
            throw new LifxClientException("Error occurred while calling LIFX HTTP API", e);
        }
    }

    private static <T> void assertSuccessfull(final Response<T> response) {
        if (!response.isSuccessful()) {
            throw new LifxClientException(String.format("Error occurred while calling LIFX HTTP API (code: %d)", response.code()));
        }
    }
}
