package com.d1vivek.wallet.presentation.statement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.d1vivek.wallet.R;
import com.d1vivek.wallet.app.WalletApplication;
import com.d1vivek.wallet.databinding.ActivityStatementBinding;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.AccountStatement;
import com.d1vivek.wallet.presentation.common.DateFormatter;
import com.d1vivek.wallet.presentation.common.MoneyFormatter;
import com.d1vivek.wallet.presentation.common.SystemBarInsets;
import com.d1vivek.wallet.presentation.common.UiState;
import com.d1vivek.wallet.presentation.login.LoginActivity;

import java.time.ZoneId;
import java.util.Locale;

public class StatementActivity extends AppCompatActivity {

    private static final String EXTRA_ACCOUNT_ID = "com.d1vivek.wallet.extra.ACCOUNT_ID";

    private ActivityStatementBinding binding;
    private StatementViewModel viewModel;
    private TransactionAdapter adapter;
    private MoneyFormatter moneyFormatter;
    private DateFormatter dateFormatter;

    public static Intent newIntent(Context context, String accountId) {
        return new Intent(context, StatementActivity.class).putExtra(EXTRA_ACCOUNT_ID, accountId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityStatementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.applyAsPadding(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String accountId = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);
        viewModel = new ViewModelProvider(this,
                WalletApplication.container(this).statementViewModelFactory(accountId))
                .get(StatementViewModel.class);

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        moneyFormatter = new MoneyFormatter(locale);
        dateFormatter = new DateFormatter(locale, ZoneId.systemDefault());
        adapter = new TransactionAdapter(moneyFormatter, dateFormatter);
        binding.transactionList.setAdapter(adapter);

        viewModel.getState().observe(this, this::render);
    }

    private void render(UiState<AccountStatement> state) {
        UiState.Status status = state.getStatus();
        if (status == UiState.Status.ERROR && state.getError() == UiState.Error.SIGNED_OUT) {
            startActivity(LoginActivity.newSignedOutIntent(this));
            finish();
            return;
        }

        AccountStatement statement = state.getData();
        binding.progress.setVisibility(status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
        binding.header.setVisibility(statement != null ? View.VISIBLE : View.GONE);
        binding.transactionList.setVisibility(status == UiState.Status.SUCCESS ? View.VISIBLE : View.GONE);
        if (statement != null) {
            bindHeader(statement);
            adapter.submitList(statement.transactions());
        }

        switch (status) {
            case EMPTY:
                showMessage(R.string.statement_empty_title, R.string.statement_empty_message,
                        R.string.action_back_to_accounts, v -> finish());
                break;
            case ERROR:
                if (state.getError() == UiState.Error.NOT_FOUND) {
                    showMessage(R.string.statement_not_found_title, R.string.statement_not_found_message,
                            R.string.action_back_to_accounts, v -> finish());
                } else {
                    showMessage(R.string.statement_error_title, R.string.statement_error_message,
                            R.string.action_try_again, v -> viewModel.retry());
                }
                break;
            case SUCCESS:
            case LOADING:
            default:
                binding.stateMessage.getRoot().setVisibility(View.GONE);
                break;
        }
    }

    private void bindHeader(AccountStatement statement) {
        Account account = statement.account();
        binding.accountName.setText(account.getName());
        binding.accountCurrency.setText(account.getCurrency().getCurrencyCode());
        binding.currentBalance.setText(moneyFormatter.format(account.getBalance(), account.getCurrency()));
        binding.period.setText(getString(R.string.statement_period,
                dateFormatter.formatDate(statement.from()),
                dateFormatter.formatDate(statement.to())));
    }

    private void showMessage(int titleRes, int messageRes, int actionRes, View.OnClickListener action) {
        binding.stateMessage.getRoot().setVisibility(View.VISIBLE);
        binding.stateMessage.title.setText(titleRes);
        binding.stateMessage.message.setText(messageRes);
        binding.stateMessage.actionButton.setText(actionRes);
        binding.stateMessage.actionButton.setOnClickListener(action);
    }
}
