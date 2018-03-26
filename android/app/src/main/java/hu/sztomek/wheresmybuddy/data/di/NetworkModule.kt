package hu.sztomek.wheresmybuddy.data.di

import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.data.api.http.IRemoteApi
import hu.sztomek.wheresmybuddy.data.api.user.IUserApi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Module
class NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor { message -> Timber.d(message) }
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    fun provideOkHttp(userApi: IUserApi, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val token = userApi.getAuthenticationToken(false).blockingGet()
                        val result = chain.proceed(appendAuthHeader(originalRequest, token))
                        // Unauthorized
                        if (result.code() == 403) {
                            val newToken = userApi.getAuthenticationToken(true).blockingGet()
                            chain.proceed(appendAuthHeader(originalRequest, newToken))
                        } else {
                            result
                        }
                }
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
    }

    private fun appendAuthHeader(request: Request, token: String): Request {
        return request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
    }

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://us-central1-wheresmybuddy-f5ade.cloudfunctions.net/api/")
//                .baseUrl("http://192.168.1.16:5000/wheresmybuddy-f5ade/us-central1/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
    }

    @Provides
    fun provideRemoteApi(retrofit: Retrofit): IRemoteApi {
        return retrofit.create(IRemoteApi::class.java)
    }

}