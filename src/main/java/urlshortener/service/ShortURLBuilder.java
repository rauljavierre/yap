package urlshortener.service;

import static com.google.common.hash.Hashing.murmur3_32;


import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import urlshortener.model.ShortURL;

public class ShortURLBuilder {

  private String hash;
  private String target;
  private URI uri;
  private String sponsor;
  private Date created;
  private String owner;
  private Integer mode;
  private Boolean safe;
  private String ip;
  private String country;

  static ShortURLBuilder newInstance() {
    return new ShortURLBuilder();
  }

  ShortURL build() {
    return new ShortURL(
        hash,
        target,
        uri,
        sponsor,
        created,
        owner,
        mode,
        safe,
        ip,
        country
    );
  }

  ShortURLBuilder target(String url) {
    target = url;
    //noinspection UnstableApiUsage
    hash = murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
    return this;
  }

  ShortURLBuilder sponsor(String sponsor) {
    this.sponsor = sponsor;
    return this;
  }

  ShortURLBuilder createdNow() {
    this.created = new Date(System.currentTimeMillis());
    return this;
  }

  ShortURLBuilder randomOwner() {
    this.owner = UUID.randomUUID().toString();
    return this;
  }

  ShortURLBuilder temporaryRedirect() {
    this.mode = HttpStatus.TEMPORARY_REDIRECT.value();
    return this;
  }

  ShortURLBuilder treatAsSafe() {
    this.safe = true;
    return this;
  }

  ShortURLBuilder ip(String ip) {
    this.ip = ip;
    return this;
  }

  ShortURLBuilder unknownCountry() {
    this.country = null;
    return this;
  }

  ShortURLBuilder uri(Function<String, URI> extractor) {
    this.uri = extractor.apply(hash);
    return this;
  }
}
