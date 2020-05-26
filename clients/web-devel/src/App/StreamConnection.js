import * as Actions from '~/state/actions';
import * as React from 'react';
import streamProxy from '~/api/streamProxy';
import { useDispatch, useSelector } from 'react-redux'

const PingPeriod = 15000;

const messageConverter = (message) => {
  switch (message.name) {
    case 'Pong':
      return Actions.pong(message.data);
    case 'Matchmaking':
      return Actions.matchmakingSet(message.data);
    case 'GameChangeSelection':
      return Actions.gameSetSelections(message.data);
    default:
      return null;
  }
};

const StreamConnection = () => {
  const dispatch = useDispatch();
  const auth = useSelector(_ => _.auth);

  // Equivalent of componentDidMount
  React.useEffect(() => {
  }, []);

  // Open port when auth changes
  React.useEffect(() => {
  }, [auth]);
  // Nothing to render, just need access to store and dispatch.
  return null;
}

export default StreamConnection;
