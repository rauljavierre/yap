package urlshortener.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkBody {

    @JsonProperty(required = true)
    private String url;

    @JsonProperty(required = false)
    private boolean generateQR;

    public String getUrl() {
        return this.url;
    }

    public boolean getGenerateQR() {
        return this.generateQR;
    }
}
