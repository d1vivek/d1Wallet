package com.d1vivek.wallet.presentation.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class UiState<T> {

    public enum Status { LOADING, SUCCESS, EMPTY, ERROR }

    public enum Error { UNAVAILABLE, NOT_FOUND, SIGNED_OUT }

    private final Status status;
    @Nullable private final T data;
    @Nullable private final Error error;

    private UiState(Status status, @Nullable T data, @Nullable Error error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, null);
    }

    public static <T> UiState<T> success(@NonNull T data) {
        return new UiState<>(Status.SUCCESS, data, null);
    }

    public static <T> UiState<T> empty(@Nullable T data) {
        return new UiState<>(Status.EMPTY, data, null);
    }

    public static <T> UiState<T> error(@NonNull Error error) {
        return new UiState<>(Status.ERROR, null, error);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public Error getError() {
        return error;
    }
}
