package com.example.seleniumtemplate.support.reporting;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Centralized failure diagnostics for Allure.
 * Collects artifacts in {@link #afterInvocation} so UI capture runs before {@code @AfterMethod}
 * browser teardown. Never rethrows attachment errors.
 */
public final class TestFailureListener implements IInvokedMethodListener, ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestFailureListener.class);

    @Override
    public void afterInvocation(
            @Nullable IInvokedMethod method,
            @Nullable ITestResult testResult) {
        if (method == null || testResult == null || !method.isTestMethod()) {
            return;
        }
        try {
            if (!isSuccessful(testResult)) {
                FailureDiagnostics.attachAvailable();
            }
        } catch (RuntimeException | Error ex) {
            log.warn("Failure diagnostics raised unexpectedly: {}", ex.toString());
        } finally {
            FailureDiagnostics.clearThreadState();
        }
    }

    @Override
    public void onTestSuccess(@Nullable ITestResult result) {
        FailureDiagnostics.clearThreadState();
    }

    @Override
    public void onTestFailure(@Nullable ITestResult result) {
        FailureDiagnostics.clearThreadState();
    }

    @Override
    public void onTestSkipped(@Nullable ITestResult result) {
        FailureDiagnostics.clearThreadState();
    }

    private static boolean isSuccessful(ITestResult result) {
        return result.getStatus() == ITestResult.SUCCESS;
    }
}
