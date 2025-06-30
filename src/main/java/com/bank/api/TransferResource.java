package com.bank.api;

import com.bank.api.dto.TransferRequest;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/transfers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferResource {

    @Inject
    AccountService accountService;

    @POST
    @Transactional
    public Response transfer(@Valid TransferRequest request) {
        accountService.transfer(request.fromAccountNumber(), request.toAccountNumber(), request.amount());
        return Response.ok().entity(new Message("Transfer successful")).build();
    }

    public record Message(String message) {}
}