package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.F0;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValues;

import java.math.BigDecimal;

import static fj.data.Option.none;
import static org.kantega.kson.JsonResult.fail;

public abstract class JsonValue {

    public static Equal<org.kantega.kson.json.JsonValue> eq() {
        return Equal.equal(one -> other ->
          one.onString(str1 -> other.onString(str1::equals).orSome(false))
            .orElse(one.onBool(bool1 -> other.onBool(bool1::equals).orSome(false)))
            .orElse(one.onNumber(num1 -> other.onNumber(num1::equals).orSome(false)))
            .orElse(one.onNull(() -> other.onNull(() -> true).orSome(false)))
            .orElse(one.onObject(obj1 -> other.onObject(Equal.treeMapEqual(Equal.stringEqual, eq()).eq(obj1)).orSome(false)))
            .orElse(one.onArray(arr -> other.onArray(Equal.listEqual(eq()).eq(arr)).orSome(false)))
            .orSome(false)
        );
    }

    public Option<String> asTextO() {
        return onString(Option::some).orSome(none());
    }

    public JsonResult<String> asText() {
        return onString(JsonResult::success).orSome(fail(toString() + " is not a string"));
    }

    public Option<BigDecimal> asNumberO() {
        return onNumber(Option::some).orSome(none());
    }

    public JsonResult<BigDecimal> asNumber() {
        return onNumber(JsonResult::success).orSome(fail(toString() + "is not a number"));
    }

    public JsonResult<Long> asLong() {
        return onNumber(JsonResult::success).map(n->n.asLong()).orSome(fail(toString() + "is not a long"));
    }

    public Option<Long> asLongO() {
        return asLong().toOption();
    }

    public Option<Boolean> asBoolO() {
        return asBool().toOption();
    }

    public JsonResult<Boolean> asBool() {
        return onBool(JsonResult::success).orSome(fail(toString() + "is not a bool"));
    }

    public JsonResult.ArrayResult<org.kantega.kson.json.JsonValue> asArray() {
        JsonResult<List<org.kantega.kson.json.JsonValue>> v =
          onArray(JsonResult::success).orSome(JsonResult.fail(toString() + "is not an array"));
        return new JsonResult.ArrayResult<>(v.toValidation());
    }

    public JsonResult<JsonObject> asObject(){
        return onObject(map-> JsonResult.success(JsonObject.JsonObject(map.toList()))).orSome(fail(toString() + "is not an object"));
    }

    public JsonResult<org.kantega.kson.json.JsonValue> field(String field) {
        return onObject(
          m -> m.get(field)
            .option(
              JsonResult.<org.kantega.kson.json.JsonValue>fail("Field " + field + " not found"),
              JsonResult::success)
        ).orSome(JsonResult.fail("Trying to read field " + field + ", but this is not abject"));
    }

    public Option<org.kantega.kson.json.JsonValue> setField(String name, org.kantega.kson.json.JsonValue value) {
        return onObject(map -> Option.some(JsonValues.jObj(map.set(name, value)))).orSome(none());
    }

    public JsonResult.ArrayResult<org.kantega.kson.json.JsonValue> fieldAsArray(String field) {
        return field(field).asArray();
    }

    public JsonResult<String> fieldAsText(String field) {
        return field(field).bind(org.kantega.kson.json.JsonValue::asText);
    }

    public JsonResult<BigDecimal> fieldAsNumber(String field) {
        return field(field).bind(org.kantega.kson.json.JsonValue::asNumber);
    }

    public JsonResult<Long> fieldAsLong(String field){
        return fieldAsNumber(field).map(n->n.longValue());
    }

    public JsonResult<Boolean> fieldAsBool(String field) {
        return field(field).bind(org.kantega.kson.json.JsonValue::asBool);
    }

    public <T> Option<T> onString(F<String, T> f) {
        return none();
    }

    public <T> Option<T> onNumber(F<BigDecimal, T> f) {
        return none();
    }

    public <T> Option<T> onArray(F<List<org.kantega.kson.json.JsonValue>, T> f) {
        return none();
    }

    public <T> Option<T> onObject(F<TreeMap<String, org.kantega.kson.json.JsonValue>, T> f) {
        return none();
    }

    public <T> Option<T> onNull(F0<T> f) {
        return none();
    }

    public <T> Option<T> onBool(F<Boolean, T> f) {
        return none();
    }

}
