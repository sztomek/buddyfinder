package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.data.Datasource
import hu.sztomek.wheresmybuddy.data.api.db.IDatabaseApi
import hu.sztomek.wheresmybuddy.data.api.db.model.UserDbModel
import hu.sztomek.wheresmybuddy.data.api.db.model.toDomainModel
import hu.sztomek.wheresmybuddy.data.api.http.IRemoteApi
import hu.sztomek.wheresmybuddy.data.api.user.IUserApi
import hu.sztomek.wheresmybuddy.data.api.user.model.UserAuthModel
import hu.sztomek.wheresmybuddy.data.api.user.model.toDomainModel
import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class GetProfileUseCaseTest {

    private val databaseApi: IDatabaseApi = mock(IDatabaseApi::class.java)
    private val userApi: IUserApi = mock(IUserApi::class.java)
    private val restApi: IRemoteApi = mock(IRemoteApi::class.java)
    private val dataSource: IDatasource = Datasource(userApi, restApi, databaseApi)
    private lateinit var getProfileUseCase: GetProfileUseCase

    companion object {
        val FAKE_SELF_USER: UserAuthModel = UserAuthModel("self", "It's me", "itsme@fake.com", "https://avatar.com/iam.jpg")
        val FAKE_SELF_USER_DB: UserDbModel = UserDbModel("self", "It's me", "itsme@fake.com", "https://avatar.com/iam.jpg")
        val FAKE_OTHER_USER_DB: UserDbModel = UserDbModel("other", "Random guy", "fakejohn@fake.com", "https://avatar.com/johndoe.jpg")
    }

    @Before
    fun setUp() {
        getProfileUseCase = GetProfileUseCase(AppSchedulers(Schedulers.io(), Schedulers.io(), Schedulers.io(), Schedulers.io()), dataSource)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `getProfile with null as userId`() {
        Mockito.`when`(userApi.getUser()).thenReturn(Single.just(FAKE_SELF_USER))
        Mockito.`when`(databaseApi.getProfile(FAKE_SELF_USER.id)).thenReturn(Single.just(FAKE_SELF_USER_DB))

        with(
                getProfileUseCase.execute(Action.GetProfileAction(null))
                        .test()
        ) {
            awaitTerminalEvent(5L, TimeUnit.SECONDS)
            assertNoErrors()
            assertValue {
                (it.data as UserModel) == FAKE_SELF_USER.toDomainModel()
            }
        }
    }

    @Test
    fun `getProfile with random userId`() {
        Mockito.`when`(userApi.getUser()).thenReturn(Single.just(FAKE_SELF_USER))
        Mockito.`when`(databaseApi.getProfile("random1234")).thenReturn(Single.just(FAKE_OTHER_USER_DB))
        with(
                getProfileUseCase.execute(Action.GetProfileAction("random1234"))
                        .test()
        ) {
            awaitTerminalEvent(5L, TimeUnit.SECONDS)
            assertNoErrors()
            assertValue {
                (it.data as UserModel) == FAKE_OTHER_USER_DB.toDomainModel().copy(id = "random1234")
            }
        }
    }

    @Test
    fun `getProfile when userApi throws exception`() {
        val exception = RuntimeException("fail")
        Mockito.`when`(userApi.getUser()).thenThrow(exception)
        Mockito.`when`(databaseApi.getProfile(ArgumentMatchers.anyString())).thenReturn(Single.just(FAKE_OTHER_USER_DB))
        with(
                getProfileUseCase.execute(Action.GetProfileAction(null))
                        .test()
        ) {
            awaitTerminalEvent(5L, TimeUnit.SECONDS)
            assertError(exception)
        }
    }

}