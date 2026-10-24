package com.d1vivek.wallet.presentation.wallet;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.d1vivek.wallet.R;
import com.d1vivek.wallet.app.WalletApplication;
import com.d1vivek.wallet.databinding.ActivityWalletBinding;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.presentation.common.MoneyFormatter;
import com.d1vivek.wallet.presentation.common.SystemBarInsets;
import com.d1vivek.wallet.presentation.common.UiState;
import com.d1vivek.wallet.presentation.login.LoginActivity;
import com.d1vivek.wallet.presentation.statement.StatementActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private ActivityWalletBinding binding;
    private WalletViewModel viewModel;
    private AccountAdapter adapter;
    private AlertDialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.applyAsPadding(binding.getRoot());

        viewModel = new ViewModelProvider(this,
                WalletApplication.container(this).walletViewModelFactory())
                .get(WalletViewModel.class);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        adapter = new AccountAdapter(new MoneyFormatter(locale), locale, this::openStatement);
        binding.accountList.setAdapter(adapter);
        binding.stateMessage.actionButton.setOnClickListener(v -> viewModel.retry());

        viewModel.getState().observe(this, this::render);
    }

    @Override
    protected void onDestroy() {
        if (logoutDialog != null) {
            logoutDialog.dismiss();
        }
        super.onDestroy();
    }

    private void confirmLogout() {
        if (logoutDialog != null && logoutDialog.isShowing()) {
            return;
        }
        logoutDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setNegativeButton(R.string.logout_confirm_cancel, null)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> viewModel.logout())
                .show();
    }

    private void openStatement(Account account) {
        startActivity(StatementActivity.newIntent(this, account.getId()));
    }

    private void render(UiState<List<Account>> state) {
        UiState.Status status = state.getStatus();
        if (status == UiState.Status.ERROR && state.getError() == UiState.Error.SIGNED_OUT) {
            startActivity(LoginActivity.newSignedOutIntent(this));
            finish();
            return;
        }

        binding.progress.setVisibility(status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
        binding.accountList.setVisibility(status == UiState.Status.SUCCESS ? View.VISIBLE : View.GONE);
        binding.walletAccounts.setVisibility(status == UiState.Status.SUCCESS ? View.VISIBLE : View.GONE);

        switch (status) {
            case SUCCESS:
                adapter.submitList(state.getData());
                binding.stateMessage.getRoot().setVisibility(View.GONE);
                assert state.getData() != null;
                binding.walletAccounts.setText(getString(R.string.wallet_accounts, String.valueOf(state.getData().size())));
                break;
            case EMPTY:
                showMessage(R.string.wallet_empty_title, R.string.wallet_empty_message);
                break;
            case ERROR:
                showMessage(R.string.wallet_error_title, R.string.wallet_error_message);
                break;
            case LOADING:
            default:
                binding.stateMessage.getRoot().setVisibility(View.GONE);
                break;
        }
    }

    private void showMessage(int titleRes, int messageRes) {
        binding.stateMessage.getRoot().setVisibility(View.VISIBLE);
        binding.stateMessage.title.setText(titleRes);
        binding.stateMessage.message.setText(messageRes);
    }
}
