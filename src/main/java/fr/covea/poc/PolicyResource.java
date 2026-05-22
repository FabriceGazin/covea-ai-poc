package fr.covea.poc;

import fr.covea.poc.model.Policy;
import fr.covea.poc.service.PolicyService;
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

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Policies",
        description = "Operations for managing insurance policies, their lifecycle, coverage details and financial limits."
)
public class PolicyResource {

    @Inject
    PolicyService policyService;

    @GET
    @Operation(
            summary = "List all policies",
            description = "Returns all insurance policies currently available in the in-memory repository."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of policies successfully returned",
            content = @Content(schema = @Schema(implementation = Policy.class))
    )
    public Collection<Policy> getAll() {
        return policyService.all();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get a policy by identifier",
            description = "Returns the insurance policy matching the provided technical identifier."
    )
    @APIResponse(
            responseCode = "200",
            description = "Policy successfully found",
            content = @Content(schema = @Schema(implementation = Policy.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "No policy exists for the provided identifier"
    )
    public Policy getById(
            @Parameter(
                    description = "Technical identifier of the policy to retrieve.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id
    ) {
        Policy policy = policyService.get(id);

        if (policy == null) {
            throw new NotFoundException("Policy not found with id: " + id);
        }

        return policy;
    }

    @POST
    @Operation(
            summary = "Create a policy",
            description = "Creates a new insurance policy. The id field from the request body is ignored and replaced by a generated identifier."
    )
    @APIResponse(
            responseCode = "201",
            description = "Policy successfully created",
            content = @Content(schema = @Schema(implementation = Policy.class))
    )
    public Response create(
            @Parameter(
                    description = "Insurance policy information to create.",
                    required = true
            )
            Policy policy
    ) {
        Policy createdPolicy = policyService.create(policy);

        return Response
                .created(URI.create("/policies/" + createdPolicy.getId()))
                .entity(policy)
                .build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update a policy",
            description = "Replaces an existing insurance policy with new information. The identifier from the path overrides the id from the request body."
    )
    @APIResponse(
            responseCode = "200",
            description = "Policy successfully updated",
            content = @Content(schema = @Schema(implementation = Policy.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "No policy exists for the provided identifier"
    )
    public Policy update(
            @Parameter(
                    description = "Technical identifier of the policy to update.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id,
            @Parameter(
                    description = "Updated insurance policy information.",
                    required = true
            )
            Policy policy
    ) {
        Policy updatedPolicy = policyService.update(id, policy);
        if (updatedPolicy == null) {
            throw new NotFoundException("Policy not found with id: " + id);
        }
        return updatedPolicy;
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a policy",
            description = "Deletes the insurance policy matching the provided identifier."
    )
    @APIResponse(
            responseCode = "204",
            description = "Policy successfully deleted"
    )
    @APIResponse(
            responseCode = "404",
            description = "No policy exists for the provided identifier"
    )
    public Response delete(
            @Parameter(
                    description = "Technical identifier of the policy to delete.",
                    required = true,
                    example = "1"
            )
            @PathParam("id") int id
    ) {
        Policy removedPolicy = policyService.remove(id);

        if (removedPolicy == null) {
            throw new NotFoundException("Policy not found with id: " + id);
        }

        return Response.noContent().build();
    }
}
