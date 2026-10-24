package com.d1vivek.wallet.presentation.wallet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.d1vivek.wallet.R;
import com.d1vivek.wallet.databinding.ItemAccountBinding;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.presentation.common.MoneyFormatter;

import java.util.Locale;

class AccountAdapter extends ListAdapter<Account, AccountAdapter.ViewHolder> {

    interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    private final MoneyFormatter moneyFormatter;
    private final Locale locale;
    private final OnAccountClickListener listener;

    AccountAdapter(MoneyFormatter moneyFormatter, Locale locale, OnAccountClickListener listener) {
        super(DIFF);
        this.moneyFormatter = moneyFormatter;
        this.locale = locale;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccountBinding binding = ItemAccountBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccountBinding binding;

        ViewHolder(ItemAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Account account) {
            String currency = binding.getRoot().getContext().getString(
                    R.string.account_currency,
                    account.getCurrency().getCurrencyCode(),
                    account.getCurrency().getDisplayName(locale));
            String balance = moneyFormatter.format(account.getBalance(), account.getCurrency());

            binding.accountName.setText(account.getName());
            binding.accountCurrency.setText(currency);
            binding.accountBalance.setText(balance);
            binding.getRoot().setContentDescription(binding.getRoot().getContext().getString(
                    R.string.account_item_description, account.getName(), currency, balance));
            binding.getRoot().setOnClickListener(v -> listener.onAccountClick(account));
        }
    }

    private static final DiffUtil.ItemCallback<Account> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
            return oldItem.equals(newItem);
        }
    };
}
