package com.jonassavas.spring_task_api.domain.dto.task_group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReorderTaskGroupsRequestDto {

    @Schema(description = "Every group on the board, in final order", example = "[12,10,11]")
    @NotNull
    private List<@NotNull Long> groupIds;
}
