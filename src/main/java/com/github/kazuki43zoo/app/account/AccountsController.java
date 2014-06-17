package com.github.kazuki43zoo.app.account;

import javax.inject.Inject;

import org.dozer.Mapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.terasoluna.gfw.common.message.ResultMessages;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenContext;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

import com.github.kazuki43zoo.domain.model.Account;
import com.github.kazuki43zoo.domain.model.AccountAuthority;
import com.github.kazuki43zoo.domain.repository.account.AccountsSearchCriteria;
import com.github.kazuki43zoo.domain.service.account.AccountService;

@TransactionTokenCheck("accounts")
@RequestMapping("accounts")
@Controller
public class AccountsController {

    @Inject
    Mapper beanMapper;

    @Inject
    AccountService accountService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(@Validated AccountsSearchQuery query, BindingResult bindingResult,
            @PageableDefault(size = 15) Pageable pageable, Model model) {
        if (bindingResult.hasErrors()) {
            return "account/list";
        }
        AccountsSearchCriteria criteria = beanMapper.map(query, AccountsSearchCriteria.class);
        Page<Account> page = accountService.searchAccounts(criteria, pageable);
        model.addAttribute("page", page);
        return "account/list";
    }

    @TransactionTokenCheck(value = "delete", type = TransactionTokenType.BEGIN)
    @RequestMapping(value = "{accountUuid}", method = RequestMethod.GET)
    public String view(@PathVariable("accountUuid") String accountUuid, Model model) {
        Account account = accountService.getAccount(accountUuid);
        model.addAttribute(account);
        return "account/view";
    }

    @TransactionTokenCheck(value = "create", type = TransactionTokenType.BEGIN)
    @RequestMapping(method = RequestMethod.GET, params = "form=create")
    public String createForm(AccountForm form) {
        return "account/create";
    }

    @TransactionTokenCheck(value = "create")
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Validated AccountForm form, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes, TransactionTokenContext transactionTokenContext) {
        if (bindingResult.hasErrors()) {
            return createForm(form);
        }

        Account inputAccount = beanMapper.map(form, Account.class);
        System.out.println(inputAccount.getAuthorities());
        for (String authority : form.getAuthorities()) {
            inputAccount.addAuthority(new AccountAuthority(null, authority));
        }
        try {
            accountService.create(inputAccount);
        } catch (DuplicateKeyException e) {
            model.addAttribute(ResultMessages.danger().add("e.xx.ac.2001"));
            return createForm(form);
        }

        redirectAttributes.addFlashAttribute(ResultMessages.success().add("i.xx.ac.0001"));

        transactionTokenContext.removeToken();

        return "redirect:/accounts";
    }

    @TransactionTokenCheck(value = "edit", type = TransactionTokenType.BEGIN)
    @RequestMapping(value = "{accountUuid}", method = RequestMethod.GET, params = "form=edit")
    public String editForm(@PathVariable("accountUuid") String accountUuid, AccountForm form,
            Model model) {
        Account account = accountService.getAccount(accountUuid);
        model.addAttribute(account);
        beanMapper.map(account, form);
        for (AccountAuthority accountAuthority : account.getAuthorities()) {
            form.addAuthority(accountAuthority.getAuthority());
        }
        form.setPassword(null);
        return "account/edit";
    }

    @TransactionTokenCheck(value = "edit")
    @RequestMapping(value = "{accountUuid}", method = RequestMethod.PUT)
    public String edit(@PathVariable("accountUuid") String accountUuid,
            @Validated AccountForm form, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes, TransactionTokenContext transactionTokenContext) {

        if (bindingResult.hasErrors()) {
            return editRedo(accountUuid, form, model);
        }

        Account inputAccount = beanMapper.map(form, Account.class);
        inputAccount.setAccountUuid(accountUuid);
        for (String authority : form.getAuthorities()) {
            inputAccount.addAuthority(new AccountAuthority(accountUuid, authority));
        }
        try {
            accountService.change(inputAccount);
        } catch (DuplicateKeyException e) {
            model.addAttribute(ResultMessages.danger().add("e.xx.ac.2001"));
            return editRedo(accountUuid, form, model);
        }

        redirectAttributes.addFlashAttribute(ResultMessages.success().add("i.xx.ac.0002"));
        redirectAttributes.addAttribute("accountUuid", accountUuid);

        transactionTokenContext.removeToken();

        return "redirect:/accounts/{accountUuid}";
    }

    private String editRedo(String accountUuid, AccountForm form, Model model) {
        Account account = accountService.getAccount(accountUuid);
        model.addAttribute(account);
        return "account/edit";
    }

    @TransactionTokenCheck(value = "delete")
    @RequestMapping(value = "{accountUuid}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("accountUuid") String accountUuid,
            RedirectAttributes redirectAttributes, TransactionTokenContext transactionTokenContext) {

        accountService.delete(accountUuid);

        redirectAttributes.addFlashAttribute(ResultMessages.success().add("i.xx.ac.0003"));

        transactionTokenContext.removeToken();

        return "redirect:/accounts";
    }

}