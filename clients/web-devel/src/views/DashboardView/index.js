import * as React from 'react';
import Alert from 'react-bootstrap/Alert';
import GamesView from '~/views/GamesView';
import EventsView from '~/views/EventsView';
import MatchmakingView from '~/views/MatchmakingView';
import NavigationView from '~/components/layout/NavigationView';
import styles from './index.css';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux'

const DashboardView = (props) => {
  const auth = useSelector(_ => _.auth);
  return (
    <>
      <NavigationView />
      <div className={styles.root}>
        <Alert variant='primary'>
          Welcome, you are logged in as <b>{auth.username}</b>
        </Alert>
        <EventsView/>
      </div>
    </>
  );
};

export default DashboardView;
