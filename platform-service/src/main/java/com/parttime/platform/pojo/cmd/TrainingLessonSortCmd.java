package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TrainingLessonSortCmd {

    @Schema(description = "所属课程ID")
    private Long courseId;

    @Schema(description = "排序列表，按展示顺序传入")
    private List<SortItem> items;

    @Data
    public static class SortItem {
        @Schema(description = "课时ID")
        private Long id;
        @Schema(description = "排序号")
        private Integer sortOrder;
    }
}
