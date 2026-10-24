package com.d1vivek.wallet.presentation.statement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.d1vivek.wallet.R;
import com.d1vivek.wallet.databinding.ItemTransactionBinding;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.model.TransactionType;
import com.d1vivek.wallet.presentation.common.DateFormatter;
import com.d1vivek.wallet.presentation.common.MoneyFormatter;

class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.ViewHolder> {

    private final MoneyFormatter moneyFormatter;
    private final DateFormatter dateFormatter;

    TransactionAdapter(MoneyFormatter moneyFormatter, DateFormatter dateFormatter) {
        super(DIFF);
        this.moneyFormatter = moneyFormatter;
        this.dateFormatter = dateFormatter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @StringRes
    static int labelFor(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> R.string.transaction_type_deposit;
            case TRANSFER_IN -> R.string.transaction_type_transfer_in;
            case REFUND -> R.string.transaction_type_refund;
            case PAYMENT -> R.string.transaction_type_payment;
            case TRANSFER_OUT -> R.string.transaction_type_transfer_out;
            case WITHDRAWAL -> R.string.transaction_type_withdrawal;
            default -> R.string.transaction_type_fee;
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Transaction transaction) {
            Context context = binding.getRoot().getContext();
            String type = context.getString(labelFor(transaction.getType()));
            String amount = moneyFormatter.formatSigned(transaction.getAmount(), transaction.getCurrency());
            String dateTime = dateFormatter.formatDateTime(transaction.getTimestamp());
            String balanceAfter = moneyFormatter.format(transaction.getBalanceAfter(), transaction.getCurrency());
            boolean credit = transaction.getType().isCredit();

            binding.description.setText(transaction.getDescription());
            binding.type.setText(type);
            binding.dateTime.setText(dateTime);
            binding.amount.setText(amount);
            binding.amount.setTextColor(ContextCompat.getColor(context,
                    credit ? R.color.amount_credit : R.color.amount_debit));
            binding.balanceAfter.setText(context.getString(R.string.transaction_balance_after, balanceAfter));

            // Screen readers get one sentence per row, with "received"/"spent" instead of a symbol.
            binding.getRoot().setContentDescription(context.getString(
                    credit ? R.string.transaction_item_description_credit
                            : R.string.transaction_item_description_debit,
                    type,
                    transaction.getDescription(),
                    moneyFormatter.format(transaction.getAmount().abs(), transaction.getCurrency()),
                    dateTime,
                    balanceAfter));
        }
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.equals(newItem);
        }
    };
}
