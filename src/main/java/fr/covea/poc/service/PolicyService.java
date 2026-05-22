package fr.covea.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.covea.poc.model.Policy;
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
public class PolicyService {

    private static final String SELECT_ALL_POLICIES = """
            SELECT id, "holderId", payload
            FROM policy
            ORDER BY id
            """;

    private static final String SELECT_POLICY_BY_ID = """
            SELECT id, "holderId", payload
            FROM policy
            WHERE id = ?
            """;

    private static final String INSERT_POLICY = """
            INSERT INTO policy ("holderId", payload)
            VALUES (?, ?::jsonb)
            RETURNING id, "holderId", payload
            """;

    private static final String UPDATE_POLICY = """
            UPDATE policy
            SET "holderId" = ?, payload = ?::jsonb
            WHERE id = ?
            RETURNING id, "holderId", payload
            """;

    private static final String DELETE_POLICY = """
            DELETE FROM policy
            WHERE id = ?
            RETURNING id, "holderId", payload
            """;

    @Inject
    DataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    public Collection<Policy> all() {
        List<Policy> policies = new ArrayList<>();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_POLICIES);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                policies.add(toPolicy(resultSet));
            }

            return policies;
        } catch (SQLException exception) {
            log.error("Unable to retrieve policies from database", exception);
            throw new IllegalStateException("Unable to retrieve policies from database", exception);
        }
    }

    public Policy get(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_POLICY_BY_ID)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPolicy(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to retrieve policy with id {}", id, exception);
            throw new IllegalStateException("Unable to retrieve policy with id " + id, exception);
        }
    }

    public Policy create(Policy policy) {
        policy.setId(0);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_POLICY)
        ) {
            statement.setObject(1, parseHolderId(policy.getHolderId()));
            statement.setString(2, toJson(policy));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Policy creation did not return a database row");
                }

                return toPolicy(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to create policy in database", exception);
            throw new IllegalStateException("Unable to create policy in database", exception);
        }
    }

    public Policy update(int id, Policy policy) {
        policy.setId(id);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_POLICY)
        ) {
            statement.setObject(1, parseHolderId(policy.getHolderId()));
            statement.setString(2, toJson(policy));
            statement.setInt(3, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPolicy(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to update policy with id {}", id, exception);
            throw new IllegalStateException("Unable to update policy with id " + id, exception);
        }
    }

    public Policy remove(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_POLICY)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPolicy(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to delete policy with id {}", id, exception);
            throw new IllegalStateException("Unable to delete policy with id " + id, exception);
        }
    }

    private Policy toPolicy(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int holderId = resultSet.getInt("holderId");
        String payload = resultSet.getString("payload");

        try {
            Policy policy = objectMapper.readValue(payload, Policy.class);
            policy.setId(id);
            policy.setHolderId(String.valueOf(holderId));
            return policy;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize policy payload with id " + id, exception);
        }
    }

    private String toJson(Policy policy) {
        try {
            return objectMapper.writeValueAsString(policy);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize policy payload", exception);
        }
    }

    private Integer parseHolderId(String holderId) {
        if (holderId == null || holderId.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(holderId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
