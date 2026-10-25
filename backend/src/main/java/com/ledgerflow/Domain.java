package com.ledgerflow;

enum Role { ADMIN, OPERATOR, VIEWER }
enum AccountType { MERCHANT, RESERVE }
enum Direction { DEBIT, CREDIT }
enum TransferStatus { PENDING, POSTED, REJECTED, REFUNDED }
enum EntryType { OPENING, TRANSFER, REFUND }
enum ReconciliationStatus { MATCHED, AMOUNT_MISMATCH, CURRENCY_MISMATCH, UNKNOWN_REFERENCE, NOT_POSTED, DUPLICATE }
