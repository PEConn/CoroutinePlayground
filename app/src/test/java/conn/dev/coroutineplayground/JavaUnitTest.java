package conn.dev.coroutineplayground;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import conn.dev.coroutineplayground.WebFragment.CookieManager;

public class JavaUnitTest {
    private final CountDownLatch mLatch = new CountDownLatch(1);
    private final Executor mMainExecutor = Executors.newSingleThreadExecutor();

    @Test
    public void useFutures() throws InterruptedException {
        WebFragment fragment = new WebFragment();
        ListenableFuture<CookieManager> cookieManagerFuture = fragment.getCookieManager();

        Futures.addCallback(cookieManagerFuture, new FutureCallback<CookieManager>() {
            @Override
            public void onSuccess(CookieManager cookieManager) {
                ListenableFuture<Void> setCookieFuture =
                        cookieManager.setCookie("https://sadchonks.com", "foo=bar123");
                Futures.addCallback(setCookieFuture, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        ListenableFuture<String> cookieFuture =
                                cookieManager.getCookie("https://sadchonks.com");
                        Futures.addCallback(cookieFuture, new FutureCallback<String>() {
                            @Override
                            public void onSuccess(String value) {
                                assert "foo=bar123".equals(value);
                                mLatch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable thrown) {
                                // Oh no!
                            }
                        }, mMainExecutor);
                    }

                    @Override
                    public void onFailure(Throwable thrown) {
                        // Oh no!
                    }
                }, mMainExecutor);
            }

            @Override
            public void onFailure(Throwable t) {
                // Oh no!
            }
        }, mMainExecutor);

        mLatch.await();
    }

    @Test
    public void useFutures_failures() throws InterruptedException {
        WebFragment fragment = new WebFragment();
        fragment.setIsBroken(true);
        ListenableFuture<CookieManager> cookieManagerFuture = fragment.getCookieManager();

        Futures.addCallback(cookieManagerFuture, new FutureCallback<CookieManager>() {
            @Override
            public void onSuccess(CookieManager cookieManager) {
                // I did not expect this to succeed.
            }

            @Override
            public void onFailure(Throwable t) {
                mLatch.countDown();
            }
        }, mMainExecutor);

        mLatch.await();
    }
}
