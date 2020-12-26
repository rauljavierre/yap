package urlshortener.domain;

public class LinkBody {
    private String url;
    private boolean generateQR;

    public String getUrl() {
        return this.url;
    }

    public boolean getGenerateQR() {
        return this.generateQR;
    }
}
