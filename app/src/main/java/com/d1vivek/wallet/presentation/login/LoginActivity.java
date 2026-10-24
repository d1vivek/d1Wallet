package com.d1vivek.wallet.presentation.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.d1vivek.wallet.R;
import com.d1vivek.wallet.app.WalletApplication;
import com.d1vivek.wallet.databinding.ActivityLoginBinding;
import com.d1vivek.wallet.presentation.common.SystemBarInsets;
import com.d1vivek.wallet.presentation.wallet.WalletActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    public static Intent newSignedOutIntent(Context context) {
        return new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.applyAsPadding(binding.getRoot());

        viewModel = new ViewModelProvider(this,
                WalletApplication.container(this).loginViewModelFactory())
                .get(LoginViewModel.class);

        binding.loginButton.setOnClickListener(v -> submit());
        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });

        viewModel.getState().observe(this, this::render);

        binding.identifierInput.setText("demo@example.com");
        binding.passwordInput.setText("password123");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Added here rather than in onCreate so that restoring the typed text after rotation
        // doesn't count as an edit and wipe the validation errors.
        binding.identifierInput.addTextChangedListener(afterTextChanged(viewModel::onIdentifierEdited));
        binding.passwordInput.addTextChangedListener(afterTextChanged(viewModel::onPasswordEdited));
    }

    private void submit() {
        viewModel.submit(textOf(binding.identifierInput.getText()), textOf(binding.passwordInput.getText()));
    }

    private void render(LoginUiState state) {
        if (state.isSignedIn()) {
            startActivity(new Intent(this, WalletActivity.class));
            finish();
            return;
        }

        boolean loading = state.isLoading();
        binding.loginButton.setEnabled(!loading);
        binding.loginButton.setText(loading ? R.string.login_button_loading : R.string.login_button);
        binding.identifierLayout.setEnabled(!loading);
        binding.passwordLayout.setEnabled(!loading);
        binding.loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);

        binding.identifierLayout.setError(
                state.isIdentifierMissing() ? getString(R.string.login_error_identifier_required) : null);
        binding.passwordLayout.setError(
                state.isPasswordMissing() ? getString(R.string.login_error_password_required) : null);

        switch (state.getFormError()) {
            case INVALID_CREDENTIALS:
                showFormError(R.string.login_error_invalid_credentials);
                break;
            case UNAVAILABLE:
                showFormError(R.string.login_error_unavailable);
                break;
            case NONE:
            default:
                binding.formError.setVisibility(View.GONE);
                break;
        }
    }

    private void showFormError(int messageRes) {
        binding.formError.setText(messageRes);
        binding.formError.setVisibility(View.VISIBLE);
    }

    private static String textOf(Editable editable) {
        return editable == null ? "" : editable.toString();
    }

    private static TextWatcher afterTextChanged(Runnable action) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                action.run();
            }
        };
    }
}
