package com.github.kazuki43zoo.domain.service.account;

import com.github.kazuki43zoo.domain.model.Account;

public interface PasswordSharedService {

    void validatePassword(String rawPassword, Account account);

    void countUpPasswordFailureCount(String failedAccountId);

    void clearPasswordLock(Account account);

}