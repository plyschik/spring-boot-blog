package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearArchiveEntry {
    private Integer year;
    private Integer count;
    private List<Month> months;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Month {
        private Integer month;
        private Integer count;
    }
}
