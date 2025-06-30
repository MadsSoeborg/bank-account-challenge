package com.bank.api;

import com.bank.api.dto.BalanceResponse;
import com.bank.api.dto.CreateAccountRequest;
import com.bank.api.dto.DepositRequest;
import com.bank.exception.AccountNotFoundExceptionMapper;
import com.bank.model.Account;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

    @POST
    public Response createAccount(CreateAccountRequest request, @Context UriInfo uriInfo) {
        if (request.firstName() == null || request.lastName() == null || request.firstName().isBlank() || request.lastName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("First name and last name must be provided.").build();
        }
        Account newAccount = accountService.createAccount(request.firstName(), request.lastName());

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(newAccount.accountNumber).build();
        return Response.created(createdUri).entity(newAccount).build();
    }

    @POST
    @Path("/{accountNumber}/deposit")
    @Transactional
    public Response deposit(@PathParam("accountNumber") String accountNumber, DepositRequest request) {
        try {
            Account updatedAccount = accountService.deposit(accountNumber, request.amount());
            return Response.ok(updatedAccount).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new AccountNotFoundExceptionMapper.ErrorMessage(e.getMessage())).build();
        }
    }
}