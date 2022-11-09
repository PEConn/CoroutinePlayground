package conn.dev.coroutineplayground;

import androidx.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebFragment {
    private final ListeningScheduledExecutorService mExecutorService;
    private boolean mIsBroken;

    public class CookieManager {
        private final ListeningScheduledExecutorService mExecutorService;
        private @Nullable String mValue;

        public CookieManager(ListeningScheduledExecutorService executorService) {
            mExecutorService = executorService;
        }

        public ListenableFuture<Void> setCookie(String site, String value) {
            mValue = value;
            return completeDelayed(mExecutorService, null);
        }

        public ListenableFuture<String> getCookie(String site) {
            return completeDelayed(mExecutorService, mValue);
        }
    }

    public WebFragment() {
        mExecutorService = MoreExecutors.listeningDecorator(
                Executors.newSingleThreadScheduledExecutor());
    }

    public void setIsBroken(boolean broken) {
        mIsBroken = broken;
    }

    public ListenableFuture<CookieManager> getCookieManager() {
        if (mIsBroken) {
            return mExecutorService.schedule(() -> {
                throw new ArithmeticException("Well, something went wrong.");
            }, 1, TimeUnit.SECONDS);
        } else {
            return completeDelayed(mExecutorService, new CookieManager(mExecutorService));
        }
    }

    private static<T> ListenableFuture<T> completeDelayed(
            ListeningScheduledExecutorService service, T thing) {
        return service.schedule(() -> thing, 1, TimeUnit.SECONDS);
    }
}
