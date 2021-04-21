package main;

import java.util.List;

public class SearcherProperties {
    private List<String> keywords;
    private Integer searchMaxPageNumber;
    private Integer delayTime;

    public SearcherProperties(List<String> keywords, Integer searchMaxPageNumber, Integer delayTime) {
        this.keywords = keywords;
        this.searchMaxPageNumber = searchMaxPageNumber;
        this.delayTime = delayTime;
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    public Integer getSearchMaxPageNumber() {
        return this.searchMaxPageNumber;
    }

    public Integer getDelayTime() {
        return this.delayTime;
    }
}
