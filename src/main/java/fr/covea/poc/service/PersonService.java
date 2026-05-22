package fr.covea.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.covea.poc.model.Person;
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
public class PersonService {

    private static final String SELECT_ALL_PERSONS = """
            SELECT id, payload
            FROM person
            ORDER BY id
            """;

    private static final String SELECT_PERSON_BY_ID = """
            SELECT id, payload
            FROM person
            WHERE id = ?
            """;

    private static final String INSERT_PERSON = """
            INSERT INTO person (payload)
            VALUES (?::jsonb)
            RETURNING id, payload
            """;

    private static final String UPDATE_PERSON = """
            UPDATE person
            SET payload = ?::jsonb
            WHERE id = ?
            RETURNING id, payload
            """;

    private static final String DELETE_PERSON = """
            DELETE FROM person
            WHERE id = ?
            RETURNING id, payload
            """;

    @Inject
    DataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    public Collection<Person> all() {
        List<Person> persons = new ArrayList<>();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_PERSONS);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                persons.add(toPerson(resultSet));
            }

            return persons;
        } catch (SQLException exception) {
            log.error("Unable to retrieve persons from database", exception);
            throw new IllegalStateException("Unable to retrieve persons from database", exception);
        }
    }

    public Person get(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_PERSON_BY_ID)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPerson(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to retrieve person with id {}", id, exception);
            throw new IllegalStateException("Unable to retrieve person with id " + id, exception);
        }
    }

    public Person create(Person person) {
        person.setId(0);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_PERSON)
        ) {
            statement.setString(1, toJson(person));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Person creation did not return a database row");
                }

                return toPerson(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to create person in database", exception);
            throw new IllegalStateException("Unable to create person in database", exception);
        }
    }

    public Person update(int id, Person person) {
        person.setId(id);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_PERSON)
        ) {
            statement.setString(1, toJson(person));
            statement.setInt(2, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPerson(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to update person with id {}", id, exception);
            throw new IllegalStateException("Unable to update person with id " + id, exception);
        }
    }

    public Person remove(int id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_PERSON)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return toPerson(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Unable to delete person with id {}", id, exception);
            throw new IllegalStateException("Unable to delete person with id " + id, exception);
        }
    }

    private Person toPerson(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String payload = resultSet.getString("payload");

        try {
            Person person = objectMapper.readValue(payload, Person.class);
            person.setId(id);
            return person;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize person payload with id " + id, exception);
        }
    }

    private String toJson(Person person) {
        try {
            return objectMapper.writeValueAsString(person);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize person payload", exception);
        }
    }
}
