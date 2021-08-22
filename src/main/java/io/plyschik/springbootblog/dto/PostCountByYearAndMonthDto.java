package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostCountByYearAndMonthDto {
    private Integer year;
    private Integer month;
    private Integer count;
}
