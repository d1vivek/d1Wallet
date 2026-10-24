package com.d1vivek.wallet.data.source;

import com.d1vivek.wallet.domain.error.DataUnavailableException;

public final class SimulatedLatency {

    private final long delayMillis;

    public SimulatedLatency(long delayMillis) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must not be negative");
        }
        this.delayMillis = delayMillis;
    }

    public void pause() throws DataUnavailableException {
        if (delayMillis == 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataUnavailableException("Interrupted while waiting for mock data", e);
        }
    }
}
