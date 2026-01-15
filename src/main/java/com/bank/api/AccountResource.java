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

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Account Operations", description = "Create accounts, check balances, and view history")
public class AccountResource {

        @Inject
        AccountService accountService;

        @GET
        @Path("/{accountNumber}/balance")
        @Operation(summary = "Get account balance", description = "Retrieve the current balance for a specific account")
        @APIResponses({
                        @APIResponse(responseCode = "200", description = "Balance retrieved successfully"),
                        @APIResponse(responseCode = "404", description = "Account not found")
        })
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

                var entries = accountService.getTransactionHistory(accountNumber, pageIndex, pageSize);

                var responseList = entries
                                .stream()
                                .map(t -> new TransactionResponse(
                                                t.account.accountNumber,
                                                t.amount,
                                                t.type,
                                                java.time.LocalDateTime.ofInstant(t.timestamp,
                                                                java.time.ZoneId.systemDefault()),
                                                t.referenceInfo))
                                .toList();

                return Response.ok(responseList).build();
        }

        @POST
        @Operation(summary = "Create new account", description = "Create a new bank account with the provided customer information")
        @APIResponses({
                        @APIResponse(responseCode = "201", description = "Account created successfully"),
                        @APIResponse(responseCode = "400", description = "Invalid request data")
        })
        public Response createAccount(@Valid CreateAccountRequest request, @Context UriInfo uriInfo) {
                Account newAccount = accountService.createAccount(request.firstName(), request.lastName());

                AccountResponse response = mapToResponse(newAccount);

                URI createdUri = uriInfo.getAbsolutePathBuilder().path(newAccount.accountNumber).build();
                return Response.created(createdUri).entity(response).build();
        }

        @POST
        @Path("/{accountNumber}/deposit")
        @Transactional
        @Operation(summary = "Deposit funds", description = "Deposit money into a specific account")
        @APIResponses({
                        @APIResponse(responseCode = "200", description = "Deposit completed successfully"),
                        @APIResponse(responseCode = "400", description = "Invalid deposit amount"),
                        @APIResponse(responseCode = "404", description = "Account not found")
        })
        public Response deposit(@PathParam("accountNumber") String accountNumber, @Valid DepositRequest request) {
                Account updatedAccount = accountService.deposit(accountNumber, request.amount());

                AccountResponse response = mapToResponse(updatedAccount);

                return Response.ok(response).build();
        }

        @POST
        @Path("/{accountNumber}/withdraw")
        @Transactional
        @Operation(summary = "Withdraw funds", description = "Withdraw money from a specific account")
        @APIResponses({
                        @APIResponse(responseCode = "200", description = "Withdrawal completed successfully"),
                        @APIResponse(responseCode = "400", description = "Invalid withdrawal amount"),
                        @APIResponse(responseCode = "402", description = "Insufficient funds"),
                        @APIResponse(responseCode = "404", description = "Account not found")
        })
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