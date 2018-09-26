package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.Ord;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.json.JsonValue;

public class JsonObject extends JsonValue {

    public static final org.kantega.kson.json.JsonObject empty =
      JsonObject(List.nil());

    public static final Equal<org.kantega.kson.json.JsonObject> eq =
      Equal.treeMapEqual(Equal.stringEqual, JsonValue.eq()).contramap(obj -> obj.pairs);

    public final TreeMap<String, JsonValue> pairs;

    public JsonObject(TreeMap<String, JsonValue> pairs) {
        this.pairs = pairs;
    }

    public static org.kantega.kson.json.JsonObject JsonObject(List<P2<String, JsonValue>> vals) {
        return new org.kantega.kson.json.JsonObject(TreeMap.iterableTreeMap(Ord.stringOrd, vals));
    }

    public org.kantega.kson.json.JsonObject empty() {
        return empty;
    }

    public <T> Option<T> onObject(F<TreeMap<String, JsonValue>, T> f) {
        return Option.some(f.f(pairs));
    }

    public org.kantega.kson.json.JsonObject update(F<TreeMap<String, JsonValue>, TreeMap<String, JsonValue>> f) {
        return new org.kantega.kson.json.JsonObject(f.f(pairs));
    }

    public org.kantega.kson.json.JsonObject withField(String name, JsonValue value) {
        return new org.kantega.kson.json.JsonObject(pairs.set(name, value));
    }

    public Option<JsonValue> get(String fieldName) {
        return pairs.get(fieldName);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JsonObject{");
        sb.append(pairs);
        sb.append('}');
        return sb.toString();
    }
}
