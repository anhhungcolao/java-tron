package org.tron.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.Parameter.NodeConstant;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.DupTransactionException;
import org.tron.core.exception.TaposException;
import org.tron.core.exception.ValidateBandwidthException;
import org.tron.core.exception.ValidateSignatureException;

@Slf4j
public class PendingManager implements AutoCloseable {

  @Getter
  static List<TransactionCapsule> tmpTransactions = new ArrayList<>();
  Manager dbManager;

  public PendingManager(Manager db) {
    this.dbManager = db;
    tmpTransactions.addAll(db.getPendingTransactions());
    db.getPendingTransactions().clear();
    db.getDialog().reset();
  }

  @Override
  public void close() {
    AtomicLong recycledPendingTransactionSize = new AtomicLong(0);
    this.tmpTransactions.stream()
        .filter(
            trx -> dbManager.getTransactionStore().get(trx.getTransactionId().getBytes()) == null)
        .forEach(trx -> {
          try {
            if (recycledPendingTransactionSize.get() > NodeConstant.MAX_TRANSACTION_PENDING) {
              return;
            }
            recycledPendingTransactionSize.incrementAndGet();
            dbManager.pushTransactions(trx);
          } catch (ValidateSignatureException e) {
            logger.error(e.getMessage(), e);
          } catch (ContractValidateException e) {
            logger.error(e.getMessage(), e);
          } catch (ContractExeException e) {
            logger.error(e.getMessage(), e);
          } catch (ValidateBandwidthException e) {
            logger.error(e.getMessage(), e);
          } catch (DupTransactionException e) {
            logger.error("pending manager: dup trans", e);
          } catch (TaposException e) {
            logger.error("pending manager: tapos exception", e);
          }
        });
    dbManager.getPoppedTransactions().stream()
        .filter(
            trx -> dbManager.getTransactionStore().get(trx.getTransactionId().getBytes()) == null)
        .forEach(trx -> {
          try {
            if (recycledPendingTransactionSize.get() > NodeConstant.MAX_TRANSACTION_PENDING) {
              return;
            }
            recycledPendingTransactionSize.incrementAndGet();
            dbManager.pushTransactions(trx);
          } catch (ValidateSignatureException e) {
            logger.debug(e.getMessage(), e);
          } catch (ContractValidateException e) {
            logger.debug(e.getMessage(), e);
          } catch (ContractExeException e) {
            logger.debug(e.getMessage(), e);
          } catch (ValidateBandwidthException e) {
            logger.debug(e.getMessage(), e);
          } catch (DupTransactionException e) {
            logger.debug("pending manager: dup trans", e);
          } catch (TaposException e) {
            logger.debug("pending manager: tapos exception", e);
          }
        });
    dbManager.getPoppedTransactions().clear();
    tmpTransactions.clear();
  }
}
