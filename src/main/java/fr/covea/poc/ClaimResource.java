package fr.covea.poc;

import fr.covea.poc.model.Claim;
import fr.covea.poc.service.ClaimService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.Collection;

@Path("/claims")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Claims",
        description = "Operations for managing insurance claims, incidents, reported damages, claim status and financial assessment."
)
public class ClaimResource {

    @Inject
    ClaimService claimService;

    @GET
    @Operation(
            summary = "List all claims",
            description = "Returns all insurance claims currently stored in the in-memory repository."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of claims successfully returned",
            content = @Content(schema = @Schema(implementation = Claim.class))
    )
    public Collection<Claim> getAll() {
        return claimService.all();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get a claim by identifier",
            description = "Returns the insurance claim matching the provided technical identifier."
    )
    @APIResponse(
            responseCode = "200",
            description = "Claim successfully found",
            content = @Content(schema = @Schema(implementation = Claim.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "No claim exists for the provided identifier"
    )
    public Claim getById(
            @Parameter(
                    description = "Technical identifier of the claim to retrieve.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id
    ) {
        Claim claim = claimService.get(id);

        if (claim == null) {
            throw new NotFoundException("Claim not found with id: " + id);
        }

        return claim;
    }

    @POST
    @Operation(
            summary = "Create a claim",
            description = "Creates a new insurance claim. The id field from the request body is ignored and replaced by a generated identifier."
    )
    @APIResponse(
            responseCode = "201",
            description = "Claim successfully created",
            content = @Content(schema = @Schema(implementation = Claim.class))
    )
    public Response create(
            @Parameter(
                    description = "Insurance claim information to create.",
                    required = true
            )
            Claim claim
    ) {
        Claim createdClaim = claimService.create(claim);

        return Response
                .created(URI.create("/claims/" + createdClaim.getId()))
                .entity(claim)
                .build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update a claim",
            description = "Replaces an existing insurance claim with the provided information. The identifier from the path overrides the id from the request body."
    )
    @APIResponse(
            responseCode = "200",
            description = "Claim successfully updated",
            content = @Content(schema = @Schema(implementation = Claim.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "No claim exists for the provided identifier"
    )
    public Claim update(
            @Parameter(
                    description = "Technical identifier of the claim to update.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id,
            @Parameter(
                    description = "Updated insurance claim information.",
                    required = true
            )
            Claim claim
    ) {
        Claim updatedClaim = claimService.update(id, claim);
        if (updatedClaim == null) {
            throw new NotFoundException("Claim not found with id: " + id);
        }

        return updatedClaim;
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a claim",
            description = "Deletes the insurance claim matching the provided identifier."
    )
    @APIResponse(
            responseCode = "204",
            description = "Claim successfully deleted"
    )
    @APIResponse(
            responseCode = "404",
            description = "No claim exists for the provided identifier"
    )
    public Response delete(
            @Parameter(
                    description = "Technical identifier of the claim to delete.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id
    ) {
        Claim removedClaim = claimService.remove(id);

        if (removedClaim == null) {
            throw new NotFoundException("Claim not found with id: " + id);
        }

        return Response.noContent().build();
    }
}
