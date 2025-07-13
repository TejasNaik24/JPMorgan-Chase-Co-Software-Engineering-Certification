package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConduit {

    private final UserRepository userRepo;
    private final TransactionRecordRepository transactionRepo;

    public DatabaseConduit(UserRepository userRepo, TransactionRecordRepository transactionRepo) {
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
    }

    public void saveUser(UserRecord user) {
        userRepo.save(user);
    }

    public void processTransaction(Transaction tx) {
        // Assumes transaction is already validated

        UserRecord sender = findUser(tx.getSenderId());
        UserRecord recipient = findUser(tx.getRecipientId());

        // Save transaction record
        TransactionRecord record = new TransactionRecord(
                sender,
                recipient,
                tx.getAmount(),
                tx.getIncentive()
        );
        transactionRepo.save(record);

        // Adjust balances
        sender.setBalance(sender.getBalance() - tx.getAmount());
        saveUser(sender);

        recipient.setBalance(recipient.getBalance() + tx.getAmount() + tx.getIncentive());
        saveUser(recipient);
    }

    public boolean isTransactionValid(Transaction tx) {
        UserRecord sender = findUser(tx.getSenderId());
        if (sender == null) return false;

        UserRecord recipient = findUser(tx.getRecipientId());
        if (recipient == null) return false;

        return sender.getBalance() >= tx.getAmount();
    }

    public UserRecord findUser(Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public float getUserBalance(Long userId) {
        UserRecord user = findUser(userId);
        return (user != null) ? user.getBalance() : 0;
    }
}
