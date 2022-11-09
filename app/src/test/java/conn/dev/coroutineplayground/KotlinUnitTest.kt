package conn.dev.coroutineplayground

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors

import kotlinx.coroutines.guava.await
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class KotlinUnitTest {
    private val mLatch = CountDownLatch(1)
    private val mMainExecutor: Executor = Executors.newSingleThreadExecutor()

    @Test
    fun javaCodeInKotlin() {
        val fragment = WebFragment()
        val cookieManagerFuture = fragment.cookieManager

        Futures.addCallback(
            cookieManagerFuture,
            object : FutureCallback<WebFragment.CookieManager> {
                override fun onSuccess(cookieManager: WebFragment.CookieManager) {
                    val setCookieFuture =
                        cookieManager.setCookie("https://sadchonks.com", "foo=bar123")
                    Futures.addCallback(setCookieFuture, object : FutureCallback<Void?> {
                        override fun onSuccess(v: Void?) {
                            val cookieFuture = cookieManager.getCookie("https://sadchonks.com")
                            Futures.addCallback(cookieFuture, object : FutureCallback<String> {
                                override fun onSuccess(value: String) {
                                    assert("foo=bar123" == value)
                                    mLatch.countDown()
                                }

                                override fun onFailure(thrown: Throwable) {
                                    // Oh no!
                                }
                            }, mMainExecutor)
                        }

                        override fun onFailure(thrown: Throwable) {
                            // Oh no!
                        }
                    }, mMainExecutor)
                }

                override fun onFailure(t: Throwable) {
                    // Oh no!
                }
            },
            mMainExecutor
        )

        mLatch.await()
    }

    @Test
    fun coroutineWithAwait() {
        val fragment = WebFragment()

        runBlocking {
            val cookieManager = fragment.cookieManager.await()

            cookieManager.setCookie("https://sadchonks.com", "foo=bar123").await()
            val value = cookieManager.getCookie("https://sadchonks.com").await()

            assert(value == "foo=bar123")
        }
    }

    @Test
    fun coroutineWithAwait_isBroken() {
        val fragment = WebFragment()
        fragment.setIsBroken(true)

        var caughtException = false

        runBlocking {
            try {
                val cookieManager = fragment.cookieManager.await()

                cookieManager.setCookie("https://sadchonks.com", "foo=bar123").await()
                val value = cookieManager.getCookie("https://sadchonks.com").await()

                assert(false)  // Shouldn't reach here, we got broken!
            } catch (e: ArithmeticException) {
                caughtException = true
            }
        }

        assert(caughtException)
    }
}