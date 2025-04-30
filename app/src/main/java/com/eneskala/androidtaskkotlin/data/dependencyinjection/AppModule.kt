package com.eneskala.androidtaskkotlin.data.dependencyinjection

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.WorkManager
import com.eneskala.androidtaskkotlin.data.local.dao.TaskDao
import com.eneskala.androidtaskkotlin.data.local.database.TaskDatabase
import com.eneskala.androidtaskkotlin.data.repository.TaskRepository
import com.eneskala.androidtaskkotlin.data.service.ApiService
import com.eneskala.androidtaskkotlin.data.util.AuthInterceptor
import com.eneskala.androidtaskkotlin.data.util.NetworkHelper
import com.eneskala.androidtaskkotlin.data.util.TokenAuthenticator
import com.eneskala.androidtaskkotlin.data.util.TokenManager
import com.eneskala.androidtaskkotlin.data.util.Util.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun injectTokenManager(@ApplicationContext context: Context) : TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun injectOkHttpClient(@ApplicationContext context: Context,
                           tokenManager: TokenManager,
                           apiServiceProvider: Provider<ApiService>) //The apiservice is only needed when a 401 is returned.
    : OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            //For detailed log information on requests and responses
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(apiServiceProvider,tokenManager)) //for code401
            .addInterceptor(logging)
            .build()
    }

    @Singleton
    @Provides
    fun injectRetrofit(okHttpClient: OkHttpClient) : ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun injectRoomDatabase(@ApplicationContext context: Context) : TaskDatabase =
        Room.databaseBuilder(context,TaskDatabase::class.java,
        "TaskDB").build()

    @Singleton
    @Provides
    fun injectDao(database: TaskDatabase) = database.taskDao()

    @Singleton
    @Provides
    fun injectTaskRepo(apiService: ApiService,dao:TaskDao, networkHelper: NetworkHelper) = TaskRepository(apiService,dao,networkHelper)

    @Provides
    @Singleton
    fun injectWorkManager(
        @ApplicationContext appContext: Context
    ): WorkManager {
        return WorkManager.getInstance(appContext)
    }

    @Provides
    @Singleton
    fun injectNetworkHelper(
        @ApplicationContext context: Context
    ): NetworkHelper = NetworkHelper(context)

}