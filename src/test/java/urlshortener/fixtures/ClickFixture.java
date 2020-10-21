package urlshortener.fixtures;

import urlshortener.model.Click;
import urlshortener.model.ShortURL;

public class ClickFixture {

  public static Click click(ShortURL su) {
    return new Click(null, su.getHash(), null, null, null, null, null, null);
  }
}
