package com.jonassavas.spring_task_api.domain.dto.task_board;

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
public class ReorderTaskBoardsRequestDto {
    @Schema(description = "Every board owned by the caller, in final order", example = "[3,1,2]")
    @NotNull
    private List<@NotNull Long> boardIds;
}
