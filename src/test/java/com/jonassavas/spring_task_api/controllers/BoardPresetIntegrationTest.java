package com.jonassavas.spring_task_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class BoardPresetIntegrationTest extends BaseAuthenticatedIntegrationTest {

    @Test
    void createsFiveOrderedGroupsOnlyWhenRequested() throws Exception {
        JsonNode empty = createBoard("Empty", false);
        JsonNode preset = createBoard("Preset", true);
        assertThat(empty.has("taskGroups")).isFalse();
        assertThat(boardDetails(empty.get("id").asLong()).get("taskGroups").size()).isZero();

        JsonNode groups = boardDetails(preset.get("id").asLong()).get("taskGroups");
        assertThat(groups).hasSize(5);
        assertThat(
                        List.of(
                                groups.get(0).get("taskGroupName").asText(),
                                groups.get(1).get("taskGroupName").asText(),
                                groups.get(2).get("taskGroupName").asText(),
                                groups.get(3).get("taskGroupName").asText(),
                                groups.get(4).get("taskGroupName").asText()))
                .containsExactly("Done", "Today", "Tomorrow", "This Week", "Deadlines");
        for (int i = 0; i < groups.size(); i++) {
            assertThat(groups.get(i).get("position").asInt()).isEqualTo(i);
            assertThat(groups.get(i).get("tasks").size()).isZero();
            assertThat(groups.get(i).get("strikeThroughTasks").asBoolean()).isEqualTo(i == 0);
            assertThat(groups.get(i).get("sortMode").asText())
                    .isEqualTo(i == 4 ? "DUE_DATE_ASC" : "MANUAL");
        }
    }

    @Test
    void deadlinesSortByDateAndAllowDateClearing() throws Exception {
        long boardId = createBoard("Dates", true).get("id").asLong();
        long deadlinesId = boardDetails(boardId).get("taskGroups").get(4).get("id").asLong();
        JsonNode late = createTask(deadlinesId, "Late", "2026-12-31");
        JsonNode undated = createTask(deadlinesId, "Undated", null);
        JsonNode early = createTask(deadlinesId, "Early", "2026-12-01");
        createTask(deadlinesId, "Also early", "2026-12-01");

        JsonNode tasks = tasks(deadlinesId);
        assertThat(taskNames(tasks)).containsExactly("Early", "Also early", "Late", "Undated");
        assertThat(tasks.get(0).get("dueDate").asText()).isEqualTo("2026-12-01");
        assertThat(
                        boardDetails(boardId)
                                .get("taskGroups")
                                .get(4)
                                .get("tasks")
                                .get(0)
                                .get("taskName")
                                .asText())
                .isEqualTo("Early");

        mockMvc.perform(
                        authenticated(
                                        MockMvcRequestBuilders.patch(
                                                "/tasks/" + early.get("id").asLong()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"dueDate\":null}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertThat(taskNames(tasks(deadlinesId)))
                .containsExactly("Also early", "Late", "Undated", "Early");
        assertThat(late.get("dueDate").asText()).isEqualTo("2026-12-31");
        assertThat(undated.get("dueDate").isNull()).isTrue();
    }

    @Test
    void updatesGroupStylingAndSortMode() throws Exception {
        long boardId = createBoard("Group settings", true).get("id").asLong();
        long doneId = boardDetails(boardId).get("taskGroups").get(0).get("id").asLong();
        mockMvc.perform(
                        authenticated(
                                        MockMvcRequestBuilders.patch(
                                                "/taskboards/" + boardId + "/groups/" + doneId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"strikeThroughTasks\":false,\"sortMode\":\"DUE_DATE_ASC\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.strikeThroughTasks").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sortMode").value("DUE_DATE_ASC"));
        JsonNode done = boardDetails(boardId).get("taskGroups").get(0);
        assertThat(done.get("strikeThroughTasks").asBoolean()).isFalse();
        assertThat(done.get("sortMode").asText()).isEqualTo("DUE_DATE_ASC");
    }

    @Test
    void reordersBoardsAndRejectsIncompleteLists() throws Exception {
        long first = createBoard("First", false).get("id").asLong();
        long second = createBoard("Second", false).get("id").asLong();
        String endpoint = "/taskboards/reorder";
        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch(endpoint))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("boardIds", List.of(second, first)))))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        assertThat(boardNames()).containsExactly("Second", "First");

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch(endpoint))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("boardIds", List.of(second, second)))))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertThat(boardNames()).containsExactly("Second", "First");
    }

    @Test
    void movesAndReordersTasksWithoutLosingAny() throws Exception {
        long boardId = createBoard("Moves", false).get("id").asLong();
        long source = createGroup(boardId, "Source", "MANUAL");
        long destination = createGroup(boardId, "Destination", "MANUAL");
        long first = createTask(source, "First", null).get("id").asLong();
        long second = createTask(source, "Second", null).get("id").asLong();
        long third = createTask(destination, "Third", null).get("id").asLong();

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "sourceGroupId",
                                                        source,
                                                        "destinationGroupId",
                                                        destination,
                                                        "sourceTaskIds",
                                                        List.of(second),
                                                        "destinationTaskIds",
                                                        List.of(third, first)))))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        assertThat(taskNames(tasks(source))).containsExactly("Second");
        assertThat(taskNames(tasks(destination))).containsExactly("Third", "First");

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "sourceGroupId",
                                                        destination,
                                                        "destinationGroupId",
                                                        destination,
                                                        "sourceTaskIds",
                                                        List.of(first, third),
                                                        "destinationTaskIds",
                                                        List.of()))))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        assertThat(taskNames(tasks(destination))).containsExactly("First", "Third");

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "sourceGroupId",
                                                        source,
                                                        "destinationGroupId",
                                                        destination,
                                                        "sourceTaskIds",
                                                        List.of(),
                                                        "destinationTaskIds",
                                                        List.of(first, third)))))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertThat(taskNames(tasks(source))).containsExactly("Second");

        long unrelated = createGroup(boardId, "Unrelated", "MANUAL");
        long unrelatedTask = createTask(unrelated, "Unrelated task", null).get("id").asLong();
        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "sourceGroupId",
                                                        source,
                                                        "destinationGroupId",
                                                        destination,
                                                        "sourceTaskIds",
                                                        List.of(second, unrelatedTask),
                                                        "destinationTaskIds",
                                                        List.of(first, third)))))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertThat(taskNames(tasks(unrelated))).containsExactly("Unrelated task");
    }

    @Test
    void rejectsManualReorderInDateSortedGroupButAllowsTaskMove() throws Exception {
        long boardId = createBoard("Sorted", true).get("id").asLong();
        JsonNode groups = boardDetails(boardId).get("taskGroups");
        long today = groups.get(1).get("id").asLong();
        long deadlines = groups.get(4).get("id").asLong();
        long taskId = createTask(today, "Move me", "2026-12-02").get("id").asLong();

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/" + taskId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("taskGroupId", deadlines))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskGroupId").value(deadlines));
        assertThat(taskNames(tasks(deadlines))).containsExactly("Move me");
        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.patch("/tasks/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "sourceGroupId",
                                                        deadlines,
                                                        "destinationGroupId",
                                                        deadlines,
                                                        "sourceTaskIds",
                                                        List.of(taskId),
                                                        "destinationTaskIds",
                                                        List.of()))))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void rejectsGroupIdsFromAnotherBoardWithoutChangingOrder() throws Exception {
        long boardId = createBoard("Own groups", true).get("id").asLong();
        long otherBoardId = createBoard("Other groups", true).get("id").asLong();
        JsonNode ownGroups = boardDetails(boardId).get("taskGroups");
        long foreignGroupId =
                boardDetails(otherBoardId).get("taskGroups").get(0).get("id").asLong();
        List<Long> invalidIds =
                List.of(
                        foreignGroupId,
                        ownGroups.get(1).get("id").asLong(),
                        ownGroups.get(2).get("id").asLong(),
                        ownGroups.get(3).get("id").asLong(),
                        ownGroups.get(4).get("id").asLong());
        mockMvc.perform(
                        authenticated(
                                        MockMvcRequestBuilders.patch(
                                                "/taskboards/" + boardId + "/groups/reorder"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("groupIds", invalidIds))))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertThat(boardDetails(boardId).get("taskGroups").get(0).get("taskGroupName").asText())
                .isEqualTo("Done");
    }

    @Test
    void rejectsInvalidDueDateAndSortMode() throws Exception {
        long boardId = createBoard("Invalid values", false).get("id").asLong();
        long groupId = createGroup(boardId, "Tasks", "MANUAL");
        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.post("/groups/" + groupId + "/tasks"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"taskName\":\"Bad date\",\"dueDate\":\"tomorrow\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(
                        authenticated(
                                        MockMvcRequestBuilders.post(
                                                "/taskboards/" + boardId + "/groups"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"taskGroupName\":\"Bad mode\",\"sortMode\":\"UNKNOWN\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private JsonNode createBoard(String name, boolean preset) throws Exception {
        String body =
                objectMapper.writeValueAsString(
                        Map.of("taskBoardName", name, "useDefaultConfiguration", preset));
        String content =
                mockMvc.perform(
                                authenticated(MockMvcRequestBuilders.post("/taskboards"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode boardDetails(long id) throws Exception {
        return objectMapper.readTree(
                mockMvc.perform(authenticated(MockMvcRequestBuilders.get("/taskboards/" + id)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }

    private long createGroup(long boardId, String name, String sortMode) throws Exception {
        return objectMapper
                .readTree(
                        mockMvc.perform(
                                        authenticated(
                                                        MockMvcRequestBuilders.post(
                                                                "/taskboards/"
                                                                        + boardId
                                                                        + "/groups"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                Map.of(
                                                                        "taskGroupName", name,
                                                                        "sortMode", sortMode))))
                                .andExpect(MockMvcResultMatchers.status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .get("id")
                .asLong();
    }

    private JsonNode createTask(long groupId, String name, String dueDate) throws Exception {
        ObjectNode body = objectMapper.createObjectNode().put("taskName", name);
        if (dueDate != null) {
            body.put("dueDate", dueDate);
        }
        return objectMapper.readTree(
                mockMvc.perform(
                                authenticated(
                                                MockMvcRequestBuilders.post(
                                                        "/groups/" + groupId + "/tasks"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body.toString()))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }

    private JsonNode tasks(long groupId) throws Exception {
        return objectMapper.readTree(
                mockMvc.perform(
                                authenticated(
                                        MockMvcRequestBuilders.get(
                                                "/groups/" + groupId + "/tasks")))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }

    private List<String> taskNames(JsonNode tasks) {
        return java.util.stream.StreamSupport.stream(tasks.spliterator(), false)
                .map(task -> task.get("taskName").asText())
                .toList();
    }

    private List<String> boardNames() throws Exception {
        JsonNode boards =
                objectMapper.readTree(
                        mockMvc.perform(authenticated(MockMvcRequestBuilders.get("/taskboards")))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        return java.util.stream.StreamSupport.stream(boards.spliterator(), false)
                .map(board -> board.get("taskBoardName").asText())
                .toList();
    }
}
