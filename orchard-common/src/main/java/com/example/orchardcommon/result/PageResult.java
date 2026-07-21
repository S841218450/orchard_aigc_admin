package com.example.orchardcommon.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "每页大小")
    private long size;

    @Schema(description = "当前页码")
    private long current;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setList(page.getRecords());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return result;
    }

    public static <T, R> PageResult<R> of(Page<T> page, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        result.setList(page.getRecords().stream().map(converter).collect(Collectors.toList()));
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return result;
    }
}
