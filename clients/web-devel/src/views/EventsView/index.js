import * as React from 'react';
import fetchEvents from './fetchEvents';
import EventsChart from "../../components/Charts/EventsChart";
import { useSelector, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { updateEventsDisplay } from '~/state/actions';
import fetchGames from "../GamesView/fetchGames";
import styles from "../DashboardView/index.css";
import Alert from "react-bootstrap/Alert";

const EventsView = (props) => {
  const dispatch = useDispatch();
  const userId = useSelector(_ => _.auth.userId);
  const bearerToken = useSelector(_ => _.auth.bearerToken);
  // const active = recent.filter(_ => _.status === 'Started');
  // onComponentDidMount, load data
  React.useEffect(() => {
    fetchEvents({userId, bearerToken}).then(_ => {
      const domainData = {};
      const domainPathData = {}
      // dispatch(updateEventsDisplay(_.data))
    })
  }, []);

  return (
    <EventsChart/>
  );
};

export default EventsView;