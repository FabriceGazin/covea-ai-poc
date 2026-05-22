package fr.covea.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.covea.poc.model.Claim;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ClaimService {

    private static final String SELECT_ALL_CLAIMS = """
            SELECT id, "claimantId", "policyId", payload
            FROM claim
            ORDER BY id
            """;

    private static final String SELECT_CLAIM_BY_ID = """
            SELECT id, "claimantId", "policyId", payload
            FROM claim
            WHERE id = ?
            """;

    private static final String INSERT_CLAIM = """
            INSERT INTO claim ("claimantId", "policyId", payload)
            VALUES (?, ?, ?::jsonb)
            RETURNING id, "claimantId", "policyId", payload
            """;

    private static final String UPDATE_CLAIM = """
            UPDATE claim
            SET "claimantId" = ?, "policyId" = ?, payload = ?::jsonb
            WHERE id = ?
            RETURNING id, "claimantId", "policyId", payload
            """;

    private static final String DELETE_CLAIM = """
            DELETE FROM claim
            WHERE id = ?
            RETURNING id, "claimantId", "policyId", payload
            """;

    @Inject
    DataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    public Collection<Claim> all() {
        List<Claim> claims = new ArrayList<>();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_CLAIMS);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                claims.add(toClaim(resultSet));
            }

            return claims;
        } catch (SQLException exception) {
            log.error("Unable to retrieve claims from database", exception);
            throw new IllegalStateException("Unable to retrieve claims from database", exception);
        }
    }

    public Claim get(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_CLAIM_BY_ID)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toClaim(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to retrieve claim with id {}", id, exception);
            throw new IllegalStateException("Unable to retrieve claim with id " + id, exception);
        }
    }

    public Claim create(Claim claim) {
        claim.setId(0);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_CLAIM)
        ) {
            statement.setInt(1, claim.getClaimantId());
            statement.setObject(2, parsePolicyId(claim.getPolicyId()));
            statement.setString(3, toJson(claim));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Claim creation did not return a database row");
                }

                return toClaim(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to create claim in database", exception);
            throw new IllegalStateException("Unable to create claim in database", exception);
        }
    }

    public Claim update(int id, Claim claim) {
        claim.setId(id);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_CLAIM)
        ) {
            statement.setInt(1, claim.getClaimantId());
            statement.setObject(2, parsePolicyId(claim.getPolicyId()));
            statement.setString(3, toJson(claim));
            statement.setInt(4, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toClaim(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to update claim with id {}", id, exception);
            throw new IllegalStateException("Unable to update claim with id " + id, exception);
        }
    }

    public Claim remove(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_CLAIM)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toClaim(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to delete claim with id {}", id, exception);
            throw new IllegalStateException("Unable to delete claim with id " + id, exception);
        }
    }

    private Claim toClaim(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int claimantId = resultSet.getInt("claimantId");
        int policyId = resultSet.getInt("policyId");
        String payload = resultSet.getString("payload");

        try {
            Claim claim = objectMapper.readValue(payload, Claim.class);
            claim.setId(id);
            claim.setClaimantId(claimantId);
            claim.setPolicyId(String.valueOf(policyId));
            return claim;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize claim payload with id " + id, exception);
        }
    }

    private String toJson(Claim claim) {
        try {
            return objectMapper.writeValueAsString(claim);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize claim payload", exception);
        }
    }

    private Integer parsePolicyId(String policyId) {
        if (policyId == null || policyId.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(policyId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
