package com.jjc.mqtt.admin.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
/**
 * @author sweeter
 * @description
 * @date 2025/5/26
 */
@Data
public class ReqPage implements Serializable {

    @Schema(title = "页码")
    @Min(value = 0, message = "表示第一页")
    private int page = 0;

    @Schema(title = "每页大小")
    @Min(value = 5,message = "最小每页行数为5")
    private int size = 20;

    private String sort;

    private String order;

    public PageRequest createPageRequest() {
        int page = Math.max(this.page - 1, 0);
        if (StringUtils.isNotBlank(this.sort)) {
            if (StringUtils.isNotBlank(this.order)) {
                return PageRequest.of(page, this.size, Sort.Direction.fromString(this.order),this.sort);
            }else {
                return PageRequest.of(page, this.size, Sort.Direction.ASC,this.sort);
            }
        }
        return PageRequest.of(page, this.size);
    }

    public PageRequest createPageRequest(Sort sort) {
        int page = Math.max(this.page - 1, 0);
        return PageRequest.of(page, this.size, sort);
    }

}
