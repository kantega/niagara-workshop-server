import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import WebSocket
import Debug exposing (log)
import Codec exposing (msgDecoder)
import Markdown exposing (..)
import WebSocket.LowLevel exposing (..)
import Task exposing (..)
import Navigation exposing (Location)
import UrlParser exposing (..)
import Random exposing (..)
import Random.String as RS exposing (..)
import Random.Char as RC exposing (..)
import Dom.Scroll as Scroll exposing (toBottom)

main =
  Navigation.program (parseLocation >> LocationChange)
    { init = init
    , view = view
    , update = update
    , subscriptions = subscriptions
    }


-- MODEL


type AppState
    = Intro
    | Started SessionId (List Message)

type SessionId =
    SessionId String

type Message
    = Task String
    | Comment String
    | Feedback String
    | First String
    | Team String


init : Location -> (AppState, Cmd Msg)
init location =
    let
        currentRoute =
            parseLocation location
    in
       update (LocationChange currentRoute) Intro


-- UPDATE
type Msg
    = NoOp
    | NewMessage Message
    | LocationChange Route
    | SetRoute String



update : Msg -> AppState -> (AppState, Cmd Msg)
update msg s =
  case msg of
        NoOp ->
            (s,Cmd.none)

        SetRoute id ->
            (s,Cmd.batch [WebSocket.send "ws://172.16.0.168:8080/ws" ("{\"topic\":\"/start/" ++ id ++ "\",\"msg\":\"start\"}"),  Navigation.modifyUrl ("#ws/" ++ id)])

        NewMessage m ->
            case s of
                Started sessid msgs ->
                    let
                        nextMsgs =
                            case m of
                                First _ -> m :: []
                                _ -> m :: msgs
                    in
                        (Started sessid nextMsgs, Task.attempt (always NoOp) <| Scroll.toBottom "page-body")
                _ -> (s,Cmd.none)

        LocationChange route ->
            case route of
                ListeningRoute id ->
                    (Started (SessionId id) [],Cmd.none)
                StartListeringRoute ->
                    (Intro,Random.generate (\id-> SetRoute (id))  (RS.string 7 RC.lowerCaseLatin))
                _ ->
                    (Intro,Cmd.none)



subscriptions : AppState -> Sub Msg
subscriptions s =
    case s of
        Intro -> Sub.none
        Started (SessionId id) _ ->  WebSocket.listen ("ws://172.16.0.168:8080/ws?/progress/" ++ id ++ "=first") (msgDecoder >> parseMessage >> NewMessage)


parseMessage : String -> Message
parseMessage msg =
    if String.startsWith "fail" msg then Feedback (String.dropLeft 5 msg)
    else if String.startsWith "start" msg then First (String.dropLeft 6 msg)
    else if String.startsWith "task" msg then Task (String.dropLeft 5 msg)
    else if String.startsWith "team" msg then Team (String.dropLeft 5 msg)
    else Comment (msg)

-- VIEW

view : AppState -> Html Msg
view s =
  div [class "container"] [
    div [] [
            viewContent s
        ]
    ]

viewContent:AppState->Html Msg
viewContent s =
    case s of
        Intro -> viewWelcome
        Started _ messages -> div [id "list",class "mt-5"] ((messages |> List.reverse |> List.map viewMessage ) )

viewWelcome: Html Msg
viewWelcome  =
    div[class "jumbotron mt-5"][
        h1 [class "display-3"][text "Welcome"]
        , p[class "lead"] [text "Velkommen til workshop event drevet arkitektur"]
        , p[class "lead"] [
            text "Kodeeksempler og kildekode her:"
            , a [ href "https://github.com/kantega/niagara-workshop-server"] [ text "https://github.com/kantega/niagara-workshop-server"]]

        , p[class "lead"] [a [class "btn btn-primary btn-lg", href (startListenPath)] [ text "start"]]]


viewMessage : Message -> Html msg
viewMessage msg =
    case msg of
        Feedback m ->
            div [] [
                Markdown.toHtml [class "feedback"] m
            ]
        Task m ->
            div [] [
                hr [][]
                , Markdown.toHtml [class "task"] m
            ]
        First m ->
            div [] [
                Markdown.toHtml [class "task"] m
            ]
        Comment m ->
            div [] [
                Markdown.toHtml [class "task"] m
            ]
        Team m ->
            div [] [
                Markdown.toHtml [class "team"] m
            ]

-- ROUTING

type Route =
    DefaultRoute
    | StartListeringRoute
    | ListeningRoute String

matchers : Parser (Route -> a) a
matchers =
    oneOf
        [ UrlParser.map DefaultRoute top
        , UrlParser.map StartListeringRoute (UrlParser.s "ws")
        , UrlParser.map ListeningRoute (UrlParser.s "ws" </> UrlParser.string)
        ]

parseLocation : Location -> Route
parseLocation location =
    case (parseHash matchers location) of
        Just route ->
            route

        Nothing ->
            DefaultRoute

startListenPath =
    "#ws"

listenPath : String -> String
listenPath id =
    "#ws/" ++ id