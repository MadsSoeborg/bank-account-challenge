package com.bank.api;

import com.bank.api.dto.AccountResponse;
import com.bank.api.dto.BalanceResponse;
import com.bank.api.dto.CreateAccountRequest;
import com.bank.api.dto.DepositRequest;
import com.bank.api.dto.TransactionResponse;
import com.bank.api.dto.WithdrawRequest;
import com.bank.model.Account;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.math.BigDecimal;
import java.net.URI;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @GET
    @Path("/{accountNumber}/balance")
    public Response getBalance(@PathParam("accountNumber") String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        BalanceResponse response = new BalanceResponse(accountNumber, balance);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{accountNumber}/transactions")
    public Response getTransactions(
            @PathParam("accountNumber") String accountNumber,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {
        var transactions = accountService.getTransactionHistory(accountNumber, pageIndex, pageSize)
                .stream()
                .map(t -> new TransactionResponse(
                        t.accountNumber,
                        t.amount,
                        t.type,
                        t.timestamp,
                        t.relatedAccountNumber))
                .toList();

        return Response.ok(transactions).build();
    }

    @POST
    public Response createAccount(@Valid CreateAccountRequest request, @Context UriInfo uriInfo) {
        Account newAccount = accountService.createAccount(request.firstName(), request.lastName());

        AccountResponse response = mapToResponse(newAccount);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(newAccount.accountNumber).build();
        return Response.created(createdUri).entity(response).build();
    }

    @POST
    @Path("/{accountNumber}/deposit")
    @Transactional
    public Response deposit(@PathParam("accountNumber") String accountNumber, @Valid DepositRequest request) {
        Account updatedAccount = accountService.deposit(accountNumber, request.amount());

        AccountResponse response = mapToResponse(updatedAccount);

        return Response.ok(response).build();
    }

    @POST
    @Path("/{accountNumber}/withdraw")
    @Transactional
    public Response withdraw(@PathParam("accountNumber") String accountNumber,
            @Valid WithdrawRequest request) {
        Account updatedAccount = accountService.withdraw(accountNumber, request.amount());

        AccountResponse response = mapToResponse(updatedAccount);

        return Response.ok(response).build();
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.accountNumber,
                account.firstName,
                account.lastName,
                account.balance);
    }
}