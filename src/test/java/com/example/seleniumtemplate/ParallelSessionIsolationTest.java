package com.example.seleniumtemplate;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.support.TestBase;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class ParallelSessionIsolationTest extends TestBase {

    private static final ConcurrentHashMap<String, CapturedSession> CAPTURES = new ConcurrentHashMap<>();
    private static final CyclicBarrier BOTH_READY = new CyclicBarrier(2);

    @Test
    public void workerOneKeepsOwnSession() throws Exception {
        verifyIsolation("workerOne");
    }

    @Test
    public void workerTwoKeepsOwnSession() throws Exception {
        verifyIsolation("workerTwo");
    }

    private void verifyIsolation(String workerName) throws Exception {
        ApplicationManager app = ApplicationContext.current();
        BrowserSession session = app.session();
        WebDriver driver = app.driver();

        assertThat(ApplicationContext.current()).isSameAs(app);
        assertThat(session.driver()).isSameAs(driver);

        CAPTURES.put(workerName, new CapturedSession(app, session, driver));

        awaitPeer();

        CapturedSession peer = peerCapture(workerName);
        assertThat(app).isNotSameAs(peer.applicationManager());
        assertThat(session).isNotSameAs(peer.session());
        assertThat(driver).isNotSameAs(peer.driver());
        assertThat(ApplicationContext.current()).isSameAs(app);
    }

    private static void awaitPeer() throws Exception {
        try {
            BOTH_READY.await(60, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            throw new IllegalStateException(
                    "Timed out waiting for the peer parallel test method. "
                            + "Ensure TestNG runs this class with parallel=\"methods\" and thread-count >= 2.",
                    ex);
        }
    }

    private static CapturedSession peerCapture(String workerName) {
        return CAPTURES.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(workerName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Peer capture is missing for worker: " + workerName));
    }

    private record CapturedSession(
            ApplicationManager applicationManager,
            BrowserSession session,
            WebDriver driver
    ) {
    }
}
