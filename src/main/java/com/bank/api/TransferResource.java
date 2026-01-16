package com.bank.api;

import com.bank.api.dto.TransferRequest;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
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
    public Response transfer(
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @Valid TransferRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // Do nothing for now
        }

        boolean processed = accountService.transfer(
                idempotencyKey,
                request.fromAccountNumber(),
                request.toAccountNumber(),
                request.amount());

        if (processed) {
            return Response.ok().entity(new Message("Transfer successful")).build();
        } else {
            // It was a replay. We return 200 OK.
            return Response.ok()
                    .header("X-Idempotent-Replay", "true")
                    .entity(new Message("Transfer successful (previously processed)"))
                    .build();
        }
    }

    public record Message(String message) {
    }
}