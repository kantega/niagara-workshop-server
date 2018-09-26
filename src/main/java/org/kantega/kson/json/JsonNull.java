package org.kantega.kson.json;

import fj.Equal;
import fj.F0;
import fj.data.Option;
import org.kantega.kson.json.JsonValue;

public class JsonNull extends JsonValue {

  public final static Equal<org.kantega.kson.json.JsonNull> eq =
      Equal.equal(one -> other -> true);

  public <T> Option<T> onNull(F0<T> f) {
    return Option.some(f.f());
  }
  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonNull");
    return sb.toString();
  }
}
