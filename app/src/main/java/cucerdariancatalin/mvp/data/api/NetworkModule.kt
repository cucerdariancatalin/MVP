package cucerdariancatalin.mvp.data.api

import com.google.gson.GsonBuilder
import com.trello.rxlifecycle2.android.BuildConfig
import cucerdariancatalin.mvp.data.api.service.NewsService
import cucerdariancatalin.mvp.data.api.service.SourceService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule () {

    @Provides
    internal fun provideHttpLoggingInterceptor() : HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return loggingInterceptor
    }

    @Provides
    internal fun provideOkHttpClient(httpLoggingInterceptor
                                     : HttpLoggingInterceptor): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.networkInterceptors().add(httpLoggingInterceptor)

        return httpClientBuilder.build()
    }

    @Provides
    internal fun provideGsonConverterFactory() : GsonConverterFactory {
        val GSON = GsonBuilder()
            .registerTypeAdapterFactory(AutoValueAdapterFactory())
            .create()
        return GsonConverterFactory.create(GSON)
    }

    @Provides
    internal fun provideRetrofit(okHttpClient: OkHttpClient,
                                 converterFactory: GsonConverterFactory) : Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://newsapi.org/v1/")
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converterFactory)
            .build()
    }

    @Provides
    internal  fun provideSourceService(retrofit: Retrofit) : SourceService {
        return retrofit.create<SourceService>(SourceService::class.java)
    }

    @Provides
    internal  fun provideNewsService(retrofit: Retrofit) : NewsService {
        return retrofit.create<NewsService>(NewsService::class.java)
    }
}