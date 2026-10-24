package com.d1vivek.wallet.presentation.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.function.Supplier;

public final class ViewModelFactory<VM extends ViewModel> implements ViewModelProvider.Factory {

    private final Class<VM> viewModelClass;
    private final Supplier<VM> creator;

    public ViewModelFactory(Class<VM> viewModelClass, Supplier<VM> creator) {
        this.viewModelClass = viewModelClass;
        this.creator = creator;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(viewModelClass)) {
            throw new IllegalArgumentException("Unexpected ViewModel class " + modelClass.getName());
        }
        return (T) creator.get();
    }
}
