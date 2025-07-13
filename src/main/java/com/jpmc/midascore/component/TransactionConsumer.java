package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TransactionConsumer {

    private final UserRepository userRepo;
    private final TransactionRecordRepository transactionRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionConsumer(UserRepository userRepo, TransactionRecordRepository transactionRepo) {
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
    }

    @KafkaListener(topics = "test-topic", groupId = "midas-core")
    public void onMessage(Transaction tx) {
        long senderId = tx.getSenderId();
        long recipientId = tx.getRecipientId();
        float amount = tx.getAmount();

        Optional<UserRecord> senderOpt = userRepo.findById(senderId);
        Optional<UserRecord> recipientOpt = userRepo.findById(recipientId);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            System.out.println("❌ Invalid sender or recipient. Transaction discarded.");
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        if (sender.getBalance() < amount) {
            System.out.println("⚠️ Sender has insufficient balance. Transaction discarded.");
            return;
        }

        // Call external incentive service
        Incentive incentive = restTemplate.postForObject(
            "http://localhost:8080/incentive",
            tx,
            Incentive.class
        );
        float bonus = (incentive != null) ? incentive.getAmount() : 0.0f;

        // Update balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + bonus);

        userRepo.save(sender);
        userRepo.save(recipient);

        // Record transaction
        TransactionRecord record = new TransactionRecord(sender, recipient, amount, bonus);
        transactionRepo.save(record);

        System.out.printf("✅ Transaction processed. Incentive applied: %.2f%n", bonus);
    }
}