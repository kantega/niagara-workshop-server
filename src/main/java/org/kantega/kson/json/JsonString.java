package org.kantega.kson.json;

import fj.F;
import fj.data.Option;
import org.kantega.kson.json.JsonValue;

public class JsonString extends JsonValue {

  public final String value;

  public JsonString(String value) {
    this.value = value;
  }

  public <T> Option<T> onString(F<String, T> f) {
    return Option.some(f.f(value));
  }

  public org.kantega.kson.json.JsonString update(F<String, String> f){
    return new org.kantega.kson.json.JsonString(f.f(value));
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonString{");
    sb.append("'").append(value).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
