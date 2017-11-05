module Codec exposing (..)

import Json.Decode exposing (..)
import Debug exposing (log)

msgDecoder : String -> String
msgDecoder msg =
    let

        res = log "Msg" (decodeString (field "msg" string) msg)
    in
        case res of
            Err m -> m
            Ok m -> m