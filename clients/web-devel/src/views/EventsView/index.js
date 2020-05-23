import * as React from 'react';
import fetchEvents from './fetchEvents';
import EventsChart from "../../components/Charts/EventsChart";
import { useSelector, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { updateEventsDisplay } from '~/state/actions';
import fetchGames from "../GamesView/fetchGames";
import styles from "../DashboardView/index.css";
import Alert from "react-bootstrap/Alert";
import {useState} from "react";

const EventsView = (props) => {
  const [data, setData] = useState([]);
  const dispatch = useDispatch();
  const userId = useSelector(_ => _.auth.userId);
  const bearerToken = useSelector(_ => _.auth.bearerToken);
  React.useEffect(() => {
    fetchEvents({userId, bearerToken}).then(_ => {
      if (_.data && _.data.fetchTopDomains) {
        if(_.data.fetchTopDomains.success === true) {
          setData(_.data.fetchTopDomains.events);
        } else {
          console.log(_.data.fetchTopDomains.message);
        }
      } else {
        console.log("Response format incorrect");
        console.log(_.data);
      }
      // dispatch(updateEventsDisplay(_.data))
    })
  }, []);

  return (
    <EventsChart
      data={data}
    />
  );
};

export default EventsView;