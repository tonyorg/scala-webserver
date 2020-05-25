import * as React from 'react';
import { eventFetch } from "../../state/actions"
import EventsChart from "../../components/Charts/EventsChart";
import { useSelector, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { updateEventsDisplay } from '~/state/actions';
import fetchGames from "../GamesView/fetchGames";
import styles from "../DashboardView/index.css";
import Alert from "react-bootstrap/Alert";
import {useState} from "react";

const EventsView = (props) => {
  const dispatch = useDispatch();
  const userId = useSelector(_ => _.auth.userId);
  const bearerToken = useSelector(_ => _.auth.bearerToken);
  React.useEffect(() => {
      dispatch(eventFetch({userId, bearerToken}));
  }, []);

  return (
    <EventsChart/>
  );
};

export default EventsView;