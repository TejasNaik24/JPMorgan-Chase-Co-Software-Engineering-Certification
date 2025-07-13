package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionHandler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    private final DatabaseConduit databaseConduit;
    private final IncentiveQuerier incentiveQuerier;

    public TransactionHandler(DatabaseConduit databaseConduit, IncentiveQuerier incentiveQuerier) {
        this.databaseConduit = databaseConduit;
        this.incentiveQuerier = incentiveQuerier;
    }

    public void handleTransaction(Transaction transaction) {
        logger.info("⚙️ Handling transaction: {}", transaction);

        if (!databaseConduit.isValid(transaction)) {
            logger.warn("❌ Transaction is invalid and will not be processed.");
            return;
        }

        Incentive incentive = incentiveQuerier.query(transaction);
        float incentiveAmount = (incentive != null) ? incentive.getAmount() : 0.0f;
        transaction.setIncentive(incentiveAmount);

        databaseConduit.save(transaction);

        logger.info("✅ Transaction processed successfully with incentive: {}", incentiveAmount);
    }
}