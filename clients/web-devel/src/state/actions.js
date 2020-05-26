import * as Types from './types'
import Auth from '~/api/auth';
import streamProxy from '~/api/streamProxy';
import fetchGame from '~/api/fetchGame';
import fetchEvents from '~/api/fetchEvents';

// Utilities
const createAction = (type, payload) => ({ type, payload });
const clockAt = () => (new Date).getTime();

// Actions below
export const authLogout = () => (dispatch) => {
  Auth.logout();
  return dispatch(createAction(Types.AUTH_LOGOUT, {}));
};

export const authLoginSuccess = (auth) => (dispatch) => {
  Auth.apply(auth);
  auth.loggedIn = true;
  return dispatch(createAction(Types.AUTH_LOGIN, auth));
};

export const matchmakingSet = (challenges) =>
  createAction(Types.MATCHMAKING_SET, challenges);

export const ping = () => {
  streamProxy.send('Ping');
  return createAction(Types.PING, { at: clockAt() });
};

export const pong = (serverAt) =>
  createAction(Types.PONG, { at: clockAt(), serverAt });

export const gamesSetRecent = (games) =>
  createAction(Types.GAMES_SET_RECENT, games);

export const gameFetch = (id) => (dispatch) => {
  dispatch(createAction(Types.GAME_FETCH));
  return fetchGame(id).then(r => {
    dispatch(createAction(Types.GAME_FETCHED, r.data.game));
    return r.data.game;
  });
};

export const eventFetch = ({userId, bearerToken}) => (dispatch) => {
  dispatch(createAction((Types.EVENT_FETCHING)));
  return fetchEvents({userId: userId, bearerToken: bearerToken}).then(response => {
    if (response.data && response.data.fetchTopDomains) {
      if(response.data.fetchTopDomains.success === true) {
        dispatch(createAction(Types.EVENT_FETCHED, response.data.fetchTopDomains.events));
      }
    }
    // dispatch(updateEventsDisplay(_.data))
  })
};

export const gameSetSelections = (selections) =>
  createAction(Types.GAME_SET_SELECTIONS, selections);

export const gameSetPhase = (phase) =>
  createAction(Types.GAME_SET_PHASE, phase);

export const updateEventsDisplay = (events) =>
  console.log(events);
