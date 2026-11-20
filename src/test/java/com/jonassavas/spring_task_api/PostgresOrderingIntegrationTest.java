package com.jonassavas.spring_task_api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonassavas.spring_task_api.domain.entities.UserEntity;
import com.jonassavas.spring_task_api.repositories.UserRepository;
import com.jonassavas.spring_task_api.security.JwtService;
import com.jonassavas.util.TestUserData;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
class PostgresOrderingIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    private String token;

    @BeforeEach
    void setUp() {
        UserEntity user =
                userRepository.saveAndFlush(
                        TestUserData.createBaseTestUserEntity(
                                UUID.randomUUID().toString().replace("-", "").substring(0, 20)));
        token = jwtService.generateToken(user.getUsername());
    }

    @Test
    void persistsPresetAndCollisionFreeReorder() throws Exception {
        JsonNode board =
                request(
                        MockMvcRequestBuilders.post("/taskboards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "taskBoardName",
                                                        "Postgres board",
                                                        "useDefaultConfiguration",
                                                        true))),
                        201);
        long boardId = board.get("id").asLong();
        JsonNode groups =
                request(MockMvcRequestBuilders.get("/taskboards/" + boardId), 200)
                        .get("taskGroups");
        assertThat(groups.size()).isEqualTo(5);
        assertThat(groups.get(0).get("taskGroupName").asText()).isEqualTo("Done");
        long today = groups.get(1).get("id").asLong();
        long tomorrow = groups.get(2).get("id").asLong();
        long first = createTask(today, "First");
        long second = createTask(today, "Second");
        long third = createTask(tomorrow, "Third");

        request(
                MockMvcRequestBuilders.patch("/tasks/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        Map.of(
                                                "sourceGroupId",
                                                today,
                                                "destinationGroupId",
                                                tomorrow,
                                                "sourceTaskIds",
                                                List.of(second),
                                                "destinationTaskIds",
                                                List.of(third, first)))),
                204);
        JsonNode source = request(MockMvcRequestBuilders.get("/groups/" + today + "/tasks"), 200);
        JsonNode destination =
                request(MockMvcRequestBuilders.get("/groups/" + tomorrow + "/tasks"), 200);
        assertThat(source.size()).isEqualTo(1);
        assertThat(source.get(0).get("id").asLong()).isEqualTo(second);
        assertThat(destination.get(0).get("id").asLong()).isEqualTo(third);
        assertThat(destination.get(1).get("id").asLong()).isEqualTo(first);
    }

    private long createTask(long groupId, String name) throws Exception {
        return request(
                        MockMvcRequestBuilders.post("/groups/" + groupId + "/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("taskName", name))),
                        201)
                .get("id")
                .asLong();
    }

    private JsonNode request(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            int status)
            throws Exception {
        return objectMapper.readTree(
                mockMvc.perform(request.header("Authorization", "Bearer " + token))
                        .andExpect(MockMvcResultMatchers.status().is(status))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }
}
