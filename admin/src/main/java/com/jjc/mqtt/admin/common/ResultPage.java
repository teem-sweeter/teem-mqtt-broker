package com.jjc.mqtt.admin.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
/**
 * @author sweeter
 * @description
 * @date 2025/5/26
 */
@NoArgsConstructor
@Data
public class ResultPage<T> implements Serializable {

    @Schema(title = "当前页,初始值1")
    private Integer page = 0;

    @Schema(title = "当前页数据行数")
    private Integer size = 0;

    @Schema(title = "数据总行数")
    private Long total = 0L;

    private List<T> content = Collections.emptyList();


    /**
     * 将spring springframework.data的分页格式化
     * @param page @ org.springframework.data.domain.Page
     * @param content
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> ResultPage<T> of(Page<S> page, List<T> content) {
        Objects.requireNonNull(page);
        ResultPage<T> result = new ResultPage<>();
        if (Objects.nonNull(content)) {
            result.setContent(content);
        }
        result.setPage(page.getPageable().getPageNumber()+1);
        result.setSize(content.size());
        result.setTotal(page.getTotalElements());
        return result;
    }

    public static <T> ResultPage<T> of(Page<T> page) {
        return ResultPage.of(page,page.getContent());
    }

    public ResultPage<T> peek(Consumer<T> action) {
        this.content = this.content.stream().peek(action).collect(Collectors.toList());
        return this;
    }

}
