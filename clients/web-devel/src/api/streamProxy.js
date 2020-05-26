import ReconnectingWebSocket from 'reconnecting-websocket';
import backendDomain from "./env";

const webSocketRoute = 'ws://' + backendDomain + '/connect';
const createWebSocket = () => (
  new ReconnectingWebSocket(webSocketRoute, null, {
    automaticOpen: false,
    debug: true
  })
);

let webSocket = null;

const send = (name, body) => {
};

const connect = () => {
};

const listenerGen = (handler) => (raw) => {
};

const listen = (handler) => {
};

export default {
  connect,
  send,
  listen
};
