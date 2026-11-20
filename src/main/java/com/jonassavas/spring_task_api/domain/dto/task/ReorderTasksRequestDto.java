package com.jonassavas.spring_task_api.domain.dto.task;

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
public class ReorderTasksRequestDto {

    @NotNull private Long sourceGroupId;

    @NotNull private Long destinationGroupId;

    @Schema(
            description = "Complete final order of tasks remaining in the source group",
            example = "[102]")
    @NotNull
    private List<@NotNull Long> sourceTaskIds;

    @Schema(description = "Complete final order in the destination group", example = "[201,101]")
    @NotNull
    private List<@NotNull Long> destinationTaskIds;
}
