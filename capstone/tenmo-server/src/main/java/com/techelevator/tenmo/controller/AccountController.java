package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.dao.AccountDao;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("account")
public class AccountController {

    private final AccountDao accountDao;

    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    //methods for adding/subtracting from balance may not be necessary in the controller

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public Account findAccountByUserId(@PathVariable int userId) {
        //need to rename method below in AccountDao so that it is clear we are searching for account and not user
        return accountDao.findUserById(userId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(path = "/{accountId}", method = RequestMethod.GET)
    //could rename method below in AccountDao to make it clearer to: findAccountByAccountId
    public Account findAccountById(@PathVariable int accountId) {
        return accountDao.findAccountById(accountId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(path = "/user/{userId}/balance", method = RequestMethod.GET)
    //wondering if we should use @PathVariable Principal principal?
    public BigDecimal getBalance(@PathVariable int userId) {
        return accountDao.getBalance(userId);
    }

    //FOR BELOW: NEED accountDao.create/update/delete method to create account
    //Not sure if we need those yet

//    @ResponseStatus(HttpStatus.CREATED)
//    @RequestMapping(path = "/", method = RequestMethod.POST)
//    public boolean create(@Valid @RequestBody Account account) {
//        return accountDao.create(account);
//    }

//    @RequestMapping(path = "/{accountId}", method = RequestMethod.PUT)
//    public boolean update(@Valid @PathVariable Long accountId, @RequestBody Account account) {
//        return accountDao.update(accountId, account);
//    }
//
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @RequestMapping(path = "/{accountId}", method = RequestMethod.DELETE)
//    public boolean delete(@Valid @PathVariable Long id) {
//        return accountDao.delete(accountId);
//    }

}